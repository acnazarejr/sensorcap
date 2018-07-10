package br.ufmg.dcc.ssig.sensorcap.activity;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.os.SystemClock;
import android.provider.Settings;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.wearable.activity.WearableActivity;
import android.view.KeyEvent;
import android.view.View;
import android.widget.Button;
import android.widget.Chronometer;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.android.gms.wearable.*;

import br.ufmg.dcc.ssig.sensorcap.BuildConfig;
import br.ufmg.dcc.ssig.sensorsmanager.SensorType;
import br.ufmg.dcc.ssig.sensorsmanager.capture.DeviceCaptureRunner;
import br.ufmg.dcc.ssig.sensorsmanager.config.DeviceConfig;
import br.ufmg.dcc.ssig.sensorsmanager.data.DeviceData;
import br.ufmg.dcc.ssig.sensorsmanager.data.SensorData;
import br.ufmg.dcc.ssig.sensorsmanager.info.DeviceInfo;
import br.ufmg.dcc.ssig.sensorsmanager.util.NTPTime;
import br.ufmg.dcc.ssig.sensorcap.R;
import br.ufmg.dcc.ssig.sensorcap.common.CountDownAnimation;
import br.ufmg.dcc.ssig.sensorcap.common.Serialization;

import java.io.File;
import java.io.IOException;
import java.lang.ref.WeakReference;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Objects;

public class MainActivity extends WearableActivity  implements
        MessageClient.OnMessageReceivedListener,
        ActivityCompat.OnRequestPermissionsResultCallback{

    private enum DeviceState{
        IDLE, CAPTURING, STOPPING, NTP, SENDING, PERMISSIONS
    }

    private final static int PERMISSION_REQUEST_WRITE_EXTERNAL_STORAGE = 1;
    private final static int PERMISSION_REQUEST_BODY_SENSORS = 2;
    private final static int REQUEST_PERMISSION_SETTING = 9;

    private View layoutMain;
    private View layoutTask;
    private View layoutCountdown;
    private View layoutCapture;
    private View layoutPermissionsDenied;
    private View layoutAmbientMode;

    private Chronometer chronometerPrimary;
    private Chronometer chronometerAmbient;
    private TextView textCountdown;

    private File systemCapturesFolder;
    private DeviceState deviceState;
    private String hostNodeId;
    private DeviceCaptureRunner deviceCaptureRunner;


    //----------------------------------------------------------------------------------------------
    // Override Functions
    //----------------------------------------------------------------------------------------------
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setAmbientEnabled();

        setContentView(R.layout.activity_main);

        String systemFolderName = getString(R.string.system_folder_name);
        String captureFolderName = getString(R.string.capture_folder_name);
        this.systemCapturesFolder = new File(String.format("%s%s%s%s%s", Environment.getExternalStorageDirectory().getAbsolutePath(), File.separator, systemFolderName, File.separator, captureFolderName));

        this.hostNodeId = null;
        this.deviceCaptureRunner = null;

        final String uri = String.format("wear://*%s", getString(R.string.message_path_client_activity_prefix));
        Wearable.getMessageClient(this).addListener(this, Uri.parse(uri), MessageClient.FILTER_PREFIX);

        this.initUI();
        this.setState(DeviceState.IDLE);
        this.checkPermissions(false);
    }

    @Override
    protected void onStart() {
        super.onStart();
    }

    @Override
    protected void onResume() {
        super.onResume();
        this.setState(this.deviceState);
    }

    @Override
    protected void onDestroy() {
        Wearable.getCapabilityClient(this).removeLocalCapability(getString(R.string.capability_sensorcap_wear));
        Wearable.getMessageClient(this).removeListener(this);
        NTPTime.close();
        this.disconnectFromHost();
        super.onDestroy();
    }

    @Override
    public void onEnterAmbient(Bundle ambientDetails) {
        super.onEnterAmbient(ambientDetails);
        this.chronometerAmbient.setVisibility(this.deviceState == DeviceState.CAPTURING ? View.VISIBLE : View.GONE);
        this.layoutAmbientMode.setVisibility(View.VISIBLE);
    }

    @Override
    public void onExitAmbient() {
        super.onExitAmbient();
        this.layoutAmbientMode.setVisibility(View.GONE);
    }

    //----------------------------------------------------------------------------------------------
    // UI STUFFS
    //----------------------------------------------------------------------------------------------
    private void initUI(){

        this.layoutMain = findViewById(R.id.main_layout);
        this.layoutTask = findViewById(R.id.task_layout);

        this.layoutCapture = findViewById(R.id.capture_layout);
        this.chronometerPrimary = findViewById(R.id.chronometer_capture);
        this.chronometerAmbient = findViewById(R.id.chronometer_ambient_mode);
        Button stopButton = findViewById(R.id.stop_button);
        stopButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                stopHostCapture();
            }
        });

        this.layoutPermissionsDenied = findViewById(R.id.permissions_layout_denied);
        Button buttonPermission = this.layoutPermissionsDenied.findViewById(R.id.permissions_button);
        buttonPermission.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                checkPermissions(true);
            }
        });

        this.layoutAmbientMode = findViewById(R.id.ambient_mode_layout);

        this.layoutCountdown = findViewById(R.id.countdown_layout);
        this.textCountdown = findViewById(R.id.countdown_text);

    }

    private void setState(DeviceState deviceState){
        this.deviceState = deviceState;

        this.layoutMain.setVisibility(View.GONE);
        this.layoutTask.setVisibility(View.GONE);
        this.layoutPermissionsDenied.setVisibility(View.GONE);
        this.layoutCountdown.setVisibility(View.GONE);
        this.layoutCapture.setVisibility(View.GONE);

        switch(this.deviceState){
            case IDLE:
                this.layoutMain.setVisibility(View.VISIBLE);
                TextView textAppVersion = findViewById(R.id.main_layout_app_version);
                textAppVersion.setText(String.format("Version %s", BuildConfig.VERSION_NAME));
                ImageView mainHostConnectedStatusIcon = findViewById(R.id.host_connected_status_icon_image);
                ImageView mainNTPSynchronizedStatusIcon = findViewById(R.id.ntp_synchronized_status_icon_image);
                mainHostConnectedStatusIcon.setImageResource(this.isConnected() ? R.drawable.ic_smartphone_on : R.drawable.ic_smartphone_off);
                mainHostConnectedStatusIcon.setColorFilter(ContextCompat.getColor(this, this.isConnected() ? R.color.colorAccent : R.color.colorAlert));
                mainNTPSynchronizedStatusIcon.setImageResource(NTPTime.isSynchronized() ? R.drawable.ic_ntp_on : R.drawable.ic_ntp_off);
                mainNTPSynchronizedStatusIcon.setColorFilter(ContextCompat.getColor(this, NTPTime.isSynchronized() ? R.color.colorAccent : R.color.colorAlert));
                break;
            case NTP:
                this.layoutTask.setVisibility(View.VISIBLE);
                ImageView taskIconNTP = findViewById(R.id.task_icon);
                TextView taskMessageNTP = findViewById(R.id.task_message);
                taskIconNTP.setImageDrawable(getDrawable(R.drawable.ic_ntp_on));
                taskMessageNTP.setText(R.string.syncing_ntp);
                break;
            case STOPPING:
                this.layoutTask.setVisibility(View.VISIBLE);
                ImageView taskIconStop = findViewById(R.id.task_icon);
                TextView taskMessageStop = findViewById(R.id.task_message);
                taskIconStop.setImageDrawable(getDrawable(R.drawable.ic_save));
                taskMessageStop.setText(R.string.saving);
                break;
            case SENDING:
                this.layoutTask.setVisibility(View.VISIBLE);
                ImageView taskIconSend = findViewById(R.id.task_icon);
                TextView taskMessageSend = findViewById(R.id.task_message);
                taskIconSend.setImageDrawable(getDrawable(R.drawable.ic_progress_upload));
                taskMessageSend.setText(R.string.sending_files);
                break;
            case CAPTURING:
                this.layoutCapture.setVisibility(View.VISIBLE);
                break;
            case PERMISSIONS:
                this.layoutPermissionsDenied.setVisibility(View.VISIBLE);
                break;
        }



    }


    //----------------------------------------------------------------------------------------------
    // Hardware Button STUFFS
    //----------------------------------------------------------------------------------------------
    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        super.onKeyDown(keyCode, event);
        if (this.deviceState == DeviceState.CAPTURING)
            this.stopHostCapture();
        event.startTracking();
        return true;
    }

    @Override
    public boolean onKeyLongPress(int keyCode, KeyEvent event) {
        this.finish();
        return false;
    }


    //----------------------------------------------------------------------------------------------
    // Wear STUFFS
    //----------------------------------------------------------------------------------------------
    @Override
    public void onMessageReceived(@NonNull MessageEvent messageEvent) {
        String path = messageEvent.getPath();

        if (path.equals(getString(R.string.message_path_client_activity_request_watch_sensorinfo))) {
            byte[] data = Serialization.serializeObject(DeviceInfo.get(this));
            this.sendMessageData(messageEvent.getSourceNodeId(), this.getString(R.string.message_path_host_service_response_watch_sensorinfo), data);
        }

        if (path.equals(getString(R.string.message_path_client_activity_connected))) {
            this.hostNodeId = messageEvent.getSourceNodeId();
            this.setState(DeviceState.IDLE);
        }

        if (path.equals(getString(R.string.message_path_client_activity_disconnected))) {
            this.disconnect();
        }

        if (path.equals(getString(R.string.message_path_client_activity_sync_ntp))){
            byte[] data = messageEvent.getData();
            String ntpPool = Serialization.deserializeObject(data);
            new NTPSynchronizationTask(this).execute(ntpPool);
        }

        if (path.equals(getString(R.string.message_path_client_activity_close_ntp))){
            NTPTime.close();
            this.setState(DeviceState.IDLE);
        }

        if (path.equals(getString(R.string.message_path_client_activity_start_capture))){
            byte[] data = messageEvent.getData();
            DeviceConfig deviceConfig = Serialization.deserializeObject(data);
            this.startCapture(deviceConfig);
        }
        if (path.equals(getString(R.string.message_path_client_activity_stop_capture))){
            this.stopCapture();
        }

        if (path.equals(getString(R.string.message_path_client_activity_request_sensor_files))){
            byte[] data = messageEvent.getData();
            DeviceData deviceData = Serialization.deserializeObject(data);
            this.sendSensorFiles(deviceData);
        }

        if (path.equals(getString(R.string.message_path_client_activity_clear_captures))){
            this.clearCaptureFiles();
        }
    }

    private boolean isConnected(){
        return this.hostNodeId != null;
    }

    private void disconnectFromHost(){
        sendMessageToHost(getString(R.string.message_path_host_activity_disconnect));
        this.disconnect();

    }

    private void disconnect(){
        this.hostNodeId = null;
        NTPTime.close();
        this.setState(DeviceState.IDLE);
    }

    private void stopHostCapture(){
        sendMessageToHost(getString(R.string.message_path_host_capture_fragment_stop_capture));
    }

    private void sendMessageToHost(String path){
        this.sendMessageToHost(path, new byte[0]);
    }

    private void sendMessageToHost(String path, byte[] data){
        if (this.isConnected()){
            this.sendMessageData(this.hostNodeId, path, data);
        }
    }

    private void sendMessageData(String nodeID, final String path, byte[] data){
        Task<Integer> sendMessageTask = Wearable.getMessageClient(this).sendMessage(nodeID, path, data);
        sendMessageTask.addOnSuccessListener(new OnSuccessListener<Integer>() {
            @Override
            public void onSuccess(Integer integer) {}
        });
    }

    //----------------------------------------------------------------------------------------------
    // Permission STUFFS
    //----------------------------------------------------------------------------------------------
    private void checkPermissions(boolean request){

        int requestCode = 0;
        List<String> permissionsList = new LinkedList<>();
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED){
            permissionsList.add(Manifest.permission.WRITE_EXTERNAL_STORAGE);
            requestCode += PERMISSION_REQUEST_WRITE_EXTERNAL_STORAGE;
        }

        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.BODY_SENSORS) != PackageManager.PERMISSION_GRANTED){
            permissionsList.add(Manifest.permission.BODY_SENSORS);
            requestCode += PERMISSION_REQUEST_BODY_SENSORS;
        }

        if(requestCode > 0){
            this.setState(DeviceState.PERMISSIONS);
            if (request){
                ActivityCompat.requestPermissions(this, permissionsList.toArray(new String[permissionsList.size()]), requestCode);
            }

        }else{
            Wearable.getCapabilityClient(this).addLocalCapability(getString(R.string.capability_sensorcap_wear));
            this.setState(DeviceState.IDLE);
        }

    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        boolean openSettings = false;
        boolean someDenied = false;
        for (int i = 0; i < grantResults.length; i++) {
            if (grantResults[i] != PackageManager.PERMISSION_GRANTED) {
                someDenied = true;
                if(!shouldShowRequestPermissionRationale(permissions[i]))
                    openSettings = true;
            }
        }
        if (someDenied){
            if (openSettings){
                Intent intent = new Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
                Uri uri = Uri.fromParts("package", getPackageName(), null);
                intent.setData(uri);
                startActivityForResult(intent, REQUEST_PERMISSION_SETTING);
            }
            return;
        }

        checkPermissions(false);
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
    }

    //----------------------------------------------------------------------------------------------
    // Capture STUFFS
    //----------------------------------------------------------------------------------------------
    private void startCapture(DeviceConfig deviceConfig){

        if (deviceConfig.isEnable()) {
            try {
                this.deviceCaptureRunner = new DeviceCaptureRunner(Objects.requireNonNull(this), deviceConfig, this.systemCapturesFolder);
            } catch (IOException e) {
                Toast.makeText(this, e.getMessage(), Toast.LENGTH_LONG).show();
                return;
            }
        }

        CountDownAnimation countDownAnimation = new CountDownAnimation(this, this.textCountdown, deviceConfig.getCountdownStart(), deviceConfig.isSound(), deviceConfig.isVibration());
        countDownAnimation.setCountDownListener(new CountDownAnimation.CountDownListener() {
            @Override
            public void onCountDownEnd(CountDownAnimation animation) {
                layoutCountdown.setVisibility(View.GONE);
                setState(DeviceState.CAPTURING);
                chronometerPrimary.setBase(SystemClock.elapsedRealtime());
                chronometerAmbient.setBase(SystemClock.elapsedRealtime());
                chronometerPrimary.start();
                chronometerAmbient.start();
                if (deviceCaptureRunner != null)
                    deviceCaptureRunner.start();
            }
        });

        this.layoutCountdown.setVisibility(View.VISIBLE);
        countDownAnimation.start();
    }

    private void stopCapture(){
        new StopCaptureTask(this).execute();
    }

    private void sendSensorFiles(DeviceData deviceData){

        this.setState(DeviceState.SENDING);

        File deviceCaptureFolder = new File(String.format("%s%s%s%s%s", this.systemCapturesFolder, File.separator, deviceData.getCaptureDataUUID(), File.separator, deviceData.getDeviceDataUUID()));
        if (!deviceCaptureFolder.exists()) {
            this.sendMessageToHost(getString(R.string.message_path_host_archive_fragment_sensor_files_error), getString(R.string.sensor_files_error_capture_folder_no_exists).getBytes());
            this.setState(DeviceState.IDLE);
            Toast.makeText(MainActivity.this, getString(R.string.sensor_files_error_capture_folder_no_exists), Toast.LENGTH_LONG).show();
            return;
        }

        Map<SensorType, Asset> assets = new HashMap<>();
        for (Map.Entry<SensorType, SensorData> entry : deviceData.getSensorsData().entrySet()){
            if (entry.getValue().isEnable()) {
                File sensorFile = new File(String.format("%s%s%s.%s", deviceCaptureFolder, File.separator, entry.getValue().getSensorDataUUID(), deviceData.getSensorWriterType().fileExtension()));
                if (sensorFile.exists()) {
                    Asset asset = Asset.createFromUri(Uri.fromFile(sensorFile));
                    assets.put(entry.getKey(), asset);
                } else {
                    this.sendMessageToHost(getString(R.string.message_path_host_archive_fragment_sensor_files_error), getString(R.string.sensor_files_error_file_no_exists).getBytes());
                    this.setState(DeviceState.IDLE);
                    Toast.makeText(MainActivity.this, getString(R.string.sensor_files_error_file_no_exists), Toast.LENGTH_LONG).show();
                    return;
                }
            }
        }


        for(Map.Entry<SensorType, Asset> entry : assets.entrySet()) {

            final Long timestamp = System.currentTimeMillis();
            PutDataMapRequest putDataMapRequest = PutDataMapRequest.create(String.format("%s/%s", getString(R.string.message_path_host_archive_fragment_sensor_files_sent), entry.getKey().code()));
            putDataMapRequest.getDataMap().putAsset(getString(R.string.data_item_sensors_smartwatch_asset), entry.getValue());
            putDataMapRequest.getDataMap().putString(getString(R.string.data_item_sensors_smartwatch_key), entry.getKey().code());
            putDataMapRequest.getDataMap().putLong("timestamp", timestamp);

            PutDataRequest putDataRequest = putDataMapRequest.asPutDataRequest();
            putDataRequest.setUrgent();

            Task<DataItem> dataItemTask = Wearable.getDataClient(this).putDataItem(putDataRequest);
            dataItemTask.addOnFailureListener(new OnFailureListener() {
                @Override
                public void onFailure(@NonNull Exception e) {
                    sendMessageToHost(getString(R.string.message_path_host_archive_fragment_sensor_files_error), getString(R.string.sensor_files_error_data_item).getBytes());
                    setState(DeviceState.IDLE);
                    Toast.makeText(MainActivity.this, getString(R.string.sensor_files_error_data_item), Toast.LENGTH_LONG).show();
                }
            });
            dataItemTask.addOnSuccessListener(new OnSuccessListener<DataItem>() {
                @Override
                public void onSuccess(DataItem dataItem) { }
            });

        }

        this.setState(DeviceState.IDLE);
        Toast.makeText(MainActivity.this, R.string.files_sent, Toast.LENGTH_LONG).show();

    }

    //----------------------------------------------------------------------------------------------
    // NTP STUFFS
    //----------------------------------------------------------------------------------------------
    @SuppressLint("StaticFieldLeak")
    private class NTPSynchronizationTask extends AsyncTask<String, Void, NTPTime.NTPTimeSyncResponse> {

        private final WeakReference<MainActivity> mainActivity;

        NTPSynchronizationTask(MainActivity mainActivity){
            this.mainActivity = new WeakReference<>(mainActivity);
        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            mainActivity.get().setState(DeviceState.NTP);
        }

        @Override
        protected NTPTime.NTPTimeSyncResponse doInBackground(String... strings) {
            if (strings.length == 0)
                return new NTPTime.NTPTimeSyncResponse(NTPTime.UNKNOWN_ERROR);
            return NTPTime.synchronize(mainActivity.get(), strings[0]);
        }

        @Override
        protected void onPostExecute(NTPTime.NTPTimeSyncResponse ntpTimeSyncResponse) {
            super.onPostExecute(ntpTimeSyncResponse);

            String responseMessage = "";
            switch (ntpTimeSyncResponse.responseType){

                case ALREADY_SYNCHRONIZED:
                    responseMessage = getString(R.string.ntp_toast_already_synchronized_error);
                    break;

                case NETWORK_DISABLED:
                    responseMessage = getString(R.string.ntp_toast_network_error);
                    break;

                case NTP_ERROR:
                    String errorMessage = ntpTimeSyncResponse.errorMessage;
                    responseMessage = String.format("%s %s", getString(R.string.ntp_toast_synchronization_error_prefix), errorMessage != null ? errorMessage : "None");
                    break;

                case UNKNOWN_ERROR:
                    responseMessage = getString(R.string.ntp_toast_unknown_error);
                    break;

                case SUCCESS:
                    responseMessage = getString(R.string.ntp_toast_synchronization_success);
                    break;

            }
            mainActivity.get().setState(DeviceState.IDLE);
            Toast.makeText(this.mainActivity.get(), responseMessage, Toast.LENGTH_LONG).show();


        }
    }


    @SuppressLint("StaticFieldLeak")
    private class StopCaptureTask extends AsyncTask<Void, Void, String>{

        private final WeakReference<MainActivity> mainActivity;

        StopCaptureTask(MainActivity mainActivity){
            this.mainActivity = new WeakReference<>(mainActivity);
        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            chronometerPrimary.stop();
            chronometerAmbient.stop();
            setState(DeviceState.STOPPING);
        }

        @Override
        protected String doInBackground(Void... voids) {
            if (deviceCaptureRunner != null){
                try {
                    deviceCaptureRunner.finish();
                    return "Capture finished";
                } catch (IOException e) {
                    return e.getMessage();
                }
            }
            return "Unknown error";
        }

        @Override
        protected void onPostExecute(String message) {
            super.onPostExecute(message);
            mainActivity.get().setState(DeviceState.IDLE);
            Toast.makeText(this.mainActivity.get(), message, Toast.LENGTH_LONG).show();
        }
    }

    private void clearCaptureFiles(){
        if (this.deleteRecursive(this.systemCapturesFolder))
            Toast.makeText(this, R.string.capture_files_deleted, Toast.LENGTH_LONG).show();
    }

    private boolean deleteRecursive(File fileOrDirectory) {
        if (!fileOrDirectory.exists())
            return false;
        if (fileOrDirectory.isDirectory())
            for (File child : fileOrDirectory.listFiles())
                deleteRecursive(child);
        return fileOrDirectory.delete();
    }

}
