package com.ssig.smartcap.activity;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.Context;
import android.content.pm.PackageManager;
import android.content.res.ColorStateList;
import android.net.Uri;
import android.os.Bundle;
import android.os.SystemClock;
import android.os.Vibrator;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.wearable.activity.WearableActivity;
import android.view.View;
import android.widget.Chronometer;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.android.gms.wearable.Asset;
import com.google.android.gms.wearable.CapabilityClient;
import com.google.android.gms.wearable.CapabilityInfo;
import com.google.android.gms.wearable.DataItem;
import com.google.android.gms.wearable.MessageClient;
import com.google.android.gms.wearable.MessageEvent;
import com.google.android.gms.wearable.PutDataMapRequest;
import com.google.android.gms.wearable.PutDataRequest;
import com.google.android.gms.wearable.Wearable;
import com.ssig.sensorsmanager.SensorType;
import com.ssig.sensorsmanager.capture.CaptureRunner;
import com.ssig.sensorsmanager.config.CaptureConfig;
import com.ssig.sensorsmanager.time.NTPTime;
import com.ssig.sensorsmanager.time.SystemTime;
import com.ssig.smartcap.R;
import com.ssig.smartcap.common.CountDownAnimation;
import com.ssig.smartcap.common.Serialization;
import com.ssig.smartcap.service.HostListenerService;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Map;
import java.util.Objects;
import java.util.Timer;
import java.util.TimerTask;

public class MainActivity extends WearableActivity  implements
        CapabilityClient.OnCapabilityChangedListener,
        MessageClient.OnMessageReceivedListener,
        ActivityCompat.OnRequestPermissionsResultCallback{

    private final static int PERMISSION_REQUEST_WRITE_EXTERNAL_STORAGE = 1;

    private enum CaptureState{
        IDLE, CAPTURING
    }

    private ImageView mImageHostConnectedIcon;
    private ImageView mImageNTPSynchronizedIcon;
    private View mLayoutContent;
    private View mLayoutLogo;
    private ImageView mImageDeviceTimeIcon;
    private TextView mTextDeviceTime;
    private ImageView mImageNTPTimeIcon;
    private TextView mTextNTPTime;
    private ImageButton mButtonControlCapture;
    private View mLayoutCountdown;
    private TextView mTextCountdown;
    private Chronometer mChronometer;

    private SimpleDateFormat mSimpleDateFormat;
    private Timer mUpdateTimer;
    private TimerTask mUpdateTimerTask;
    private SystemTime mSystemTime;
    private NTPTime mNtpTime;

    private CaptureRunner captureRunner;
    private CaptureState captureState;


    @SuppressLint("SimpleDateFormat")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        setAmbientEnabled();
        this.mSimpleDateFormat = new SimpleDateFormat(getString(R.string.util_time_format));
        this.mUpdateTimer = null;
        this.mUpdateTimerTask = null;
        this.mSystemTime = new SystemTime();
        this.mNtpTime = new NTPTime();
        this.captureState = CaptureState.IDLE;
        this.captureRunner = null;
        final String uri = String.format("wear://*%s", getString(R.string.message_path_client_activity_prefix));
        Wearable.getMessageClient(this).addListener(this, Uri.parse(uri), MessageClient.FILTER_PREFIX);
        this.initUI();
        this.registerListeners();
    }


    @Override
    protected void onStart() {
        super.onStart();
        this.checkPermissions();
    }

    @Override
    protected void onResume() {
        super.onResume();
        Wearable.getCapabilityClient(this).addListener(this, Uri.parse("wear://"), CapabilityClient.FILTER_REACHABLE);
        this.updateStatusIcons();
        this.updateStateButtonControlCapture();
        this.startTimer();
    }

    @Override
    protected void onPause() {
        super.onPause();
        this.stopTimer();
        Wearable.getCapabilityClient(this).removeListener(this);
    }

    @Override
    protected void onDestroy() {
        Wearable.getMessageClient(this).removeListener(this);
        NTPTime.close(this);
        HostListenerService.disconnect();
        super.onDestroy();
    }

    @Override
    public void onCapabilityChanged(@NonNull CapabilityInfo capabilityInfo) {
        if (capabilityInfo.getName().equals(getString(R.string.capability_smartcap_capture))){
            updateStateButtonControlCapture(!capabilityInfo.getNodes().isEmpty());
        }
    }

    @Override
    public void onMessageReceived(@NonNull MessageEvent messageEvent) {
        String path = messageEvent.getPath();
        if (path.equals(getString(R.string.message_path_client_activity_start_capture))){
            byte[] data = messageEvent.getData();
            CaptureConfig captureConfig = Serialization.deserializeObject(data);
            this.startCapture(captureConfig);
        }
        if (path.equals(getString(R.string.message_path_client_activity_stop_capture))){
            this.stopCapture();
        }
    }

    private void checkPermissions(){
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED){
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, PERMISSION_REQUEST_WRITE_EXTERNAL_STORAGE);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        if (requestCode == PERMISSION_REQUEST_WRITE_EXTERNAL_STORAGE){
            if (!((grantResults.length == 1) && (grantResults[0] == PackageManager.PERMISSION_GRANTED))) {
                this.checkPermissions();
            }
        }
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
    }

    private void initUI(){
        this.mImageHostConnectedIcon = findViewById(R.id.image_host_connected);
        this.mImageNTPSynchronizedIcon = findViewById(R.id.image_ntp_synchronized);
        this.mLayoutContent = findViewById(R.id.layout_content);
        this.mImageDeviceTimeIcon = findViewById(R.id.image_device_time);
        this.mTextDeviceTime = findViewById(R.id.text_device_time);
        this.mImageNTPTimeIcon = findViewById(R.id.image_ntp_time);
        this.mTextNTPTime = findViewById(R.id.text_ntp_time);
        this.mButtonControlCapture = findViewById(R.id.button_control_capture);
        this.mTextCountdown = findViewById(R.id.text_countdown);

        this.mLayoutLogo = findViewById(R.id.layout_logo);
        this.mLayoutLogo.setVisibility(View.VISIBLE);

        this.mLayoutCountdown = findViewById(R.id.layout_countdown);
        this.mLayoutCountdown.setVisibility(View.GONE);

        this.mChronometer = findViewById(R.id.chronometer);
        this.mChronometer.setVisibility(View.GONE);
    }

    private TimerTask createUpdateTimerTask(){

        return new TimerTask() {
            @Override
            public void run() {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        Long unixTimestampDevice = mSystemTime.now();
                        Long unixTimestampNTP = mNtpTime.now();
                        Date dateTimestampDevice = new Date(unixTimestampDevice);
                        mTextDeviceTime.setText(mSimpleDateFormat.format(dateTimestampDevice));
                        mTextNTPTime.setText(unixTimestampNTP != null ? mSimpleDateFormat.format(new Date(unixTimestampNTP)) : getString(R.string.time_tool_dummy_hour));
                    }
                });
            }
        };
    }

    private void updateStatusIcons(){
        this.mImageHostConnectedIcon.setImageResource(HostListenerService.connectedOnHost ? R.drawable.ic_smartphone_on : R.drawable.ic_smartphone_off);
        this.mImageHostConnectedIcon.setColorFilter(ContextCompat.getColor(this, HostListenerService.connectedOnHost ? R.color.colorAccent : R.color.colorAlert));
        this.mImageNTPSynchronizedIcon.setImageResource(NTPTime.isInitialized() ? R.drawable.ic_ntp_on : R.drawable.ic_ntp_off);
        this.mImageNTPSynchronizedIcon.setColorFilter(ContextCompat.getColor(this, NTPTime.isInitialized() ? R.color.colorAccent : R.color.colorAlert));
    }

    private void updateStateButtonControlCapture(){
        Task<CapabilityInfo> capabilityInfoTask = Wearable.getCapabilityClient(this).getCapability(getString(R.string.capability_smartcap_capture), CapabilityClient.FILTER_REACHABLE);
        capabilityInfoTask.addOnCompleteListener(new OnCompleteListener<CapabilityInfo>() {
            @Override
            public void onComplete(@NonNull Task<CapabilityInfo> task) {
                CapabilityInfo capabilityInfo = task.getResult();
                updateStateButtonControlCapture(!capabilityInfo.getNodes().isEmpty());
            }
        });
    }

    private void updateStateButtonControlCapture(boolean hostHasCaptureCapability){
        if (HostListenerService.connectedOnHost && hostHasCaptureCapability){
            this.mButtonControlCapture.setBackgroundTintList(ColorStateList.valueOf(ContextCompat.getColor(this, R.color.colorAccent)));
            this.mButtonControlCapture.setEnabled(true);
        }else{
            this.mButtonControlCapture.setBackgroundTintList(ColorStateList.valueOf(ContextCompat.getColor(this, R.color.colorDisabled)));
            this.mButtonControlCapture.setEnabled(false);
        }
        this.mButtonControlCapture.setImageResource(this.captureState == CaptureState.IDLE ? R.drawable.ic_play : R.drawable.ic_stop);
    }

    private void stopTimer(){
        if (this.mUpdateTimer != null){
            this.mUpdateTimer.cancel();
            this.mUpdateTimer.purge();
            this.mUpdateTimerTask = null;
            this.mUpdateTimer = null;
        }
    }

    private void startTimer() {
        mUpdateTimer = new Timer();
        mUpdateTimerTask = createUpdateTimerTask();
        mUpdateTimer.scheduleAtFixedRate(mUpdateTimerTask, 0, 20);
    }

    private void startCapture(CaptureConfig captureConfig){

        if (captureConfig.isSmartwatchEnabled()) {
            try {
                String captureAlias = String.format("smartwatch_%s", captureConfig.getAlias());
                this.captureRunner = new CaptureRunner(Objects.requireNonNull(this), captureConfig.getSmartwatchSensors(), captureConfig.getAppFolderName(), captureAlias);
            } catch (IOException e) {
                Toast.makeText(this, e.getMessage(), Toast.LENGTH_LONG).show();
                return;
            }
        }


        CountDownAnimation countDownAnimation = new CountDownAnimation(this, this.mTextCountdown, captureConfig.getCountdownStart(), captureConfig.hasSound(), captureConfig.hasVibration());
        countDownAnimation.setCountDownListener(new CountDownAnimation.CountDownListener() {
            @Override
            public void onCountDownEnd(CountDownAnimation animation) {
                mLayoutCountdown.setVisibility(View.GONE);
                mLayoutLogo.setVisibility(View.GONE);
                mChronometer.setBase(SystemClock.elapsedRealtime());
                mChronometer.start();
                mChronometer.setVisibility(View.VISIBLE);
                if (captureRunner != null)
                    captureRunner.start();
                updateStateButtonControlCapture(true);
            }
        });


        this.mLayoutCountdown.setVisibility(View.VISIBLE);
        this.captureState = CaptureState.CAPTURING;
        countDownAnimation.start();
    }

    private void stopCapture(){
        if (this.captureRunner != null){
            try {
                final File captureCompressedFile = captureRunner.finish();
                Asset asset = Asset.createFromUri(Uri.fromFile(captureCompressedFile));

                PutDataMapRequest putDataMapRequest = PutDataMapRequest.create(getString(R.string.message_path_host_capture_fragment_sensor_files));
                putDataMapRequest.getDataMap().putAsset(getString(R.string.asset_sensors_smartwatch), asset);
                PutDataRequest putDataRequest = putDataMapRequest.asPutDataRequest();
                putDataRequest.setUrgent();

                Task<DataItem> dataItemTask = Wearable.getDataClient(this).putDataItem(putDataRequest);
                dataItemTask.addOnSuccessListener(new OnSuccessListener<DataItem>() {
                    @Override
                    public void onSuccess(DataItem dataItem) {
                        Toast.makeText(MainActivity.this, "Sensor files sent", Toast.LENGTH_LONG).show();
                        captureCompressedFile.delete();
                    }
                });
            } catch (IOException e) {
                Toast.makeText(this, e.getMessage(), Toast.LENGTH_LONG).show();
                return;
            }
        }
        this.mChronometer.stop();
        this.mChronometer.setVisibility(View.GONE);
        this.captureState = CaptureState.IDLE;
        this.captureRunner = null;
        mLayoutLogo.setVisibility(View.VISIBLE);
        updateStateButtonControlCapture(true);
    }

    private void registerListeners(){
        this.mButtonControlCapture.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Toast.makeText(MainActivity.this, R.string.toast_press_hold, Toast.LENGTH_SHORT).show();
            }
        });
        this.mButtonControlCapture.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                if (captureState == CaptureState.IDLE)
                    HostListenerService.startHostCapture();
                else if (captureState == CaptureState.CAPTURING)
                    HostListenerService.stopHostCapture();
                return true;
            }
        });
    }

}
