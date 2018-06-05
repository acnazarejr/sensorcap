package com.ssig.smartcap.activity;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.PorterDuff;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.AsyncTask;
import android.support.annotation.NonNull;
import android.support.v4.content.ContextCompat;
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
import com.google.android.gms.wearable.CapabilityClient;
import com.google.android.gms.wearable.CapabilityInfo;
import com.google.android.gms.wearable.Wearable;
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
import com.ssig.smartcap.utils.WearUtil;

import java.io.IOException;
import java.lang.ref.WeakReference;
import java.util.Objects;


public class MainActivity extends AppCompatActivity implements CapabilityClient.OnCapabilityChangedListener {

    private Toolbar toolbar;
    private MenuItem ntpMenuItem;
    private MenuItem wearMenuItem;
    private AHBottomNavigationViewPager viewPager;
    private AHBottomNavigation bottomNavigation;


    private ViewPagerAdapter viewPagerAdapter;
    private AHBottomNavigationAdapter bottomNavigationAdapter;
    private SharedPreferences sharedPreferences;
//    private AbstractMainFragment currentFragment;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        this.sharedPreferences = this.getPreferences(MODE_PRIVATE);
        WearUtil.initialize(getApplicationContext());
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
        Tools.setSystemBarColor(this, R.color.colorGreyMediumLight);
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
        if (WearUtil.get().hasWearClientNodes())
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
//                    this.sendMessage(this.wearNode, "TESTE");
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
                if (wasSelected) {
                    refreshCurrentFragment();
                    return true;
                }
                setCurrentFragment(position);
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
        this.viewPager.setOffscreenPageLimit(5);
    }

    public void setCurrentFragment(int position){

        AbstractMainFragment currentFragment = this.viewPagerAdapter.getCurrentFragment();
        currentFragment.hide();

        String fragmentTitle = this.bottomNavigation.getItem(position).getTitle(this);
        Drawable fragmentIcon = this.bottomNavigation.getItem(position).getDrawable(this);
        this.toolbar.setTitle(fragmentTitle);
        this.toolbar.setNavigationIcon(fragmentIcon);
        Objects.requireNonNull(this.toolbar.getNavigationIcon()).setColorFilter(ContextCompat.getColor(this, R.color.colorPrimary), PorterDuff.Mode.SRC_ATOP);
        this.viewPager.setCurrentItem(position, false);

        currentFragment = this.viewPagerAdapter.getCurrentFragment();
        currentFragment.show();
    }

    public void refreshCurrentFragment(){
        AbstractMainFragment currentFragment = this.viewPagerAdapter.getCurrentFragment();
        if (currentFragment != null)
            currentFragment.refresh();
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
            Toast.makeText(this.mainActivity.get(), getString(R.string.toast_ntp_synchronization_success), Toast.LENGTH_LONG).show();
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
        if (!WearUtil.get().hasWearOS()) {
            new MaterialDialog.Builder(this)
                    .title(R.string.dialog_wear_os_error_title)
                    .content(R.string.dialog_wear_os_error_content)
                    .icon(Tools.changeDrawableColor(getDrawable(R.drawable.ic_wear_os_color), ContextCompat.getColor(this, R.color.colorPrimary)))
                    .cancelable(true)
                    .neutralText(R.string.button_cancel)
                    .positiveText(R.string.dialog_wear_os_error_button)
                    .onPositive(new MaterialDialog.SingleButtonCallback() {
                        @Override
                        public void onClick(@NonNull MaterialDialog dialog, @NonNull DialogAction which) {
                            Intent goToMarket = new Intent(Intent.ACTION_VIEW).setData(Uri.parse(String.format("market://details?id=%s", getString(R.string.util_wear_package))));
                            startActivity(goToMarket);
                        }
                    })
                    .show();
        } else if (!DeviceTools.isBlueetothEnabled()) {
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
        } else{
            new WearSynchronizationTask(this).execute();
        }
    }

    private class WearSynchronizationTask extends AsyncTask< Void, Void, WearUtil.SynchronizationResponse >{

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
        protected WearUtil.SynchronizationResponse doInBackground(Void... voids) {
            return WearUtil.get().synchronize();
        }

        @Override
        protected void onPostExecute(WearUtil.SynchronizationResponse synchronizationResponse) {
            super.onPostExecute(synchronizationResponse);
            switch (synchronizationResponse){
                case UNKNOWN_ERROR:
                    Toast.makeText(this.mainActivity.get(), getString(R.string.toast_wear_synchronization_unknown_error), Toast.LENGTH_LONG).show();
                    break;
                case NO_WEAR_APP:
                    Toast.makeText(this.mainActivity.get(), getString(R.string.toast_wear_synchronization_unknown_error), Toast.LENGTH_LONG).show();
                    break;
                case BLUETOOTH_DISABLED:
                    Toast.makeText(this.mainActivity.get(), getString(R.string.toast_wear_synchronization_bluetooth_error), Toast.LENGTH_LONG).show();
                    break;
                case NO_PAIRED_DEVICES:
                    Toast.makeText(this.mainActivity.get(), getString(R.string.toast_wear_synchronization_no_paired_error), Toast.LENGTH_LONG).show();
                    break;
                case NO_CAPABLE_DEVICES:
                    Toast.makeText(this.mainActivity.get(), getString(R.string.toast_wear_synchronization_no_capable_error), Toast.LENGTH_LONG).show();
                    break;
                case SUCCESS:
                    Toast.makeText(this.mainActivity.get(), getString(R.string.toast_wear_synchronization_success), Toast.LENGTH_LONG).show();
                    WearUtil.get().openClientActivity();
                    break;
            }
            this.dialog.dismiss();
            this.mainActivity.get().updateWearMenuItem();
            this.mainActivity.get().refreshCurrentFragment();
        }

    }

//    public void sendMessage(final Node nodeToSend, final String path){
////        Thread thread = new Thread(new Runnable() {
////            @Override
////            public void run() {
////                Task<Integer> sendMessageTask = Wearable.getMessageClient(MainActivity.this).sendMessage(nodeToSend.getId(), path, new byte[0]);
////                try {
////                    Tasks.await(sendMessageTask);
////                } catch (ExecutionException|InterruptedException e) {}
////            }
////        });
////        thread.start();
//        Task<Integer> sendMessageTask = Wearable.getMessageClient(MainActivity.this).sendMessage(nodeToSend.getId(), path, new byte[0]);
//        sendMessageTask.addOnSuccessListener(new OnSuccessListener<Integer>() {
//            @Override
//            public void onSuccess(Integer integer) {
//
//            }
//        });
//    }
//
//    public void sendData(final Node nodeToSend){
//
//        ByteArrayOutputStream bos = new ByteArrayOutputStream();
//        ObjectOutput out = null;
//        try {
//            out = new ObjectOutputStream(bos);
//            out.writeObject(SensorInfoFactory.getSensorInfo(this, SensorType.TYPE_ACCELEROMETER));
//            out.flush();
//        } catch (IOException e) {
//            e.printStackTrace();
//        }
//        byte[] yourBytes = bos.toByteArray();
//        PutDataRequest putDataRequest = PutDataRequest.create("/teste");
//        putDataRequest.setData(yourBytes);
//        putDataRequest.setUrgent();
//        Task<DataItem> dataItemTask = Wearable.getDataClient(this).putDataItem(putDataRequest);
//        dataItemTask.addOnSuccessListener(new OnSuccessListener<DataItem>() {
//            @Override
//            public void onSuccess(DataItem dataItem) {}
//        });
//
//    }
//
//
//
//


    @Override
    public void onBackPressed() {
        doExitApp();
    }

    private long exitTime = 0;
    public void doExitApp() {
        if ((System.currentTimeMillis() - exitTime) > 2000) {
            Toast.makeText(this, "Press again to exit app", Toast.LENGTH_SHORT).show();
            exitTime = System.currentTimeMillis();
        } else {
            finish();
        }
    }

}
