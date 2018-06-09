package com.ssig.smartcap.activity;

import android.annotation.SuppressLint;
import android.content.DialogInterface;
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
import com.ssig.smartcap.R;
import com.ssig.smartcap.adapter.ViewPagerAdapter;
import com.ssig.smartcap.fragment.AbstractMainFragment;
import com.ssig.smartcap.fragment.ArchiveFragment;
import com.ssig.smartcap.fragment.CaptureFragment;
import com.ssig.smartcap.fragment.SmartphoneFragment;
import com.ssig.smartcap.fragment.SmartwatchFragment;
import com.ssig.smartcap.fragment.TimeToolFragment;
import com.ssig.smartcap.utils.DeviceTools;
import com.ssig.sensorsmanager.time.NTPTime;
import com.ssig.smartcap.utils.Tools;
import com.ssig.smartcap.utils.WearUtil;

import java.io.IOException;
import java.lang.ref.WeakReference;
import java.util.Objects;


public class MainActivity extends AppCompatActivity {

    private Toolbar toolbar;
    private MenuItem ntpMenuItem;
    private MenuItem wearMenuItem;
    private AHBottomNavigationViewPager viewPager;
    private AHBottomNavigation bottomNavigation;

    public AbstractMainFragment captureFragment;
    public AbstractMainFragment smartphoneFragment;
    public AbstractMainFragment smartwatchFragment;
    public AbstractMainFragment timeToolFragment;
    public AbstractMainFragment archiveFragment;

    private ViewPagerAdapter viewPagerAdapter;
    private SharedPreferences sharedPreferences;
    private long exitTime = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        this.setContentView(R.layout.activity_main);
        this.sharedPreferences = this.getPreferences(MODE_PRIVATE);
        WearUtil.initialize(getApplicationContext());
        this.initUI();
    }

    @Override
    protected void onDestroy() {
        WearUtil.disconnect(this);
        super.onDestroy();
    }

    @Override
    public void onBackPressed() {
        if ((System.currentTimeMillis() - exitTime) > 2000) {
            Toast.makeText(this, R.string.util_exit_app_message, Toast.LENGTH_SHORT).show();
            exitTime = System.currentTimeMillis();
        } else {
            finish();
        }
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

    public void updateNTPMenuItem(){
        boolean initialized = NTPTime.isInitialized();
        if (initialized)
            this.ntpMenuItem.setIcon(R.drawable.ic_earth);
        else
            this.ntpMenuItem.setIcon(R.drawable.ic_earth_off);
        int color = ContextCompat.getColor(this, initialized ? R.color.colorAccent : R.color.colorAlert);
        Tools.changeDrawableColor(this.ntpMenuItem.getIcon(), color);
    }

    public void updateWearMenuItem(){
        boolean connected = WearUtil.isConnected();
        if (connected)
            this.wearMenuItem.setIcon(R.drawable.ic_smartwatch_on);
        else
            this.wearMenuItem.setIcon(R.drawable.ic_smartwatch_off);
        int color = ContextCompat.getColor(this, connected ? R.color.colorAccent : R.color.colorAlert);
        Tools.changeDrawableColor(this.wearMenuItem.getIcon(), color);
    }

    // ---------------------------------------------------------------------------------------------
    // BOTTOM NAVIGATION STUFFS
    // ---------------------------------------------------------------------------------------------
    private void initBottomNavigation(){

        this.bottomNavigation = findViewById(R.id.bottom_navigation);

        AHBottomNavigationAdapter bottomNavigationAdapter = new AHBottomNavigationAdapter(this, R.menu.menu_bottom_navigation);
        bottomNavigationAdapter.setupWithBottomNavigation(this.bottomNavigation);

        this.bottomNavigation.setTranslucentNavigationEnabled(true);
        this.bottomNavigation.setTitleState(AHBottomNavigation.TitleState.SHOW_WHEN_ACTIVE);
        this.bottomNavigation.setForceTint(true);
        this.bottomNavigation.setAccentColor(ContextCompat.getColor(this, R.color.colorPrimary));
        this.bottomNavigation.setOnTabSelectedListener(new AHBottomNavigation.OnTabSelectedListener() {
            @Override
            public boolean onTabSelected(int position, boolean wasSelected) {
                if (wasSelected) {
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

        this.captureFragment = new CaptureFragment();
        this.smartphoneFragment = new SmartphoneFragment();
        this.smartwatchFragment = new SmartwatchFragment();
        this.timeToolFragment = new TimeToolFragment();
        this.archiveFragment = new ArchiveFragment();

        this.viewPagerAdapter.add(this.captureFragment);
        this.viewPagerAdapter.add(this.smartphoneFragment);
        this.viewPagerAdapter.add(this.smartwatchFragment);
        this.viewPagerAdapter.add(this.timeToolFragment);
        this.viewPagerAdapter.add(this.archiveFragment);

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

    // ---------------------------------------------------------------------------------------------
    // NTP STUFFS
    // ---------------------------------------------------------------------------------------------
    public void startNTPSynchronization(){
        NTPTime.clear(this);
        if (DeviceTools.isNetworkConnected(this)) {
            new NTPSynchronizationTask(this).execute();
            if (WearUtil.isConnected()) {
                String ntpPool = sharedPreferences.getString(getString(R.string.preference_main_default_key_ntp_pool), getString(R.string.preference_main_default_ntp_pool));
                WearUtil.syncClientNTP(this, ntpPool);
            }
        } else{
            new MaterialDialog.Builder(this)
                    .title(R.string.dialog_network_error_title)
                    .content(R.string.dialog_network_error_content)
                    .icon(Tools.changeDrawableColor(Objects.requireNonNull(getDrawable(R.drawable.ic_wifi_off)), ContextCompat.getColor(this, R.color.colorPrimary)))
                    .cancelable(true)
                    .neutralText(R.string.button_cancel)
                    .positiveText(R.string.button_try_again)
                    .onPositive(new MaterialDialog.SingleButtonCallback() {
                        @Override
                        public void onClick(@NonNull MaterialDialog dialog, @NonNull DialogAction which) {
                            startNTPSynchronization();
                        }
                    })
                    .onNeutral(new MaterialDialog.SingleButtonCallback() {
                        @Override
                        public void onClick(@NonNull MaterialDialog dialog, @NonNull DialogAction which) {
                            dialog.cancel();
                        }
                    })
                    .cancelListener(new DialogInterface.OnCancelListener() {
                        @Override
                        public void onCancel(DialogInterface dialog) {
                            updateNTPMenuItem();
                            timeToolFragment.refresh();
                        }
                    })
                    .show();
        }
    }

    @SuppressLint("StaticFieldLeak")
    private class NTPSynchronizationTask extends AsyncTask<Void, Void, String>{

        private String ntpPool;
        private MaterialDialog dialog;
        private final WeakReference<MainActivity> mainActivity;

        NTPSynchronizationTask(MainActivity mainActivity){
            this.mainActivity = new WeakReference<>(mainActivity);
        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            this.ntpPool = sharedPreferences.getString(getString(R.string.preference_main_default_key_ntp_pool), getString(R.string.preference_main_default_ntp_pool));
            this.dialog = new MaterialDialog.Builder(this.mainActivity.get())
                .title(R.string.dialog_ntp_synchronization_title)
                .content(getString(R.string.dialog_ntp_synchronization_content) + " " + ntpPool)
                .icon(Tools.changeDrawableColor(Objects.requireNonNull(getDrawable(R.drawable.ic_earth)), ContextCompat.getColor(this.mainActivity.get(), R.color.colorPrimary)))
                .cancelable(false)
                .progress(true, 0)
                .show();

        }

        @Override
        protected String doInBackground(Void... voids) {
            try {
                Thread.sleep(500);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            try {
                NTPTime.initialize(mainActivity.get(), this.ntpPool);
            } catch (IOException e) {
                return e.getMessage();
            }
            return null;
        }

        @Override
        protected void onPostExecute(String message) {
            super.onPostExecute(message);
            this.dialog.dismiss();
            boolean refresh = (message == null);
            message = message == null ? this.mainActivity.get().getString(R.string.toast_ntp_synchronization_success) : message;
            Toast.makeText(this.mainActivity.get(), message, Toast.LENGTH_LONG).show();
            this.mainActivity.get().updateNTPMenuItem();
            if (refresh)
                timeToolFragment.refresh();
        }
    }

    // ---------------------------------------------------------------------------------------------
    // WEAR STUFFS
    // ---------------------------------------------------------------------------------------------
    public void startWearSynchronization(){
        if (!WearUtil.hasWearOS(this)) {
            new MaterialDialog.Builder(this)
                    .title(R.string.dialog_wear_os_error_title)
                    .content(R.string.dialog_wear_os_error_content)
                    .icon(Tools.changeDrawableColor(Objects.requireNonNull(getDrawable(R.drawable.ic_wear_os_color)), ContextCompat.getColor(this, R.color.colorPrimary)))
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
                    .icon(Tools.changeDrawableColor(Objects.requireNonNull(getDrawable(R.drawable.ic_bluetooth_off)), ContextCompat.getColor(this, R.color.colorPrimary)))
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

    @SuppressLint("StaticFieldLeak")
    private class WearSynchronizationTask extends AsyncTask< Void, Void, WearUtil.ConnectionResponse>{

        private MaterialDialog dialog;
        private final WeakReference<MainActivity> mainActivity;

        WearSynchronizationTask(MainActivity mainActivity){
            this.mainActivity = new WeakReference<>(mainActivity);
        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            this.dialog = new MaterialDialog.Builder(this.mainActivity.get())
                    .title(R.string.dialog_wear_synchronization_title)
                    .content(R.string.dialog_wear_synchronization_content)
                    .icon(Tools.changeDrawableColor(Objects.requireNonNull(getDrawable(R.drawable.ic_smartphone)), ContextCompat.getColor(this.mainActivity.get(), R.color.colorPrimary)))
                    .cancelable(false)
                    .progress(true, 0)
                    .show();
        }

        @Override
        protected WearUtil.ConnectionResponse doInBackground(Void... voids) {
            return WearUtil.synchronize(this.mainActivity.get());
        }

        @Override
        protected void onPostExecute(WearUtil.ConnectionResponse connectionResponse) {
            super.onPostExecute(connectionResponse);
            switch (connectionResponse){
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
                    break;
            }
            this.dialog.dismiss();
            this.mainActivity.get().updateWearMenuItem();
            smartwatchFragment.refresh();
            captureFragment.refresh();
            if (NTPTime.isInitialized())
                startNTPSynchronization();
            WearUtil.connectionDone(this.mainActivity.get());
        }

    }


}
