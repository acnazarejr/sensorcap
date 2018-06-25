package com.ssig.smartcap.service;

import android.app.Service;
import android.content.Intent;
import android.net.Uri;
import android.os.Binder;
import android.os.IBinder;
import android.support.annotation.NonNull;
import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.common.api.Status;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.android.gms.tasks.Tasks;
import com.google.android.gms.wearable.*;
import com.ssig.sensorsmanager.config.DeviceConfig;
import com.ssig.sensorsmanager.data.DeviceData;
import com.ssig.sensorsmanager.info.DeviceInfo;
import com.ssig.smartcap.R;
import com.ssig.smartcap.common.Serialization;
import com.ssig.smartcap.utils.DeviceTools;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

public class WearService extends Service implements MessageClient.OnMessageReceivedListener{

    public class WearBinder extends Binder {
        public WearService getService() {
            return WearService.this;
        }
    }

    public enum WearConnectionResponse {
        ALREADY_CONNECTED,
        NO_WEAR_APP,
        BLUETOOTH_DISABLED,
        NO_PAIRED_DEVICES,
        NO_CAPABLE_DEVICES,
        TIMEOUT,
        DEVICE_INFO_ERROR,
        SUCCESS,
        UNKNOWN_ERROR
    }

    private final IBinder mBinder = new WearBinder();

    private Node clientNode;
    private DeviceInfo clientDeviceInfo;
    private CountDownLatch requestSensorInfoLatch;
    private String lastExceptionMessage;

    //----------------------------------------------------------------------------------------------
    // Override Methods
    //----------------------------------------------------------------------------------------------
    @Override
    public void onCreate() {
        super.onCreate();
        this.reset();
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        return START_NOT_STICKY;
    }

    @Override
    public IBinder onBind(Intent intent) {
        final String uri = String.format("wear://*%s", getString(R.string.message_path_host_service_prefix));
        Wearable.getMessageClient(this).addListener(this, Uri.parse(uri), MessageClient.FILTER_PREFIX);
        return  mBinder;
    }

    @Override
    public boolean onUnbind(Intent intent) {
        Wearable.getMessageClient(this).removeListener(this);
        if (this.isConnected())
            this.disconnect();
        return super.onUnbind(intent);
    }



    //----------------------------------------------------------------------------------------------
    // Message Received
    //----------------------------------------------------------------------------------------------
    @Override
    public void onMessageReceived(@NonNull MessageEvent messageEvent) {
        String path = messageEvent.getPath();
        if (path.equals(getString(R.string.message_path_host_service_response_watch_sensorinfo))) {
            if (this.requestSensorInfoLatch != null) {
                byte[] data = messageEvent.getData();
                this.clientDeviceInfo = Serialization.deserializeObject(data);
                if (this.requestSensorInfoLatch != null)
                    this.requestSensorInfoLatch.countDown();
            }
        }
    }


    //----------------------------------------------------------------------------------------------
    // Public Properties
    //----------------------------------------------------------------------------------------------
    public DeviceInfo getClientDeviceInfo() {
        return clientDeviceInfo;
    }

    public boolean isConnected() {
        return this.clientNode != null;
    }

    public String getLastExceptionMessage(){
        return this.lastExceptionMessage;
    }

    //----------------------------------------------------------------------------------------------
    // Generic STUFFS
    //----------------------------------------------------------------------------------------------
    private void reset(){
        this.clientNode = null;
        this.clientDeviceInfo = null;
        this.requestSensorInfoLatch = null;
        this.lastExceptionMessage = null;
    }

    private void sendMessage(final String path) throws ApiException {
        this.sendMessage(path, new byte[0]);
    }

    private void sendMessage(final String path, final byte[] data) throws ApiException {
        if (this.clientNode == null)
            throw new ApiException(Status.RESULT_DEAD_CLIENT);
        Task<Integer> sendMessageTask = Wearable.getMessageClient(this).sendMessage(this.clientNode.getId(), path, data);
        sendMessageTask.addOnSuccessListener(new OnSuccessListener<Integer>() {
            @Override
            public void onSuccess(Integer integer) {}
        });
    }

    //----------------------------------------------------------------------------------------------
    // Connection STUFFS
    //----------------------------------------------------------------------------------------------
    public WearConnectionResponse connect(){

        if (this.clientNode != null)
            return WearConnectionResponse.ALREADY_CONNECTED;

        this.reset();

        if (!DeviceTools.isAppInstalled(this, getString(R.string.util_wear_package)))
            return WearConnectionResponse.NO_WEAR_APP;

        if (DeviceTools.isBluetoothDisabled())
            return WearConnectionResponse.BLUETOOTH_DISABLED;

        try {

            Node capableNode = this.getFirstCapableNode();

            if (capableNode == null){
                Task<List<Node>> connectedNodesTask = Wearable.getNodeClient(this).getConnectedNodes();
                List<Node> nodeList = Tasks.await(connectedNodesTask, 10, TimeUnit.SECONDS);
                List<Node> nearbyNodeList = new ArrayList<>();
                for (Node node : nodeList){
                    if (node.isNearby())
                        nearbyNodeList.add(node);
                }
                return nearbyNodeList.isEmpty() ?  WearConnectionResponse.NO_PAIRED_DEVICES : WearConnectionResponse.NO_CAPABLE_DEVICES;
            }

            this.clientNode = capableNode;
            this.sendMessage(getString(R.string.message_path_client_activity_connected));
            if(requestClientSensorInfo()){
                return WearConnectionResponse.SUCCESS;
            }else{
                this.disconnect();
                return WearConnectionResponse.DEVICE_INFO_ERROR;
            }

        } catch (InterruptedException | ExecutionException | ApiException e) {
            this.lastExceptionMessage = e.getMessage();
            return WearConnectionResponse.UNKNOWN_ERROR;
        } catch (TimeoutException e) {
            return WearConnectionResponse.TIMEOUT;
        }

    }

    private Node getFirstCapableNode() throws InterruptedException, ExecutionException, TimeoutException {

        Node foundedNode = null;
        Task<CapabilityInfo> capabilityInfoTask = Wearable.getCapabilityClient(this).getCapability(getString(R.string.capability_smartcap_wear), CapabilityClient.FILTER_REACHABLE);
        CapabilityInfo capabilityInfo = Tasks.await(capabilityInfoTask, 10, TimeUnit.SECONDS);
        if (!capabilityInfo.getNodes().isEmpty()){
            List<Node> foundedClientNodes = new ArrayList<>(capabilityInfo.getNodes());
            if(foundedClientNodes.get(0).isNearby())
                foundedNode = foundedClientNodes.get(0);
        }
        return foundedNode;

    }

    private boolean requestClientSensorInfo() throws ApiException, TimeoutException {
        if (this.requestSensorInfoLatch != null)
            return false;
        this.requestSensorInfoLatch = new CountDownLatch(1);
        this.sendMessage(getString(R.string.message_path_client_activity_request_watch_sensorinfo));
        try {
            this.requestSensorInfoLatch.await(30, TimeUnit.SECONDS);
        } catch (InterruptedException e) {
            this.requestSensorInfoLatch = null;
            throw new TimeoutException(e.getMessage());
        }
        this.requestSensorInfoLatch = null;
        return this.clientDeviceInfo != null;
    }

    //----------------------------------------------------------------------------------------------
    // Disconnection STUFFS
    //----------------------------------------------------------------------------------------------
    private void disconnect(){
        this.disconnect(true);
    }

    public void disconnect(boolean sendDisconnectMessageToClient){
        if (sendDisconnectMessageToClient) {
            try {
                this.sendMessage(getString(R.string.message_path_client_activity_disconnected));
            } catch (ApiException e) {
                e.printStackTrace();
            }
        }
        this.reset();
    }


    //----------------------------------------------------------------------------------------------
    // NTP STUFFS
    //----------------------------------------------------------------------------------------------
    public void syncClientNTP(String ntpPool){
        try {
            byte[] data = Serialization.serializeObject(ntpPool);
            this.sendMessage(getString(R.string.message_path_client_activity_sync_ntp), data);
        } catch (ApiException e) {
            e.printStackTrace();
        }
    }

    public void closeClientNTP(){
        try {
            this.sendMessage(getString(R.string.message_path_client_activity_close_ntp));
        } catch (ApiException e) {
            e.printStackTrace();
        }
    }

    //----------------------------------------------------------------------------------------------
    // Capture STUFFS
    //----------------------------------------------------------------------------------------------
    public void startCapture(DeviceConfig deviceConfig){
        try {
            byte[] data = Serialization.serializeObject(deviceConfig);
            this.sendMessage(getString(R.string.message_path_client_activity_start_capture), data);
        } catch (ApiException e) {
            e.printStackTrace();
        }
    }

    public void stopCapture(){
        try {
            this.sendMessage(getString(R.string.message_path_client_activity_stop_capture));
        } catch (ApiException e) {
            e.printStackTrace();
        }
    }

    //----------------------------------------------------------------------------------------------
    // Sensor Files STUFFS
    //----------------------------------------------------------------------------------------------
    public void clearWearCaptures(){
        try {
            this.sendMessage(getString(R.string.message_path_client_activity_clear_captures));
        } catch (ApiException e) {
            e.printStackTrace();
        }
    }


    public void requestSensorFiles(DeviceData deviceData) throws ApiException {
        byte[] data = Serialization.serializeObject(deviceData);
        this.sendMessage(getString(R.string.message_path_client_activity_request_sensor_files), data);
    }

}
