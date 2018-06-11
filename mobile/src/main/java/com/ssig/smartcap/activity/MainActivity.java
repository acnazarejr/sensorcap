package com.ssig.smartcap.activity;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.SharedPreferences;
import android.graphics.PorterDuff;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.IBinder;
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
import com.google.android.gms.wearable.MessageClient;
import com.google.android.gms.wearable.MessageEvent;
import com.google.android.gms.wearable.Wearable;
import com.karumi.dexter.Dexter;
import com.karumi.dexter.MultiplePermissionsReport;
import com.karumi.dexter.PermissionToken;
import com.karumi.dexter.listener.PermissionRequest;
import com.karumi.dexter.listener.multi.MultiplePermissionsListener;
import com.ssig.smartcap.R;
import com.ssig.smartcap.adapter.ViewPagerAdapter;
import com.ssig.smartcap.fragment.AbstractMainFragment;
import com.ssig.smartcap.fragment.ArchiveFragment;
import com.ssig.smartcap.fragment.CaptureFragment;
import com.ssig.smartcap.fragment.SmartphoneFragment;
import com.ssig.smartcap.fragment.SmartwatchFragment;
import com.ssig.smartcap.fragment.TimeToolFragment;
import com.ssig.smartcap.service.WearService;
import com.ssig.smartcap.utils.DeviceTools;
import com.ssig.sensorsmanager.time.NTPTime;
import com.ssig.smartcap.utils.Tools;
import com.ssig.smartcap.utils.WearUtil;

import java.io.IOException;
import java.lang.ref.WeakReference;
import java.util.List;
import java.util.Objects;


public class MainActivity extends AppCompatActivity implements MessageClient.OnMessageReceivedListener{

    private SharedPreferences sharedPreferences;
    private long exitTime = 0;

    private Toolbar mToolbar;
    private MenuItem mNTPMenuItem;
    private MenuItem mWearMenuItem;
    private AHBottomNavigationViewPager mViewPager;
    private AHBottomNavigation mBottomNavigation;

    private ViewPagerAdapter mViewPagerAdapter;
    public AbstractMainFragment captureFragment;
    public AbstractMainFragment smartphoneFragment;
    public AbstractMainFragment smartwatchFragment;
    public AbstractMainFragment timeToolFragment;
    public AbstractMainFragment archiveFragment;


    private WearService mWearService;
    private boolean mWearServiceBounded;
    private ServiceConnection mWearServiceConnection;

    // ---------------------------------------------------------------------------------------------
    // ACTIVITY STUFFS
    // ---------------------------------------------------------------------------------------------
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        this.setContentView(R.layout.activity_main);

        this.sharedPreferences = this.getPreferences(MODE_PRIVATE);
        this.mWearServiceConnection = this.createWearServiceConnection();

        Intent wearServiceIntent = new Intent(this, WearService.class);
        bindService(wearServiceIntent, this.mWearServiceConnection, Context.BIND_AUTO_CREATE);

        this.initUI();
    }

    @Override
    protected void onStart() {
        super.onStart();
        this.requestPermissions();

        final String uri = String.format("wear://*%s", getString(R.string.message_path_host_activity_prefix));
        Wearable.getMessageClient(this).addListener(this, Uri.parse(uri), MessageClient.FILTER_PREFIX);
    }

    @Override
    protected void onStop() {
        super.onStop();
        Wearable.getMessageClient(this).removeListener(this);
    }

    @Override
    protected void onDestroy() {
        WearUtil.disconnect(this);

        if (this.mWearServiceBounded){
            unbindService(this.mWearServiceConnection);
            this.mWearServiceBounded = false;
        }

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

    @Override
    public void onMessageReceived(@NonNull MessageEvent messageEvent) {
        boolean connected = getWearService() != null && this.mWearService.isConnected();
        if (!connected)
            return;
        String path = messageEvent.getPath();
        if (path.equals(getString(R.string.message_path_host_activity_disconnect))){
            getWearService().disconnect(false);
            this.updateWearMenuItem();
            this.smartwatchFragment.refresh();
            this.captureFragment.refresh();
        }
    }

    private void initUI() {
        this.initToolbar();
        this.initBottomNavigation();
        this.initPagerView();
    }

    private void requestPermissions(){
        Dexter.withActivity(this)
                .withPermissions(
                        Manifest.permission.ACCESS_NETWORK_STATE,
                        Manifest.permission.BLUETOOTH,
                        Manifest.permission.WRITE_EXTERNAL_STORAGE,
                        Manifest.permission.VIBRATE
                ).withListener(new MultiplePermissionsListener() {
            @Override
            public void onPermissionsChecked(MultiplePermissionsReport report) {
                if (!report.areAllPermissionsGranted()){
                    new MaterialDialog.Builder(MainActivity.this)
                            .title(R.string.dialog_permissions_failed_title)
                            .titleColorRes(R.color.colorAlert)
                            .content(R.string.dialog_permissions_failed_content)
                            .icon(Tools.changeDrawableColor(Objects.requireNonNull(MainActivity.this.getDrawable(R.drawable.ic_smartphone_lock)), ContextCompat.getColor(MainActivity.this, R.color.colorAlert)))
                            .cancelable(false)
                            .positiveText(R.string.button_try_again)
                            .onPositive(new MaterialDialog.SingleButtonCallback() {
                                @Override
                                public void onClick(@NonNull MaterialDialog dialog, @NonNull DialogAction which) {
                                    requestPermissions();
                                }
                            })
                            .negativeText(R.string.button_exit)
                            .onNegative(new MaterialDialog.SingleButtonCallback() {
                                @Override
                                public void onClick(@NonNull MaterialDialog dialog, @NonNull DialogAction which) {
                                    finish();
                                }
                            })
                            .show();
                }
            }

            @Override
            public void onPermissionRationaleShouldBeShown(List<PermissionRequest> permissions, PermissionToken token) {
                token.continuePermissionRequest();
            }
        }).check();
    }

    // ---------------------------------------------------------------------------------------------
    // ACTION TOOLBAR STUFFS
    // ---------------------------------------------------------------------------------------------
    private void initToolbar() {
        this.mToolbar = findViewById(R.id.appbar_toolbar);
        this.setSupportActionBar(this.mToolbar);
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
        this.mNTPMenuItem = menu.findItem(R.id.action_ntp);
        this.mWearMenuItem = menu.findItem(R.id.action_wear);
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
                    doNTPSynchronization();
                    break;
                case R.id.action_wear:
                    doWearConnection();
                    break;
                case R.id.action_settings:
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
            this.mNTPMenuItem.setIcon(R.drawable.ic_ntp_on);
        else
            this.mNTPMenuItem.setIcon(R.drawable.ic_ntp_off);
        int color = ContextCompat.getColor(this, initialized ? R.color.colorAccent : R.color.colorAlert);
        Tools.changeDrawableColor(this.mNTPMenuItem.getIcon(), color);
    }

    public void updateWearMenuItem(){
        boolean connected = this.getWearService() != null && this.getWearService().isConnected();
        if (connected)
            this.mWearMenuItem.setIcon(R.drawable.ic_smartwatch_on);
        else
            this.mWearMenuItem.setIcon(R.drawable.ic_smartwatch_off);
        int color = ContextCompat.getColor(this, connected ? R.color.colorAccent : R.color.colorAlert);
        Tools.changeDrawableColor(this.mWearMenuItem.getIcon(), color);
    }

    // ---------------------------------------------------------------------------------------------
    // BOTTOM NAVIGATION STUFFS
    // ---------------------------------------------------------------------------------------------
    private void initBottomNavigation(){

        this.mBottomNavigation = findViewById(R.id.bottom_navigation);

        AHBottomNavigationAdapter bottomNavigationAdapter = new AHBottomNavigationAdapter(this, R.menu.menu_bottom_navigation);
        bottomNavigationAdapter.setupWithBottomNavigation(this.mBottomNavigation);

        this.mBottomNavigation.setTranslucentNavigationEnabled(true);
        this.mBottomNavigation.setTitleState(AHBottomNavigation.TitleState.SHOW_WHEN_ACTIVE);
        this.mBottomNavigation.setForceTint(true);
        this.mBottomNavigation.setAccentColor(ContextCompat.getColor(this, R.color.colorPrimary));
        this.mBottomNavigation.setOnTabSelectedListener(new AHBottomNavigation.OnTabSelectedListener() {
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
        this.mViewPager = findViewById(R.id.view_pager);

        this.mViewPagerAdapter = new ViewPagerAdapter(getSupportFragmentManager());

        this.captureFragment = new CaptureFragment();
        this.smartphoneFragment = new SmartphoneFragment();
        this.smartwatchFragment = new SmartwatchFragment();
        this.timeToolFragment = new TimeToolFragment();
        this.archiveFragment = new ArchiveFragment();

        this.mViewPagerAdapter.add(this.captureFragment);
        this.mViewPagerAdapter.add(this.smartphoneFragment);
        this.mViewPagerAdapter.add(this.smartwatchFragment);
        this.mViewPagerAdapter.add(this.timeToolFragment);
        this.mViewPagerAdapter.add(this.archiveFragment);

        this.mViewPager.setAdapter(mViewPagerAdapter);
        this.mViewPager.setOffscreenPageLimit(5);
    }

    public void setCurrentFragment(int position){

        AbstractMainFragment currentFragment = this.mViewPagerAdapter.getCurrentFragment();
        currentFragment.hide();

        String fragmentTitle = this.mBottomNavigation.getItem(position).getTitle(this);
        Drawable fragmentIcon = this.mBottomNavigation.getItem(position).getDrawable(this);
        this.mToolbar.setTitle(fragmentTitle);
        this.mToolbar.setNavigationIcon(fragmentIcon);
        Objects.requireNonNull(this.mToolbar.getNavigationIcon()).setColorFilter(ContextCompat.getColor(this, R.color.colorPrimary), PorterDuff.Mode.SRC_ATOP);
        this.mViewPager.setCurrentItem(position, false);

        currentFragment = this.mViewPagerAdapter.getCurrentFragment();
        currentFragment.show();
    }

    // ---------------------------------------------------------------------------------------------
    // NTP STUFFS
    // ---------------------------------------------------------------------------------------------
    public void doNTPSynchronization(){

        if (NTPTime.isInitialized()){

            NTPTime.close(this);
            if (this.getWearService().isConnected()) {
                this.getWearService().closeClientNTP();
            }
            this.updateNTPMenuItem();
            this.timeToolFragment.refresh();

        }else {

            if (DeviceTools.isNetworkConnected(this)) {
                new NTPSynchronizationTask(this).execute();
                if (this.getWearService().isConnected()) {
                    String ntpPool = sharedPreferences.getString(getString(R.string.preference_main_default_key_ntp_pool), getString(R.string.preference_main_default_ntp_pool));
                    this.getWearService().syncClientNTP(ntpPool);
                }
            } else {
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
                                doNTPSynchronization();
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
                .icon(Tools.changeDrawableColor(Objects.requireNonNull(getDrawable(R.drawable.ic_ntp_on)), ContextCompat.getColor(this.mainActivity.get(), R.color.colorPrimary)))
                .cancelable(false)
                .progress(true, 0)
                .show();

        }

        @Override
        protected String doInBackground(Void... voids) {
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
    public void doWearConnection(){

        if (getWearService().isConnected()){

            getWearService().disconnect();
            this.updateWearMenuItem();
            this.smartwatchFragment.refresh();
            this.captureFragment.refresh();

        }else {

            if (!getWearService().hasWearOS()) {
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
            } else if (DeviceTools.isBluetoothDisabled()) {
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
                                doWearConnection();
                            }
                        })
                        .show();
            } else {
                new WearConnectionTask(this).execute();
            }

        }
    }

    @SuppressLint("StaticFieldLeak")
    private class WearConnectionTask extends AsyncTask< Void, Void, WearService.ConnectionResponse>{

        private MaterialDialog dialog;
        private final WeakReference<MainActivity> mainActivity;

        WearConnectionTask(MainActivity mainActivity){
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
        protected WearService.ConnectionResponse doInBackground(Void... voids) {
            return getWearService().connect();
        }

        @Override
        protected void onPostExecute(WearService.ConnectionResponse connectionResponse) {
            super.onPostExecute(connectionResponse);
            switch (connectionResponse){
                case UNKNOWN_ERROR:
                    Toast.makeText(this.mainActivity.get(), getString(R.string.toast_wear_synchronization_unknown_error), Toast.LENGTH_LONG).show();
                    break;
                case TIMEOUT:
                    Toast.makeText(this.mainActivity.get(), getString(R.string.toast_wear_synchronization_no_capable_error), Toast.LENGTH_LONG).show();
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
            if (NTPTime.isInitialized()){
                NTPTime.close(this.mainActivity.get());
                updateNTPMenuItem();
                timeToolFragment.refresh();
            }
        }

    }

    private ServiceConnection createWearServiceConnection() {
        return new ServiceConnection() {
            @Override
            public void onServiceConnected(ComponentName name, IBinder service) {
                WearService.WearBinder wearBinder = (WearService.WearBinder) service;
                mWearService = wearBinder.getService();
                mWearServiceBounded = true;
            }

            @Override
            public void onServiceDisconnected(ComponentName name) {
                mWearServiceBounded = false;
            }
        };
    }

    public WearService getWearService() {
        return mWearService;
    }
}
