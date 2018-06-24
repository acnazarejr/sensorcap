package com.ssig.smartcap.activity;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.SharedPreferences;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Environment;
import android.os.IBinder;
import android.support.annotation.NonNull;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.view.menu.MenuBuilder;
import android.support.v7.widget.AppCompatButton;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.RadioGroup;
import android.widget.Switch;
import android.widget.Toast;

import com.afollestad.materialdialogs.DialogAction;
import com.afollestad.materialdialogs.GravityEnum;
import com.afollestad.materialdialogs.MaterialDialog;
import com.aurelhubert.ahbottomnavigation.AHBottomNavigation;
import com.aurelhubert.ahbottomnavigation.AHBottomNavigationAdapter;
import com.aurelhubert.ahbottomnavigation.AHBottomNavigationViewPager;
import com.google.android.gms.common.util.ArrayUtils;
import com.google.android.gms.wearable.CapabilityClient;
import com.google.android.gms.wearable.CapabilityInfo;
import com.google.android.gms.wearable.MessageClient;
import com.google.android.gms.wearable.MessageEvent;
import com.google.android.gms.wearable.Wearable;
import com.jaredrummler.materialspinner.MaterialSpinner;
import com.jaredrummler.materialspinner.MaterialSpinnerAdapter;
import com.karumi.dexter.Dexter;
import com.karumi.dexter.MultiplePermissionsReport;
import com.karumi.dexter.PermissionToken;
import com.karumi.dexter.listener.PermissionRequest;
import com.karumi.dexter.listener.multi.MultiplePermissionsListener;
import com.ssig.sensorsmanager.data.CaptureData;
import com.ssig.sensorsmanager.util.JSONUtil;
import com.ssig.smartcap.R;
import com.ssig.smartcap.adapter.ViewPagerAdapter;
import com.ssig.smartcap.fragment.AbstractMainFragment;
import com.ssig.smartcap.fragment.ArchiveFragment;
import com.ssig.smartcap.fragment.CaptureFragment;
import com.ssig.smartcap.fragment.SmartphoneFragment;
import com.ssig.smartcap.fragment.SmartwatchFragment;
import com.ssig.smartcap.fragment.TimeToolFragment;
import com.ssig.smartcap.model.CaptureListItem;
import com.ssig.smartcap.service.WearService;
import com.ssig.sensorsmanager.time.NTPTime;
import com.ssig.smartcap.utils.Tools;
import com.warkiz.widget.IndicatorSeekBar;

import java.io.File;
import java.io.FileNotFoundException;
import java.lang.ref.WeakReference;
import java.util.List;
import java.util.Objects;


public class MainActivity extends AppCompatActivity implements
        MessageClient.OnMessageReceivedListener,
        CapabilityClient.OnCapabilityChangedListener {

    private SharedPreferences sharedPreferences;
    private long exitTime = 0;

    private Toolbar toolbar;
    private MenuItem NTPMenuItem;
    private MenuItem wearMenuItem;
    private AHBottomNavigationViewPager ahBottomNavigationViewPager;
    private AHBottomNavigation ahBottomNavigation;

    private MaterialDialog dialogSettings;
    private MaterialDialog dialogAbout;

    private ViewPagerAdapter viewPagerAdapter;
    public AbstractMainFragment captureFragment;
    public AbstractMainFragment smartphoneFragment;
    public AbstractMainFragment smartwatchFragment;
    public AbstractMainFragment timeToolFragment;
    public AbstractMainFragment archiveFragment;

    private File systemCapturesFolder;
    private File systemArchiveFolder;

    private WearService wearService;
    private boolean wearServiceBounded;
    private ServiceConnection wearServiceConnection;

    // ---------------------------------------------------------------------------------------------
    // ACTIVITY STUFFS
    // ---------------------------------------------------------------------------------------------
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        this.setContentView(R.layout.activity_main);

        this.sharedPreferences = this.getPreferences(MODE_PRIVATE);
        this.wearServiceConnection = this.createWearServiceConnection();

        String systemFolderName = getString(R.string.system_folder_name);
        String captureFolderName = getString(R.string.capture_folder_name);
        String archiveFolderName = getString(R.string.archive_folder_name);
        this.systemCapturesFolder = new File(String.format("%s%s%s%s%s", Environment.getExternalStorageDirectory().getAbsolutePath(), File.separator, systemFolderName, File.separator, captureFolderName));
        this.systemArchiveFolder = new File(String.format("%s%s%s%s%s", Environment.getExternalStorageDirectory().getAbsolutePath(), File.separator, systemFolderName, File.separator, archiveFolderName));

        Intent wearServiceIntent = new Intent(this, WearService.class);
        bindService(wearServiceIntent, this.wearServiceConnection, Context.BIND_AUTO_CREATE);

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

        if (this.wearServiceBounded){
            unbindService(this.wearServiceConnection);
            this.wearServiceBounded = false;
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

    private void initUI() {
        this.initToolbar();
        this.initSettings();
        this.initAbout();
        this.initBottomNavigation();
        this.initPagerView();
        this.updateToolBarTitleAndIcon(0);
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
                            .title(R.string.permissions_dialog_failed_title)
                            .titleColorRes(R.color.colorAlert)
                            .content(R.string.permissions_dialog_failed_content)
                            .icon(Tools.changeDrawableColor(Objects.requireNonNull(MainActivity.this.getDrawable(R.drawable.ic_smartphone_lock)), ContextCompat.getColor(MainActivity.this, R.color.colorAlert)))
                            .cancelable(false)
                            .positiveText(R.string.button_try_again)
                            .onPositive(new MaterialDialog.SingleButtonCallback() {
                                @Override
                                public void onClick(@NonNull MaterialDialog dialog, @NonNull DialogAction which) {
                                    requestPermissions();
                                }
                            })
                            .negativeText(R.string.permissions_button_exit_app)
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
    // WEAR LISTENERS STUFFS
    // ---------------------------------------------------------------------------------------------
    @Override
    public void onMessageReceived(@NonNull MessageEvent messageEvent) {
        if (!this.isWearClientConnected())
            return;
        String path = messageEvent.getPath();
        if (path.equals(getString(R.string.message_path_host_activity_disconnect))){
            this.resetWearClientConnection(false);
        }
    }

    @Override
    public void onCapabilityChanged(@NonNull CapabilityInfo capabilityInfo) {
        if (capabilityInfo.getName().equals(getString(R.string.capability_smartcap_wear)) && capabilityInfo.getNodes().isEmpty())
            this.resetWearClientConnection(false);
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
        this.NTPMenuItem = menu.findItem(R.id.action_ntp);
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
                    doNTPSynchronization();
                    break;
                case R.id.action_wear:
                    doWearConnection();
                    break;
                case R.id.action_settings:
                    this.dialogSettings.show();
                    break;
                case R.id.action_clear:
                    this.clearData();
                    break;
                case R.id.action_about:
                    this.dialogAbout.show();
                    break;
            }

        }
        return super.onOptionsItemSelected(item);
    }

    public void updateNTPMenuItem(){
        boolean initialized = NTPTime.isSynchronized();
        if (initialized)
            this.NTPMenuItem.setIcon(R.drawable.ic_ntp_on);
        else
            this.NTPMenuItem.setIcon(R.drawable.ic_ntp_off);
        int color = ContextCompat.getColor(this, initialized ? R.color.colorAccent : R.color.colorAlert);
        Tools.changeDrawableColor(this.NTPMenuItem.getIcon(), color);
    }

    public void updateWearMenuItem(){
        if (this.isWearClientConnected())
            this.wearMenuItem.setIcon(R.drawable.ic_smartwatch_on);
        else
            this.wearMenuItem.setIcon(R.drawable.ic_smartwatch_off);
        int color = ContextCompat.getColor(this, this.isWearClientConnected() ? R.color.colorAccent : R.color.colorAlert);
        Tools.changeDrawableColor(this.wearMenuItem.getIcon(), color);
    }

    private void clearData(){

        new MaterialDialog.Builder(this)
                .title(R.string.dialog_clear_data_title)
                .titleColorRes(R.color.colorAlert)
                .content(R.string.dialog_clear_data_content)
                .icon(Tools.changeDrawableColor(Objects.requireNonNull(getDrawable(R.drawable.ic_delete)), ContextCompat.getColor(this, R.color.colorAlert)))
                .cancelable(true)
                .positiveText(R.string.button_yes)
                .onPositive(new MaterialDialog.SingleButtonCallback() {
                    @Override
                    public void onClick(@NonNull MaterialDialog dialog, @NonNull DialogAction which) {
                        if(isWearClientConnected())
                            getWearService().clearWearCaptures();
                        if(deleteRecursive(systemCapturesFolder) && deleteUnclosedArchivedCaptures())
                            Toast.makeText(MainActivity.this, R.string.dialog_clear_data_toast, Toast.LENGTH_LONG).show();
                        archiveFragment.refresh();
                    }
                })
                .negativeText(R.string.button_no)
                .show();

    }

    private boolean deleteRecursive(File fileOrDirectory) {
        if (!fileOrDirectory.exists())
            return false;
        if (fileOrDirectory.isDirectory())
            for (File child : fileOrDirectory.listFiles())
                deleteRecursive(child);
        return fileOrDirectory.delete();
    }

    private boolean deleteUnclosedArchivedCaptures(){
        boolean deleted = true;
        try {
            for(File captureFile : ((ArchiveFragment)this.archiveFragment).listCaptureFiles()){
                if(captureFile.exists()){
                    CaptureData captureData = JSONUtil.load(captureFile, CaptureData.class);
                    if (!captureData.isClosed())
                        deleted &= captureFile.delete();
                }
            }
        } catch (FileNotFoundException e) {
            e.printStackTrace();
            return false;
        }
        return deleted;
    }

    // ---------------------------------------------------------------------------------------------
    // Settings Stuffs
    // ---------------------------------------------------------------------------------------------
    public void initSettings(){
        this.dialogSettings =  new MaterialDialog.Builder(Objects.requireNonNull(this))
                .customView(R.layout.dialog_settings, true)
                .title(R.string.settings_title)
                .icon(Tools.changeDrawableColor(Objects.requireNonNull(getDrawable(R.drawable.ic_settings)), ContextCompat.getColor(this, R.color.colorPrimary)))
                .cancelable(true)
                .canceledOnTouchOutside(true)
                .positiveText(R.string.button_save)
                .onPositive(new MaterialDialog.SingleButtonCallback() {
                    @Override
                    public void onClick(@NonNull MaterialDialog dialog, @NonNull DialogAction which) {
                        SharedPreferences.Editor editor = sharedPreferences.edit();
                        View view = Objects.requireNonNull(dialog.getCustomView());

                        CharSequence[] ntpPoolOptions = getResources().getStringArray(R.array.ntp_pools);
                        MaterialSpinner inputNTPPoolServer = view.findViewById(R.id.ntp_pool_server_input);
                        editor.putString(getString(R.string.preference_main_default_key_ntp_pool), ntpPoolOptions[inputNTPPoolServer.getSelectedIndex()].toString().trim());

                        Switch switchSoundEnable = view.findViewById(R.id.enable_sound_switch);
                        editor.putBoolean(getString(R.string.preference_main_key_has_sound), switchSoundEnable.isChecked());

                        Switch switchVibrationEnable = view.findViewById(R.id.enable_vibration_switch);
                        editor.putBoolean(getString(R.string.preference_main_key_has_vibration), switchVibrationEnable.isChecked());

                        RadioGroup radioGroupCountdownStart = view.findViewById(R.id.countdown_start_radio_group);
                        int countdownStart = (radioGroupCountdownStart.getCheckedRadioButtonId() == R.id.countdown_start_3s_radio_option) ? 3 : 10;
                        editor.putInt(getString(R.string.preference_main_key_countdown_capture), countdownStart);

                        IndicatorSeekBar indicatorSeekBar = view.findViewById(R.id.timetool_qrcode_fps);
                        editor.putInt(getString(R.string.preference_main_key_qrcode_fps), indicatorSeekBar.getProgress());

                        editor.apply();
                    }
                }).neutralText(R.string.button_cancel)
                .onNeutral(new MaterialDialog.SingleButtonCallback() {
                    @Override
                    public void onClick(@NonNull MaterialDialog dialog, @NonNull DialogAction which) {
                        dialog.cancel();
                    }
                })
                .build();

        View view = Objects.requireNonNull(this.dialogSettings.getCustomView());

        CharSequence[] ntpPoolOptions = getResources().getStringArray(R.array.ntp_pools);
        MaterialSpinnerAdapter<CharSequence> spinnerAdapter = new MaterialSpinnerAdapter<>(this, ArrayUtils.toArrayList(ntpPoolOptions));

        MaterialSpinner inputNTPPoolServer = view.findViewById(R.id.ntp_pool_server_input);
        inputNTPPoolServer.setAdapter(spinnerAdapter);
        String currentNTPPool = this.sharedPreferences.getString(getString(R.string.preference_main_default_key_ntp_pool), getString(R.string.preference_main_default_ntp_pool));
        for(int i=0; i<ntpPoolOptions.length; i++){
            if(currentNTPPool.contentEquals(ntpPoolOptions[i])){
                inputNTPPoolServer.setSelectedIndex(i);
            }
        }


        Switch switchSoundEnable = view.findViewById(R.id.enable_sound_switch);
        switchSoundEnable.setChecked(this.sharedPreferences.getBoolean(getString(R.string.preference_main_key_has_sound), getResources().getBoolean(R.bool.preference_main_default_has_sound)));

        Switch switchSoundVibration = view.findViewById(R.id.enable_vibration_switch);
        switchSoundVibration.setChecked(this.sharedPreferences.getBoolean(getString(R.string.preference_main_key_has_vibration), getResources().getBoolean(R.bool.preference_main_default_has_vibration)));

        IndicatorSeekBar indicatorSeekBar = view.findViewById(R.id.timetool_qrcode_fps);
        indicatorSeekBar.setProgress(this.sharedPreferences.getInt(getString(R.string.preference_main_key_qrcode_fps), getResources().getInteger(R.integer.preference_main_default_qrcode_fps)));

        int countdownStart = this.sharedPreferences.getInt(getString(R.string.preference_main_key_countdown_capture), getResources().getInteger(R.integer.preference_main_default_countdown_capture));
        RadioGroup radioGroupCountdownStart = view.findViewById(R.id.countdown_start_radio_group);
        if(countdownStart == 3) {
            radioGroupCountdownStart.check(R.id.countdown_start_3s_radio_option);
        }else{
            radioGroupCountdownStart.check(R.id.countdown_start_3s_radio_option);
        }



    }

    // ---------------------------------------------------------------------------------------------
    // About Stuffs
    // ---------------------------------------------------------------------------------------------
    public void initAbout(){
        this.dialogAbout = new MaterialDialog.Builder(Objects.requireNonNull(this))
                .customView(R.layout.dialog_about, true)
                .btnStackedGravity(GravityEnum.CENTER)
                .canceledOnTouchOutside(true)
                .cancelable(true)
                .positiveText(R.string.button_ok)
                .build();

        View view = Objects.requireNonNull(this.dialogAbout.getCustomView());
        AppCompatButton buttonGetCode = view.findViewById(R.id.button_get_code);
        buttonGetCode.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent i = new Intent(Intent.ACTION_VIEW);
                i.setData(Uri.parse("https://github.com/acnazarejr/smartcap"));
                startActivity(i);
            }
        });
    }


    // ---------------------------------------------------------------------------------------------
    // BOTTOM NAVIGATION STUFFS
    // ---------------------------------------------------------------------------------------------
    private void initBottomNavigation(){

        this.ahBottomNavigation = findViewById(R.id.bottom_navigation);

        AHBottomNavigationAdapter bottomNavigationAdapter = new AHBottomNavigationAdapter(this, R.menu.menu_bottom_navigation);
        bottomNavigationAdapter.setupWithBottomNavigation(this.ahBottomNavigation);

        this.ahBottomNavigation.setTranslucentNavigationEnabled(true);
        this.ahBottomNavigation.setBehaviorTranslationEnabled(false);
        this.ahBottomNavigation.setTitleState(AHBottomNavigation.TitleState.SHOW_WHEN_ACTIVE);
        this.ahBottomNavigation.setForceTint(true);
        this.ahBottomNavigation.setAccentColor(ContextCompat.getColor(this, R.color.colorGreyLight));
        this.ahBottomNavigation.setInactiveColor(ContextCompat.getColor(this, R.color.colorPrimaryLight));
        this.ahBottomNavigation.setDefaultBackgroundColor(ContextCompat.getColor(this, R.color.colorPrimary));
        this.ahBottomNavigation.setOnTabSelectedListener(new AHBottomNavigation.OnTabSelectedListener() {
            @Override
            public boolean onTabSelected(int position, boolean wasSelected) {
                if (wasSelected) {
                    return true;
                }
                setCurrentFragment(position);
                return true;
            }
        });

        this.getWindow().setNavigationBarColor(ContextCompat.getColor(this, R.color.colorPrimaryDark));

    }

    // ---------------------------------------------------------------------------------------------
    // PAGE VIEWER STUFFS
    // ---------------------------------------------------------------------------------------------
    public void initPagerView(){
        this.ahBottomNavigationViewPager = findViewById(R.id.view_pager);

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

        this.ahBottomNavigationViewPager.setAdapter(viewPagerAdapter);
        this.ahBottomNavigationViewPager.setOffscreenPageLimit(5);

    }

    public void setCurrentFragment(int position){

        AbstractMainFragment currentFragment = this.viewPagerAdapter.getCurrentFragment();
        currentFragment.hide();

        this.updateToolBarTitleAndIcon(position);
        this.ahBottomNavigationViewPager.setCurrentItem(position, false);

        currentFragment = this.viewPagerAdapter.getCurrentFragment();
        currentFragment.show();
    }

    public void updateToolBarTitleAndIcon(int position){
        String fragmentTitle = this.ahBottomNavigation.getItem(position).getTitle(this);
        this.toolbar.setTitle(fragmentTitle);
        Drawable fragmentIcon;
        fragmentIcon = Objects.requireNonNull(this.ahBottomNavigation.getItem(position).getDrawable(this).getConstantState()).newDrawable().mutate();
        fragmentIcon = Tools.changeDrawableColor(fragmentIcon, ContextCompat.getColor(this, R.color.colorPrimary));
        this.toolbar.setNavigationIcon(fragmentIcon);
    }

    // ---------------------------------------------------------------------------------------------
    // NTP STUFFS
    // ---------------------------------------------------------------------------------------------
    public void doNTPSynchronization(){

        if (NTPTime.isSynchronized()){

            NTPTime.close(this);
            if (this.getWearService().isConnected()) {
                this.getWearService().closeClientNTP();
            }
            this.updateNTPMenuItem();
            this.timeToolFragment.refresh();

        }else {
            new NTPSynchronizationTask(this).execute();
        }
    }

    @SuppressLint("StaticFieldLeak")
    private class NTPSynchronizationTask extends AsyncTask<Void, Void, NTPTime.NTPSynchronizationResponse>{

        private String ntpPool;
        private MaterialDialog whileSynchronizationDialog;
        private final WeakReference<MainActivity> mainActivity;

        NTPSynchronizationTask(MainActivity mainActivity){
            this.mainActivity = new WeakReference<>(mainActivity);
        }

        MaterialDialog makeResponseErrorDialog(NTPTime.NTPSynchronizationResponse ntpSynchronizationResponse){

            MaterialDialog responseErrorDialog = new MaterialDialog.Builder(this.mainActivity.get())
                    .title("")
                    .titleColorRes(R.color.colorAlert)
                    .content("")
                    .icon(Tools.changeDrawableColor(Objects.requireNonNull(getDrawable(R.drawable.ic_ntp_off)), ContextCompat.getColor(this.mainActivity.get(), R.color.colorAlert)))
                    .cancelable(true)
                    .positiveText(R.string.button_try_again)
                    .onPositive(new MaterialDialog.SingleButtonCallback() {
                        @Override
                        public void onClick(@NonNull MaterialDialog dialog, @NonNull DialogAction which) {
                            doNTPSynchronization();
                        }
                    })
                    .neutralText(R.string.button_cancel)
                    .onNeutral(new MaterialDialog.SingleButtonCallback() {
                        @Override
                        public void onClick(@NonNull MaterialDialog dialog, @NonNull DialogAction which) {
                            dialog.cancel();
                        }
                    })
                    .build();

            switch (ntpSynchronizationResponse){

                case ALREADY_SYNCHRONIZED:
                    responseErrorDialog.setTitle(R.string.ntp_dialog_already_synchronized_error_title);
                    responseErrorDialog.setContent(R.string.ntp_dialog_already_synchronized_error_content);
                    break;

                case NETWORK_DISABLED:
                    responseErrorDialog.setIcon(Tools.changeDrawableColor(Objects.requireNonNull(getDrawable(R.drawable.ic_wifi_off)), ContextCompat.getColor(this.mainActivity.get(), R.color.colorAlert)));
                    responseErrorDialog.setTitle(R.string.ntp_dialog_network_error_title);
                    responseErrorDialog.setContent(R.string.ntp_dialog_network_error_content);
                    break;

                case NTP_TIMEOUT:
                    responseErrorDialog.setTitle(R.string.ntp_dialog_timeout_error_title);
                    responseErrorDialog.setContent(R.string.ntp_dialog_timeout_error_content);
                    break;

                case NTP_ERROR:
                    responseErrorDialog.setTitle(R.string.ntp_dialog_synchronization_error_title);
                    String lastExceptionMessage = NTPTime.getLastExceptionMessage();
                    String errorContent = String.format("%s %s", getString(R.string.ntp_dialog_synchronization_error_content_prefix), lastExceptionMessage != null ? lastExceptionMessage : "None");
                    responseErrorDialog.setContent(errorContent);
                    break;

                case UNKNOWN_ERROR:
                    responseErrorDialog.setTitle(R.string.ntp_dialog_unknown_error_title);
                    responseErrorDialog.setContent(R.string.ntp_dialog_unknown_error_content);
                    break;

            }
            return responseErrorDialog;
        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            this.ntpPool = sharedPreferences.getString(getString(R.string.preference_main_default_key_ntp_pool), getString(R.string.preference_main_default_ntp_pool));
            this.whileSynchronizationDialog = new MaterialDialog.Builder(this.mainActivity.get())
                .title(R.string.ntp_dialog_synchronization_while_title)
                .content(getString(R.string.ntp_dialog_synchronization_while_content) + " " + ntpPool)
                .icon(Tools.changeDrawableColor(Objects.requireNonNull(getDrawable(R.drawable.ic_ntp_on)), ContextCompat.getColor(this.mainActivity.get(), R.color.colorPrimary)))
                .cancelable(false)
                .progress(true, 0)
                .show();

        }

        @Override
        protected NTPTime.NTPSynchronizationResponse doInBackground(Void... voids) {
            return NTPTime.synchronize(mainActivity.get(), this.ntpPool);
        }

        @Override
        protected void onPostExecute(NTPTime.NTPSynchronizationResponse ntpSynchronizationResponse) {
            super.onPostExecute(ntpSynchronizationResponse);

            this.whileSynchronizationDialog.dismiss();
            this.mainActivity.get().updateNTPMenuItem();


            if (ntpSynchronizationResponse == NTPTime.NTPSynchronizationResponse.SUCCESS) {
                timeToolFragment.refresh();
                if (isWearClientConnected()) {
                    getWearService().syncClientNTP(this.ntpPool);
                }
                Toast.makeText(this.mainActivity.get(), getString(R.string.ntp_toast_synchronization_success), Toast.LENGTH_LONG).show();
            } else {
                MaterialDialog responseErrorDialog = this.makeResponseErrorDialog(ntpSynchronizationResponse);
                responseErrorDialog.show();
            }

        }
    }

    // ---------------------------------------------------------------------------------------------
    // WEAR STUFFS
    // ---------------------------------------------------------------------------------------------
    public void doWearConnection(){
        if (getWearService().isConnected()){
            this.resetWearClientConnection(true);
        }else {
            new WearConnectionTask(this).execute();
        }
    }

    @SuppressLint("StaticFieldLeak")
    private class WearConnectionTask extends AsyncTask< Void, Void, WearService.WearConnectionResponse>{

        private MaterialDialog whileConnectionDialog;
        private final WeakReference<MainActivity> mainActivity;

        WearConnectionTask(MainActivity mainActivity){
            this.mainActivity = new WeakReference<>(mainActivity);
        }

        MaterialDialog makeResponseErrorDialog(WearService.WearConnectionResponse wearConnectionResponse){


            if (wearConnectionResponse == WearService.WearConnectionResponse.NO_WEAR_APP){
                return new MaterialDialog.Builder(this.mainActivity.get())
                        .title(R.string.wear_dialog_wearos_error_title)
                        .titleColorRes(R.color.colorAlert)
                        .content(R.string.wear_dialog_wearos_error_content)
                        .icon(Tools.changeDrawableColor(Objects.requireNonNull(getDrawable(R.drawable.ic_wear_os_color)), ContextCompat.getColor(this.mainActivity.get(), R.color.colorAlert)))
                        .cancelable(true)
                        .positiveText(R.string.wear_dialog_wearos_error_button)
                        .onPositive(new MaterialDialog.SingleButtonCallback() {
                            @Override
                            public void onClick(@NonNull MaterialDialog dialog, @NonNull DialogAction which) {
                                Intent goToMarket = new Intent(Intent.ACTION_VIEW).setData(Uri.parse(String.format("market://details?id=%s", getString(R.string.util_wear_package))));
                                startActivity(goToMarket);
                            }
                        })
                        .neutralText(R.string.button_cancel)
                        .onNeutral(new MaterialDialog.SingleButtonCallback() {
                            @Override
                            public void onClick(@NonNull MaterialDialog dialog, @NonNull DialogAction which) {
                                dialog.cancel();
                            }
                        })
                        .build();
            }


            MaterialDialog responseErrorDialog = new MaterialDialog.Builder(this.mainActivity.get())
                    .title("")
                    .titleColorRes(R.color.colorAlert)
                    .content("")
                    .icon(Tools.changeDrawableColor(Objects.requireNonNull(getDrawable(R.drawable.ic_smartwatch_off)), ContextCompat.getColor(this.mainActivity.get(), R.color.colorAlert)))
                    .cancelable(true)
                    .positiveText(R.string.button_try_again)
                    .onPositive(new MaterialDialog.SingleButtonCallback() {
                        @Override
                        public void onClick(@NonNull MaterialDialog dialog, @NonNull DialogAction which) {
                            doWearConnection();
                        }
                    })
                    .neutralText(R.string.button_cancel)
                    .onNeutral(new MaterialDialog.SingleButtonCallback() {
                        @Override
                        public void onClick(@NonNull MaterialDialog dialog, @NonNull DialogAction which) {
                            dialog.cancel();
                        }
                    })
                    .build();

            switch (wearConnectionResponse){

                case ALREADY_CONNECTED:
                    responseErrorDialog.setTitle(R.string.wear_dialog_already_connected_error_title);
                    responseErrorDialog.setContent(R.string.wear_dialog_already_connected_error_content);
                    break;

                case BLUETOOTH_DISABLED:
                    responseErrorDialog.setIcon(Tools.changeDrawableColor(Objects.requireNonNull(getDrawable(R.drawable.ic_bluetooth_off)), ContextCompat.getColor(this.mainActivity.get(), R.color.colorAlert)));
                    responseErrorDialog.setTitle(R.string.wear_dialog_bluetooth_error_title);
                    responseErrorDialog.setContent(R.string.wear_dialog_bluetooth_error_content);
                    break;

                case NO_PAIRED_DEVICES:
                    responseErrorDialog.setTitle(R.string.wear_dialog_no_paired_error_title);
                    responseErrorDialog.setContent(R.string.wear_dialog_no_paired_error_content);
                    break;

                case NO_CAPABLE_DEVICES:
                    responseErrorDialog.setIcon(Tools.changeDrawableColor(Objects.requireNonNull(getDrawable(R.drawable.logo)), ContextCompat.getColor(this.mainActivity.get(), R.color.colorAlert)));
                    responseErrorDialog.setTitle(R.string.wear_dialog_no_capable_error_title);
                    responseErrorDialog.setContent(R.string.wear_dialog_no_capable_error_content);
                    break;

                case DEVICE_INFO_ERROR:
                    responseErrorDialog.setTitle(R.string.wear_dialog_no_deviceinfo_error_title);
                    responseErrorDialog.setContent(R.string.wear_dialog_no_deviceinfo_error_content);
                    break;

                case TIMEOUT:
                    responseErrorDialog.setTitle(R.string.wear_dialog_timeout_error_title);
                    responseErrorDialog.setContent(R.string.wear_dialog_timeout_error_content);
                    break;

                case UNKNOWN_ERROR:
                    responseErrorDialog.setTitle(R.string.wear_dialog_unknown_error_title);

                    WearService wearService = getWearService();
                    String lastExceptionMessage = null;
                    if (wearService != null) {
                        lastExceptionMessage = wearService.getLastExceptionMessage();
                    }
                    String errorContent = String.format("%s %s", getString(R.string.wear_dialog_unknown_error_prefix_content), lastExceptionMessage != null ? lastExceptionMessage : "None");
                    responseErrorDialog.setContent(errorContent);

                    break;

            }
            return responseErrorDialog;
        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            this.whileConnectionDialog = new MaterialDialog.Builder(this.mainActivity.get())
                    .title(R.string.wear_dialog_connection_while_title)
                    .content(R.string.wear_dialog_connection_while_content)
                    .icon(Tools.changeDrawableColor(Objects.requireNonNull(getDrawable(R.drawable.ic_smartwatch)), ContextCompat.getColor(this.mainActivity.get(), R.color.colorPrimary)))
                    .cancelable(false)
                    .progress(true, 0)
                    .show();
        }

        @Override
        protected WearService.WearConnectionResponse doInBackground(Void... voids) {
            return getWearService().connect();
        }

        @Override
        protected void onPostExecute(WearService.WearConnectionResponse wearConnectionResponse) {
            super.onPostExecute(wearConnectionResponse);

            this.whileConnectionDialog.dismiss();
            this.mainActivity.get().updateWearMenuItem();

            if (wearConnectionResponse == WearService.WearConnectionResponse.SUCCESS) {
                smartwatchFragment.refresh();
                captureFragment.refresh();
                if (NTPTime.isSynchronized()){
                    NTPTime.close(this.mainActivity.get());
                    updateNTPMenuItem();
                    timeToolFragment.refresh();
                }
                Toast.makeText(this.mainActivity.get(), getString(R.string.wear_toast_connection_success), Toast.LENGTH_LONG).show();
            }else{
                MaterialDialog responseErrorDialog = this.makeResponseErrorDialog(wearConnectionResponse);
                responseErrorDialog.show();
            }

//            switch (wearConnectionResponse){
//                case UNKNOWN_ERROR:
//                    Toast.makeText(this.mainActivity.get(), getString(R.string.toast_wear_synchronization_unknown_error), Toast.LENGTH_LONG).show();
//                    break;
//                case TIMEOUT:
//                    Toast.makeText(this.mainActivity.get(), getString(R.string.toast_wear_synchronization_no_capable_error), Toast.LENGTH_LONG).show();
//                    break;
//                case NO_WEAR_APP:
//                    Toast.makeText(this.mainActivity.get(), getString(R.string.toast_wear_synchronization_unknown_error), Toast.LENGTH_LONG).show();
//                    break;
//                case BLUETOOTH_DISABLED:
//                    Toast.makeText(this.mainActivity.get(), getString(R.string.toast_wear_synchronization_bluetooth_error), Toast.LENGTH_LONG).show();
//                    break;
//                case NO_PAIRED_DEVICES:
//                    Toast.makeText(this.mainActivity.get(), getString(R.string.toast_wear_synchronization_no_paired_error), Toast.LENGTH_LONG).show();
//                    break;
//                case NO_CAPABLE_DEVICES:
//                    Toast.makeText(this.mainActivity.get(), getString(R.string.toast_wear_synchronization_no_capable_error), Toast.LENGTH_LONG).show();
//                    break;
//                case SUCCESS:
//                    break;
//            }

//            this.mainActivity.get().updateWearMenuItem();
//            smartwatchFragment.refresh();
//            captureFragment.refresh();
//            if (NTPTime.isSynchronized()){
//                NTPTime.close(this.mainActivity.get());
//                updateNTPMenuItem();
//                timeToolFragment.refresh();
//            }
        }

    }

    private ServiceConnection createWearServiceConnection() {
        return new ServiceConnection() {
            @Override
            public void onServiceConnected(ComponentName name, IBinder service) {
                WearService.WearBinder wearBinder = (WearService.WearBinder) service;
                wearService = wearBinder.getService();
                wearServiceBounded = true;
            }

            @Override
            public void onServiceDisconnected(ComponentName name) {
                wearServiceBounded = false;
            }
        };
    }

    public WearService getWearService() {
        return wearService;
    }

    private boolean isWearClientConnected(){
        return this.getWearService() != null && this.getWearService().isConnected();
    }

    private void resetWearClientConnection(boolean sendDisconnectMessageToClient){
        this.getWearService().disconnect(sendDisconnectMessageToClient);
        this.updateWearMenuItem();
        this.smartwatchFragment.refresh();
        this.captureFragment.refresh();
    }
}
