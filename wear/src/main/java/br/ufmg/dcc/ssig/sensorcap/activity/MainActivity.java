package br.ufmg.dcc.ssig.sensorcap.activity;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.os.SystemClock;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.wearable.activity.WearableActivity;
import android.support.wearable.input.WearableButtons;
import android.view.KeyEvent;
import android.view.View;
import android.widget.Chronometer;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.android.gms.wearable.*;
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
import java.util.Map;
import java.util.Objects;

public class MainActivity extends WearableActivity  implements
        MessageClient.OnMessageReceivedListener,
        ActivityCompat.OnRequestPermissionsResultCallback{

    private final static int PERMISSION_REQUEST_WRITE_EXTERNAL_STORAGE = 1;
    private final static int PERMISSION_REQUEST_BODY_SENSORS = 2;

    private enum DeviceState{
        DISCONNECTED, CONNECTED, CAPTURING
    }

    private ImageView imageHostConnectedStatusIcon;
    private ImageView imageNTPSynchronizedStatusIcon;
    private View layoutLogo;
    private View layoutCountdown;
    private Chronometer chronometer;
    private TextView textCountdown;
    private TextView textStatus;

    private View sendingFilesLayout;
    private View syncingNTPLayout;

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
        setContentView(R.layout.activity_main);

        String systemFolderName = getString(R.string.system_folder_name);
        String captureFolderName = getString(R.string.capture_folder_name);
        this.systemCapturesFolder = new File(String.format("%s%s%s%s%s", Environment.getExternalStorageDirectory().getAbsolutePath(), File.separator, systemFolderName, File.separator, captureFolderName));
        this.deviceState = DeviceState.DISCONNECTED;

        this.hostNodeId = null;
        this.deviceCaptureRunner = null;

        final String uri = String.format("wear://*%s", getString(R.string.message_path_client_activity_prefix));
        Wearable.getMessageClient(this).addListener(this, Uri.parse(uri), MessageClient.FILTER_PREFIX);
        Wearable.getCapabilityClient(this).addLocalCapability(getString(R.string.capability_sensorcap_wear));

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
        this.updateStatusIcons();
        this.updateStatus();
    }

    @Override
    protected void onDestroy() {
        Wearable.getCapabilityClient(this).removeLocalCapability(getString(R.string.capability_sensorcap_wear));
        Wearable.getMessageClient(this).removeListener(this);
        NTPTime.close();
        this.disconnectFromHost();
        super.onDestroy();
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
            this.setState(DeviceState.CONNECTED);
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
            this.updateStatusIcons();
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
        return this.deviceState != DeviceState.DISCONNECTED;
    }

    private void disconnectFromHost(){
        sendMessageToHost(getString(R.string.message_path_host_activity_disconnect));
        this.disconnect();

    }

    private void disconnect(){
        this.hostNodeId = null;
        NTPTime.close();
        this.setState(DeviceState.DISCONNECTED);
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
    private void checkPermissions(){
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED){
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, PERMISSION_REQUEST_WRITE_EXTERNAL_STORAGE);
        }
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.BODY_SENSORS) != PackageManager.PERMISSION_GRANTED){
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.BODY_SENSORS}, PERMISSION_REQUEST_BODY_SENSORS);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        if (requestCode == PERMISSION_REQUEST_WRITE_EXTERNAL_STORAGE){
            if (!((grantResults.length == 1) && (grantResults[0] == PackageManager.PERMISSION_GRANTED))) {
                finish();
            }
        }
        if (requestCode == PERMISSION_REQUEST_BODY_SENSORS){
            if (!((grantResults.length == 1) && (grantResults[0] == PackageManager.PERMISSION_GRANTED))) {
                finish();
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

        this.syncingNTPLayout = findViewById(R.id.ntp_layout);
        this.sendingFilesLayout = findViewById(R.id.sending_layout);

        View imageKeyOneLayout = findViewById(R.id.key_one_image_layout);
        imageKeyOneLayout.setVisibility(View.GONE);

        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.N) {
            WearableButtons.ButtonInfo buttonOneInfo = WearableButtons.getButtonInfo(this, KeyEvent.KEYCODE_STEM_1);
            if(buttonOneInfo != null){
                imageKeyOneLayout.setVisibility(View.VISIBLE);
                ImageView imageKeyOne = findViewById(R.id.key_one_image);
                imageKeyOne.setImageDrawable(WearableButtons.getButtonIcon(this, KeyEvent.KEYCODE_STEM_1));
            }
        }

    }

    private void updateStatusIcons(){
        this.imageHostConnectedStatusIcon.setImageResource(this.isConnected() ? R.drawable.ic_smartphone_on : R.drawable.ic_smartphone_off);
        this.imageHostConnectedStatusIcon.setColorFilter(ContextCompat.getColor(this, this.isConnected() ? R.color.colorAccent : R.color.colorAlert));
        this.imageNTPSynchronizedStatusIcon.setImageResource(NTPTime.isSynchronized() ? R.drawable.ic_ntp_on : R.drawable.ic_ntp_off);
        this.imageNTPSynchronizedStatusIcon.setColorFilter(ContextCompat.getColor(this, NTPTime.isSynchronized() ? R.color.colorAccent : R.color.colorAlert));
    }

    private void updateStatus(){
        switch(this.deviceState){
            case DISCONNECTED:
                this.textStatus.setText(R.string.waiting_connection);
                break;
            case CONNECTED:
                this.textStatus.setText(R.string.ready_to_capture);
                break;
            case CAPTURING:
                this.textStatus.setText(R.string.capturing);
                break;
        }
    }

    private void setState(DeviceState deviceState){
        this.deviceState = deviceState;
        this.updateStatus();
        this.updateStatusIcons();
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
                layoutLogo.setVisibility(View.GONE);
                chronometer.setBase(SystemClock.elapsedRealtime());
                chronometer.start();
                chronometer.setVisibility(View.VISIBLE);
                if (deviceCaptureRunner != null)
                    deviceCaptureRunner.start();
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
        this.setState(DeviceState.CONNECTED);

        if (this.deviceCaptureRunner != null){
            try {
                deviceCaptureRunner.finish();
            } catch (IOException e) {
                Toast.makeText(this, e.getMessage(), Toast.LENGTH_LONG).show();
                return;
            }
        }

        this.deviceCaptureRunner = null;
    }

    private void sendSensorFiles(DeviceData deviceData){

        this.sendingFilesLayout.setVisibility(View.VISIBLE);

        File deviceCaptureFolder = new File(String.format("%s%s%s%s%s", this.systemCapturesFolder, File.separator, deviceData.getCaptureDataUUID(), File.separator, deviceData.getDeviceDataUUID()));
        if (!deviceCaptureFolder.exists()) {
            this.sendMessageToHost(getString(R.string.message_path_host_archive_fragment_sensor_files_error), getString(R.string.sensor_files_error_capture_folder_no_exists).getBytes());
            this.sendingFilesLayout.setVisibility(View.GONE);
            Toast.makeText(MainActivity.this, getString(R.string.sensor_files_error_capture_folder_no_exists), Toast.LENGTH_LONG).show();
            return;
        }

        Map<SensorType, Asset> assets = new HashMap<>();
        for (Map.Entry<SensorType, SensorData> entry : deviceData.getSensorsData().entrySet()){
            if (entry.getValue().isEnable()) {
                File sensorFile = new File(String.format("%s%s%s.dat", deviceCaptureFolder, File.separator, entry.getValue().getSensorDataUUID()));
                if (sensorFile.exists()) {
                    Asset asset = Asset.createFromUri(Uri.fromFile(sensorFile));
                    assets.put(entry.getKey(), asset);
                } else {
                    this.sendMessageToHost(getString(R.string.message_path_host_archive_fragment_sensor_files_error), getString(R.string.sensor_files_error_file_no_exists).getBytes());
                    this.sendingFilesLayout.setVisibility(View.GONE);
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
                    sendingFilesLayout.setVisibility(View.GONE);
                    Toast.makeText(MainActivity.this, getString(R.string.sensor_files_error_data_item), Toast.LENGTH_LONG).show();
                }
            });
            dataItemTask.addOnSuccessListener(new OnSuccessListener<DataItem>() {
                @Override
                public void onSuccess(DataItem dataItem) { }
            });

        }

        sendingFilesLayout.setVisibility(View.GONE);
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
            mainActivity.get().syncingNTPLayout.setVisibility(View.VISIBLE);
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

            this.mainActivity.get().updateStatusIcons();

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
            mainActivity.get().syncingNTPLayout.setVisibility(View.GONE);
            Toast.makeText(this.mainActivity.get(), responseMessage, Toast.LENGTH_LONG).show();


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
