package com.ssig.smartcap.activity;

import android.annotation.SuppressLint;
import android.content.SharedPreferences;
import android.graphics.PorterDuff;
import android.graphics.drawable.Drawable;
import android.hardware.SensorManager;
import android.net.Uri;
import android.os.AsyncTask;
import android.support.annotation.NonNull;
import android.support.v4.content.ContextCompat;
import android.support.v4.util.Pair;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.view.menu.MenuBuilder;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Toast;

import com.afollestad.materialdialogs.DialogAction;
import com.afollestad.materialdialogs.MaterialDialog;
import com.aurelhubert.ahbottomnavigation.AHBottomNavigation;
import com.aurelhubert.ahbottomnavigation.AHBottomNavigationAdapter;
import com.aurelhubert.ahbottomnavigation.AHBottomNavigationViewPager;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.android.gms.tasks.Tasks;
import com.google.android.gms.wearable.CapabilityClient;
import com.google.android.gms.wearable.CapabilityInfo;
import com.google.android.gms.wearable.DataItem;
import com.google.android.gms.wearable.Node;
import com.google.android.gms.wearable.PutDataRequest;
import com.google.android.gms.wearable.Wearable;
import com.ssig.sensorsmanager.SensorInfoFactory;
import com.ssig.sensorsmanager.SensorType;
import com.ssig.smartcap.R;
import com.ssig.smartcap.adapter.ViewPagerAdapter;
import com.ssig.smartcap.fragment.AbstractMainFragment;
import com.ssig.smartcap.fragment.ArchiveFragment;
import com.ssig.smartcap.fragment.SmartphoneFragment;
import com.ssig.smartcap.fragment.SmartwatchFragment;
import com.ssig.smartcap.fragment.TimeToolFragment;
import com.ssig.smartcap.utils.DeviceTools;
import com.ssig.smartcap.utils.TimeUtils;
import com.ssig.smartcap.utils.Tools;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectOutput;
import java.io.ObjectOutputStream;
import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ExecutionException;


public class MainActivity extends AppCompatActivity implements CapabilityClient.OnCapabilityChangedListener {

    private Toolbar toolbar;
    private MenuItem ntpMenuItem;
    private MenuItem wearMenuItem;
    private AHBottomNavigationViewPager viewPager;
    private AHBottomNavigation bottomNavigation;
    protected List<Node> wearClientNodes;
    protected List<Node> wearSmartcapClientNodes;
    protected Node wearNode;

    private ViewPagerAdapter viewPagerAdapter;
    private AHBottomNavigationAdapter bottomNavigationAdapter;
    private SharedPreferences sharedPreferences;
    private AbstractMainFragment currentFragment;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        this.sharedPreferences = this.getPreferences(MODE_PRIVATE);
        this.wearClientNodes = null;
        this.wearSmartcapClientNodes = null;
        this.wearNode = null;
        this.initUI();
    }

    @Override
    protected void onResume() {
        super.onResume();
        Wearable.getCapabilityClient(this).addListener(this, Uri.parse("wear://"), CapabilityClient.FILTER_ALL);
    }

    private void initUI() {
        this.initToolbar();
        this.initBottomNavigation();
        this.initPagerView();
    }


    // ---------------------------------------------------------------------------------------------
    // ACTION TOOLBAR STUFFS
    // ---------------------------------------------------------------------------------------------
    private void initToolbar() {
        this.toolbar = findViewById(R.id.appbar_toolbar);
        this.setSupportActionBar(this.toolbar);
        Tools.setSystemBarColor(this, R.color.colorGreyLight);
        Tools.setSystemBarLight(this);
    }

    @SuppressLint("RestrictedApi")
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_action, menu);
        if(menu instanceof MenuBuilder){
            MenuBuilder m = (MenuBuilder) menu;
            m.setOptionalIconsVisible(true);
        }
        Tools.changeMenuIconColor(menu, ContextCompat.getColor(this, R.color.colorGrey));
        this.ntpMenuItem = menu.findItem(R.id.action_ntp);
        this.wearMenuItem = menu.findItem(R.id.action_wear);
        this.updateNTPMenuItem();
        this.updateWearMenuItem();
        return true;
    }

    public void updateNTPMenuItem(){
        if (TimeUtils.ntpIsInitialized())
            this.ntpMenuItem.setIcon(R.drawable.ic_earth);
        else
            this.ntpMenuItem.setIcon(R.drawable.ic_earth_off);
    }

    public void updateWearMenuItem(){
        if (this.hasWearClientNodes() && this.hasWearSmartClientNodes())
            this.wearMenuItem.setIcon(R.drawable.ic_smartwatch);
        else
            this.wearMenuItem.setIcon(R.drawable.ic_smartwatch_off);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int itemID = item.getItemId();
        if (itemID != android.R.id.home) {
            switch (itemID){
                case R.id.action_ntp:
                    startNTPSynchronization();
                    break;
                case R.id.action_wear:
                    startWearSynchronization();
                    break;
                case R.id.action_settings:
                    this.sendMessage(this.wearNode, "TESTE");
                    Toast.makeText(getApplicationContext(), item.getTitle(), Toast.LENGTH_SHORT).show();
                    break;
                case R.id.action_about:
                    Toast.makeText(getApplicationContext(), item.getTitle(), Toast.LENGTH_SHORT).show();
                    break;
            }

        }
        return super.onOptionsItemSelected(item);
    }


    // ---------------------------------------------------------------------------------------------
    // BOTTOM NAVIGATION STUFFS
    // ---------------------------------------------------------------------------------------------
    private void initBottomNavigation(){

        this.bottomNavigation = findViewById(R.id.bottom_navigation);


        this.bottomNavigationAdapter = new AHBottomNavigationAdapter(this, R.menu.menu_bottom_navigation);
        this.bottomNavigationAdapter.setupWithBottomNavigation(this.bottomNavigation);

        this.bottomNavigation.setTranslucentNavigationEnabled(true);
        this.bottomNavigation.setTitleState(AHBottomNavigation.TitleState.SHOW_WHEN_ACTIVE);
        this.bottomNavigation.setForceTint(true);
        this.bottomNavigation.setAccentColor(ContextCompat.getColor(this, R.color.colorPrimary));
        this.bottomNavigation.setOnTabSelectedListener(new AHBottomNavigation.OnTabSelectedListener() {
            @Override
            public boolean onTabSelected(int position, boolean wasSelected) {

                if (currentFragment == null) {
                    currentFragment = viewPagerAdapter.getCurrentFragment();
                }

                if (wasSelected) {
                    currentFragment.refresh();
                    return true;
                }

                if (currentFragment != null) {
                    currentFragment.willBeHidden();
                }

                setCurrentFragment(position);
                viewPager.setCurrentItem(position, false);

                if (currentFragment == null) {
                    return true;
                }

                currentFragment = viewPagerAdapter.getCurrentFragment();
                currentFragment.refresh();
                currentFragment.willBeDisplayed();
                return true;
            }
        });

    }


    // ---------------------------------------------------------------------------------------------
    // PAGE VIEWER STUFFS
    // ---------------------------------------------------------------------------------------------
    public void initPagerView(){
        this.viewPager = findViewById(R.id.view_pager);

        this.viewPagerAdapter = new ViewPagerAdapter(getSupportFragmentManager());
        this.viewPagerAdapter.add(new ArchiveFragment());
        this.viewPagerAdapter.add(new SmartphoneFragment());
        this.viewPagerAdapter.add(new SmartwatchFragment());
        this.viewPagerAdapter.add(new TimeToolFragment());
        this.viewPagerAdapter.add(new ArchiveFragment());

        this.viewPager.setAdapter(viewPagerAdapter);
    }

    public void setCurrentFragment(int position){
        String fragmentTitle = this.bottomNavigation.getItem(position).getTitle(this);
        Drawable fragmentIcon = this.bottomNavigation.getItem(position).getDrawable(this);
        this.toolbar.setTitle(fragmentTitle);
        this.toolbar.setNavigationIcon(fragmentIcon);
        Objects.requireNonNull(this.toolbar.getNavigationIcon()).setColorFilter(ContextCompat.getColor(this, R.color.colorPrimary), PorterDuff.Mode.SRC_ATOP);
        this.viewPager.setCurrentItem(position, false);
    }

    public void refreshCurrentFragment(){
        if (this.currentFragment != null)
            this.currentFragment.refresh();
    }


    // ---------------------------------------------------------------------------------------------
    // NTP STUFFS
    // ---------------------------------------------------------------------------------------------
    public void startNTPSynchronization(){
        if (DeviceTools.isNetworkConnected(this)) {
            new NTPSynchronizationTask(this).execute();
        } else{
            new MaterialDialog.Builder(this)
                    .title(R.string.dialog_network_error_title)
                    .content(R.string.dialog_network_error_content)
                    .icon(Tools.changeDrawableColor(getDrawable(R.drawable.ic_wifi_off), ContextCompat.getColor(this, R.color.colorPrimary)))
                    .cancelable(true)
                    .neutralText(R.string.button_cancel)
                    .positiveText(R.string.button_try_again)
                    .onPositive(new MaterialDialog.SingleButtonCallback() {
                        @Override
                        public void onClick(@NonNull MaterialDialog dialog, @NonNull DialogAction which) {
                            startNTPSynchronization();
                        }
                    })
                    .show();
        }
    }

    private class NTPSynchronizationTask extends AsyncTask<Void, Void, Boolean>{

        private String ntpPool;
        private MaterialDialog dialog;
        private final WeakReference<MainActivity> mainActivity;

        public NTPSynchronizationTask(MainActivity mainActivity){
            this.mainActivity = new WeakReference<>(mainActivity);
        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            this.ntpPool = sharedPreferences.getString(getString(R.string.preference_key_ntp_pool), getString(R.string.preference_default_ntp_pool));
            this.dialog = new MaterialDialog.Builder(this.mainActivity.get())
                .title(R.string.dialog_ntp_synchronization_title)
                .content(getString(R.string.dialog_ntp_synchronization_content) + " " + ntpPool)
                .icon(Tools.changeDrawableColor(getDrawable(R.drawable.ic_earth), ContextCompat.getColor(this.mainActivity.get(), R.color.colorPrimary)))
                .cancelable(false)
                .progress(true, 0)
                .show();

        }

        @Override
        protected Boolean doInBackground(Void... voids) {
            boolean response = false;
            TimeUtils.clearNTPCache(this.mainActivity.get());
            try {
                Thread.sleep(500);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            try {
                response = TimeUtils.initializeNTP(this.mainActivity.get(), this.ntpPool);
            } catch (IOException e) {
                Toast.makeText(this.mainActivity.get(), e.toString(), Toast.LENGTH_LONG).show();
            }
            return response;
        }

        @Override
        protected void onPostExecute(Boolean aBoolean) {
            super.onPostExecute(aBoolean);
            Toast.makeText(this.mainActivity.get(), getString(R.string.dialog_ntp_synchronization_success), Toast.LENGTH_LONG).show();
            this.dialog.dismiss();
            this.mainActivity.get().updateNTPMenuItem();
            this.mainActivity.get().refreshCurrentFragment();
        }
    }


    // ---------------------------------------------------------------------------------------------
    // ---------------------------------------------------------------------------------------------
    // ---------------------------------------------------------------------------------------------
    // ---------------------------------------------------------------------------------------------
    // WEAR STUFFS
    // ---------------------------------------------------------------------------------------------
    // ---------------------------------------------------------------------------------------------
    // ---------------------------------------------------------------------------------------------
    // ---------------------------------------------------------------------------------------------
    @Override
    public void onCapabilityChanged(@NonNull CapabilityInfo capabilityInfo) {
        String info = capabilityInfo.toString();
        Toast.makeText(this, info, Toast.LENGTH_LONG);
    }

    public void startWearSynchronization(){
        if (DeviceTools.isBlueetothEnabled()) {
            new WearSynchronizationTask(this).execute();
        } else{
            new MaterialDialog.Builder(this)
                    .title(R.string.dialog_bluetooth_error_title)
                    .content(R.string.dialog_bluetooth_error_content)
                    .icon(Tools.changeDrawableColor(getDrawable(R.drawable.ic_bluetooth_off), ContextCompat.getColor(this, R.color.colorPrimary)))
                    .cancelable(true)
                    .neutralText(R.string.button_cancel)
                    .positiveText(R.string.button_try_again)
                    .onPositive(new MaterialDialog.SingleButtonCallback() {
                        @Override
                        public void onClick(@NonNull MaterialDialog dialog, @NonNull DialogAction which) {
                            startWearSynchronization();
                        }
                    })
                    .show();
        }
    }

    private class WearSynchronizationTask extends AsyncTask< Void, Void, Pair< List<Node>, List<Node> > >{

        private MaterialDialog dialog;
        private final WeakReference<MainActivity> mainActivity;

        public WearSynchronizationTask(MainActivity mainActivity){
            this.mainActivity = new WeakReference<>(mainActivity);
        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            this.dialog = new MaterialDialog.Builder(this.mainActivity.get())
                    .title(R.string.dialog_wear_synchronization_title)
                    .content(R.string.dialog_wear_synchronization_content)
                    .icon(Tools.changeDrawableColor(getDrawable(R.drawable.ic_smartphone), ContextCompat.getColor(this.mainActivity.get(), R.color.colorPrimary)))
                    .cancelable(false)
                    .progress(true, 0)
                    .show();
        }

        @Override
        protected Pair< List<Node>, List<Node> > doInBackground(Void... voids) {
            List<Node> wearClientNodes = null;
            Map<String, CapabilityInfo> wearSmartcapClientCapabilities = null;
            Task<List<Node>> wearClientNodeListTask = Wearable.getNodeClient(getApplicationContext()).getConnectedNodes();
            Task<Map<String, CapabilityInfo>> wearSmartcapClientCapabilitiesListTask = Wearable.getCapabilityClient(getApplicationContext()).getAllCapabilities(CapabilityClient.FILTER_ALL);
            try {
                wearClientNodes = Tasks.await(wearClientNodeListTask);
                wearSmartcapClientCapabilities = Tasks.await(wearSmartcapClientCapabilitiesListTask);
                int a = 0;
            } catch (InterruptedException | ExecutionException e) {
                Toast.makeText(this.mainActivity.get(), e.toString(), Toast.LENGTH_LONG).show();
            }
            if (wearClientNodes != null && wearClientNodes.size() == 0)
                wearClientNodes = null;
            List<Node> wearSmartcapClientNodes = null;
            if (wearSmartcapClientCapabilities != null && wearSmartcapClientCapabilities.size() >= 0) {
                for (Map.Entry<String, CapabilityInfo> entry : wearSmartcapClientCapabilities.entrySet()) {
                    if (entry.getKey().equals(getString(R.string.capability_smartcap_wear)) && !entry.getValue().getNodes().isEmpty()) {
                        wearSmartcapClientNodes = new ArrayList<>(entry.getValue().getNodes());
                    }
                }
            }
            return new Pair<>(wearClientNodes, wearSmartcapClientNodes);
        }

        @Override
        protected void onPostExecute(Pair< List<Node>, List<Node> > nodes) {
            super.onPostExecute(nodes);
            this.mainActivity.get().wearClientNodes = nodes.first;
            this.mainActivity.get().wearSmartcapClientNodes = nodes.second;
            if(this.mainActivity.get().hasWearClientNodes() && this.mainActivity.get().hasWearSmartClientNodes()) {
                Toast.makeText(this.mainActivity.get(), getString(R.string.dialog_wear_synchronization_success), Toast.LENGTH_LONG).show();
                this.mainActivity.get().wearNode = this.mainActivity.get().wearSmartcapClientNodes.get(0);
                this.mainActivity.get().sendMessage(this.mainActivity.get().wearNode, getString(R.string.message_path_open_watch_activity));
                this.mainActivity.get().sendData(this.mainActivity.get().wearNode);
            }else {
                Toast.makeText(this.mainActivity.get(), getString(R.string.dialog_wear_synchronization_not_success), Toast.LENGTH_LONG).show();
            }
            this.dialog.dismiss();
            this.mainActivity.get().updateWearMenuItem();
            this.mainActivity.get().refreshCurrentFragment();
        }

    }

    public boolean hasWearClientNodes() {
        return (this.wearClientNodes != null);
    }

    public boolean hasWearSmartClientNodes() {
        return (this.wearSmartcapClientNodes != null);
    }

    public void sendMessage(final Node nodeToSend, final String path){
//        Thread thread = new Thread(new Runnable() {
//            @Override
//            public void run() {
//                Task<Integer> sendMessageTask = Wearable.getMessageClient(MainActivity.this).sendMessage(nodeToSend.getId(), path, new byte[0]);
//                try {
//                    Tasks.await(sendMessageTask);
//                } catch (ExecutionException|InterruptedException e) {}
//            }
//        });
//        thread.start();
        Task<Integer> sendMessageTask = Wearable.getMessageClient(MainActivity.this).sendMessage(nodeToSend.getId(), path, new byte[0]);
        sendMessageTask.addOnSuccessListener(new OnSuccessListener<Integer>() {
            @Override
            public void onSuccess(Integer integer) {

            }
        });
    }

    public void sendData(final Node nodeToSend){

        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        ObjectOutput out = null;
        try {
            out = new ObjectOutputStream(bos);
            out.writeObject(SensorInfoFactory.getSensorInfo(this, SensorType.TYPE_ACCELEROMETER));
            out.flush();
        } catch (IOException e) {
            e.printStackTrace();
        }
        byte[] yourBytes = bos.toByteArray();
        PutDataRequest putDataRequest = PutDataRequest.create("/teste");
        putDataRequest.setData(yourBytes);
        putDataRequest.setUrgent();
        Task<DataItem> dataItemTask = Wearable.getDataClient(this).putDataItem(putDataRequest);
        dataItemTask.addOnSuccessListener(new OnSuccessListener<DataItem>() {
            @Override
            public void onSuccess(DataItem dataItem) {}
        });

    }



    public List<Node> getWearClientNodes(){
        return this.wearClientNodes;
    }


}
