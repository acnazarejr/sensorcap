package com.ssig.smartcap.activity;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.pm.PackageManager;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.SystemClock;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.wearable.activity.WearableActivity;
import android.support.wearable.input.WearableButtons;
import android.view.KeyEvent;
import android.view.View;
import android.widget.Chronometer;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.android.gms.wearable.Asset;
import com.google.android.gms.wearable.DataItem;
import com.google.android.gms.wearable.MessageClient;
import com.google.android.gms.wearable.MessageEvent;
import com.google.android.gms.wearable.PutDataMapRequest;
import com.google.android.gms.wearable.PutDataRequest;
import com.google.android.gms.wearable.Wearable;
import com.ssig.sensorsmanager.capture.CaptureRunner;
import com.ssig.sensorsmanager.config.CaptureConfig;
import com.ssig.sensorsmanager.time.NTPTime;
import com.ssig.smartcap.R;
import com.ssig.smartcap.common.CountDownAnimation;
import com.ssig.smartcap.common.Serialization;
import com.ssig.smartcap.service.HostListenerService;

import java.io.File;
import java.io.IOException;
import java.lang.ref.WeakReference;
import java.util.Objects;

public class MainActivity extends WearableActivity  implements
        MessageClient.OnMessageReceivedListener,
        ActivityCompat.OnRequestPermissionsResultCallback{

    private final static int PERMISSION_REQUEST_WRITE_EXTERNAL_STORAGE = 1;

    private enum DeviceState{
        IDLE, WAITING_CONNECTION, CAPTURING, SENDING
    }

    private ImageView imageHostConnectedStatusIcon;
    private ImageView imageNTPSynchronizedStatusIcon;
    private View layoutLogo;
    private View layoutCountdown;
    private Chronometer chronometer;
    private TextView textCountdown;
    private TextView textStatus;
    private ImageView imageKeyPrimary;
    private ImageView imageKeyOne;


    private CaptureRunner captureRunner;
    private DeviceState deviceState;


    //----------------------------------------------------------------------------------------------
    // Override Functions
    //----------------------------------------------------------------------------------------------
    @SuppressLint("SimpleDateFormat")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        setAmbientEnabled();
        this.captureRunner = null;
        final String uri = String.format("wear://*%s", getString(R.string.message_path_client_activity_prefix));
        Wearable.getMessageClient(this).addListener(this, Uri.parse(uri), MessageClient.FILTER_PREFIX);
        this.initUI();
    }

    @Override
    protected void onStart() {
        super.onStart();
        this.checkPermissions();
    }

    @Override
    protected void onResume() {
        super.onResume();
        this.setState(HostListenerService.connectedOnHost ? DeviceState.IDLE : DeviceState.WAITING_CONNECTION);
        this.updateStatusIcons();
        this.updateStatus();
    }

    @Override
    protected void onStop() {
        super.onStop();
        finish();
    }

    @Override
    protected void onDestroy() {
        Wearable.getMessageClient(this).removeListener(this);
        NTPTime.close(this);
        HostListenerService.disconnect();
        super.onDestroy();
    }

    //----------------------------------------------------------------------------------------------
    // Message Received
    //----------------------------------------------------------------------------------------------
    @Override
    public void onMessageReceived(@NonNull MessageEvent messageEvent) {
        String path = messageEvent.getPath();

        if (path.equals(getString(R.string.message_path_client_activity_sync_ntp))){
            byte[] data = messageEvent.getData();
            String ntpPool = Serialization.deserializeObject(data);
            new NTPSynchronizationTask(this).execute(ntpPool);
        }

        if (path.equals(getString(R.string.message_path_client_activity_close_ntp))){
            NTPTime.close(this);
            this.updateStatusIcons();
        }

        if (path.equals(getString(R.string.message_path_client_activity_start_capture))){
            byte[] data = messageEvent.getData();
            CaptureConfig captureConfig = Serialization.deserializeObject(data);
            this.startCapture(captureConfig);
        }
        if (path.equals(getString(R.string.message_path_client_activity_stop_capture))){
            this.stopCapture();
        }
    }

    //----------------------------------------------------------------------------------------------
    // Permission STUFFS
    //----------------------------------------------------------------------------------------------
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

    //----------------------------------------------------------------------------------------------
    // UI STUFFS
    //----------------------------------------------------------------------------------------------
    private void initUI(){
        this.imageHostConnectedStatusIcon = findViewById(R.id.host_connected_status_icon_image);
        this.imageNTPSynchronizedStatusIcon = findViewById(R.id.ntp_synchronized_status_icon_image);

        this.layoutLogo = findViewById(R.id.logo_layout);
        this.layoutLogo.setVisibility(View.VISIBLE);

        this.layoutCountdown = findViewById(R.id.countdown_layout);
        this.textCountdown = findViewById(R.id.countdown_text);
        this.layoutCountdown.setVisibility(View.GONE);

        this.chronometer = findViewById(R.id.chronometer);
        this.chronometer.setVisibility(View.GONE);

        this.textStatus = findViewById(R.id.status_text);

        View imageKeyPrimaryLayout = findViewById(R.id.key_primary_image_layout);
        imageKeyPrimaryLayout.setVisibility(View.GONE);
        View imageKeyOneLayout = findViewById(R.id.key_one_image_layout);
        imageKeyOneLayout.setVisibility(View.GONE);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {

            WearableButtons.ButtonInfo buttonPrimaryInfo = WearableButtons.getButtonInfo(this, KeyEvent.KEYCODE_STEM_PRIMARY);
            WearableButtons.ButtonInfo buttonOneInfo = WearableButtons.getButtonInfo(this, KeyEvent.KEYCODE_STEM_1);

            if(buttonPrimaryInfo != null){
                imageKeyPrimaryLayout.setVisibility(View.VISIBLE);
                ImageView imageKeyPrimary = findViewById(R.id.key_primary_image);
                imageKeyPrimary.setImageDrawable(WearableButtons.getButtonIcon(this, KeyEvent.KEYCODE_STEM_PRIMARY));
            }

            if(buttonOneInfo != null){
                imageKeyOneLayout.setVisibility(View.VISIBLE);
                ImageView imageKeyOne = findViewById(R.id.key_one_image);
                imageKeyOne.setImageDrawable(WearableButtons.getButtonIcon(this, KeyEvent.KEYCODE_STEM_1));
            }

        }


    }

    private void updateStatusIcons(){
        this.imageHostConnectedStatusIcon.setImageResource(HostListenerService.connectedOnHost ? R.drawable.ic_smartphone_on : R.drawable.ic_smartphone_off);
        this.imageHostConnectedStatusIcon.setColorFilter(ContextCompat.getColor(this, HostListenerService.connectedOnHost ? R.color.colorAccent : R.color.colorAlert));
        this.imageNTPSynchronizedStatusIcon.setImageResource(NTPTime.isSynchronized() ? R.drawable.ic_ntp_on : R.drawable.ic_ntp_off);
        this.imageNTPSynchronizedStatusIcon.setColorFilter(ContextCompat.getColor(this, NTPTime.isSynchronized() ? R.color.colorAccent : R.color.colorAlert));
    }

    private void updateStatus(){
        switch(this.deviceState){
            case WAITING_CONNECTION:
                this.textStatus.setText(R.string.waiting_connection);
                break;
            case IDLE:
                this.textStatus.setText(R.string.ready_to_capture);
                break;
            case CAPTURING:
                this.textStatus.setText(R.string.capturing);
                break;
            case SENDING:
                this.textStatus.setText(R.string.sending_files);
                break;
        }
    }

    private void setState(DeviceState deviceState){
        this.deviceState = deviceState;
        this.updateStatus();
    }

    //----------------------------------------------------------------------------------------------
    // Hardware Button STUFFS
    //----------------------------------------------------------------------------------------------
    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {

        if (this.deviceState == DeviceState.CAPTURING)
            HostListenerService.stopHostCapture();

        return super.onKeyDown(keyCode, event);
    }

    //----------------------------------------------------------------------------------------------
    // Capture STUFFS
    //----------------------------------------------------------------------------------------------
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

        CountDownAnimation countDownAnimation = new CountDownAnimation(this, this.textCountdown, captureConfig.getCountdownStart(), captureConfig.hasSound(), captureConfig.hasVibration());
        countDownAnimation.setCountDownListener(new CountDownAnimation.CountDownListener() {
            @Override
            public void onCountDownEnd(CountDownAnimation animation) {
                layoutCountdown.setVisibility(View.GONE);
                layoutLogo.setVisibility(View.GONE);
                chronometer.setBase(SystemClock.elapsedRealtime());
                chronometer.start();
                chronometer.setVisibility(View.VISIBLE);
                if (captureRunner != null)
                    captureRunner.start();
            }
        });

        this.layoutCountdown.setVisibility(View.VISIBLE);
        this.setState(DeviceState.CAPTURING);
        countDownAnimation.start();
    }

    private void stopCapture(){

        this.chronometer.stop();
        this.chronometer.setVisibility(View.GONE);
        this.layoutLogo.setVisibility(View.VISIBLE);
        this.setState(DeviceState.SENDING);

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
                        captureCompressedFile.delete();
                        setState(HostListenerService.connectedOnHost ? DeviceState.IDLE : DeviceState.WAITING_CONNECTION);
                        Toast.makeText(MainActivity.this, "Sensor files sent", Toast.LENGTH_LONG).show();

                    }
                });
            } catch (IOException e) {
                Toast.makeText(this, e.getMessage(), Toast.LENGTH_LONG).show();
                return;
            }
        }

        this.captureRunner = null;
    }

    //----------------------------------------------------------------------------------------------
    // NTP STUFFS
    //----------------------------------------------------------------------------------------------
    @SuppressLint("StaticFieldLeak")
    private class NTPSynchronizationTask extends AsyncTask<String, Void, NTPTime.NTPSynchronizationResponse> {

        private final WeakReference<MainActivity> mainActivity;

        NTPSynchronizationTask(MainActivity mainActivity){
            this.mainActivity = new WeakReference<>(mainActivity);
        }


        @Override
        protected NTPTime.NTPSynchronizationResponse doInBackground(String... strings) {
            if (strings.length == 0)
                return NTPTime.NTPSynchronizationResponse.UNKNOWN_ERROR;
            return NTPTime.synchronize(mainActivity.get(), strings[0]);
        }

        @Override
        protected void onPostExecute(NTPTime.NTPSynchronizationResponse ntpSynchronizationResponse) {
            super.onPostExecute(ntpSynchronizationResponse);

            this.mainActivity.get().updateStatusIcons();

            String responseMessage = "";
            switch (ntpSynchronizationResponse){

                case ALREADY_SYNCHRONIZED:
                    responseMessage = getString(R.string.ntp_toast_already_synchronized_error);
                    break;

                case NETWORK_DISABLED:
                    responseMessage = getString(R.string.ntp_toast_network_error);
                    break;

                case NTP_TIMEOUT:
                    responseMessage = getString(R.string.ntp_toast_timeout_error);
                    break;

                case NTP_ERROR:
                    String lastExceptionMessage = NTPTime.getLastExceptionMessage();
                    responseMessage = String.format("%s %s", getString(R.string.ntp_toast_synchronization_error_prefix), lastExceptionMessage != null ? lastExceptionMessage : "None");
                    break;

                case UNKNOWN_ERROR:
                    responseMessage = getString(R.string.ntp_toast_unknown_error);
                    break;

                case SUCCESS:
                    responseMessage = getString(R.string.ntp_toast_synchronization_success);
                    break;

            }
            Toast.makeText(this.mainActivity.get(), responseMessage, Toast.LENGTH_LONG).show();


        }
    }

}
