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
import com.google.android.gms.wearable.CapabilityClient;
import com.google.android.gms.wearable.CapabilityInfo;
import com.google.android.gms.wearable.MessageClient;
import com.google.android.gms.wearable.MessageEvent;
import com.google.android.gms.wearable.Node;
import com.google.android.gms.wearable.Wearable;
import com.ssig.sensorsmanager.SensorType;
import com.ssig.sensorsmanager.config.CaptureConfig;
import com.ssig.sensorsmanager.info.SensorInfo;
import com.ssig.smartcap.R;
import com.ssig.smartcap.common.Serialization;
import com.ssig.smartcap.utils.DeviceTools;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;

public class WearService extends Service implements MessageClient.OnMessageReceivedListener{

    public class WearBinder extends Binder {
        public WearService getService() {
            return WearService.this;
        }
    }

    public enum ConnectionResponse {
        SUCCESS,
        NO_WEAR_APP,
        BLUETOOTH_DISABLED,
        NO_PAIRED_DEVICES,
        NO_CAPABLE_DEVICES,
        TIMEOUT,
        UNKNOWN_ERROR
    }

    private final IBinder mBinder = new WearBinder();

    private Node mClientNode;
    private Map<SensorType, SensorInfo> mClientSensorInfo;
    private CountDownLatch mRequestSensorInfoLatch;

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
        return super.onUnbind(intent);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
    }

    @Override
    public void onMessageReceived(@NonNull MessageEvent messageEvent) {
        String path = messageEvent.getPath();
        if (path.equals(getString(R.string.message_path_host_service_response_watch_sensorinfo))) {
            if (this.mRequestSensorInfoLatch != null) {
                byte[] data = messageEvent.getData();
                this.mClientSensorInfo = Serialization.deserializeObject(data);
                this.mRequestSensorInfoLatch.countDown();
            }
        }
    }



    public ConnectionResponse connect(){
        if (this.mClientNode != null)
            return ConnectionResponse.SUCCESS;

        if (DeviceTools.isBluetoothDisabled())
            return ConnectionResponse.BLUETOOTH_DISABLED;

        if (!this.hasWearOS())
            return ConnectionResponse.NO_WEAR_APP;

        try {

            Task<CapabilityInfo> capabilityInfoTask = Wearable.getCapabilityClient(this).getCapability(getString(R.string.capability_smartcap_wear), CapabilityClient.FILTER_REACHABLE);
            CapabilityInfo capabilityInfo = Tasks.await(capabilityInfoTask);
            if (!capabilityInfo.getNodes().isEmpty()){
                List<Node> foundedClientNodes = new ArrayList<>(capabilityInfo.getNodes());
                this.mClientNode = foundedClientNodes.get(0);
                if (this.requestClientSensorInfo()) {
                    this.sendMessage(getString(R.string.message_path_client_service_connection_done));
                    return ConnectionResponse.SUCCESS;
                }else{
                    return ConnectionResponse.TIMEOUT;
                }
            }

            Task<List<Node>> connectedNodesTask = Wearable.getNodeClient(this).getConnectedNodes();
            List<Node> nodeList = Tasks.await(connectedNodesTask);
            return nodeList.isEmpty() ?  ConnectionResponse.NO_PAIRED_DEVICES : ConnectionResponse.NO_CAPABLE_DEVICES;

        } catch (ExecutionException | InterruptedException | ApiException e) {
            return ConnectionResponse.UNKNOWN_ERROR;
        }

    }

    public void disconnect(){
        this.disconnect(true);
    }

    public void disconnect(boolean sendDisconnectMessageToClient){
        if (sendDisconnectMessageToClient) {
            try {
                this.sendMessage(getString(R.string.message_path_client_service_disconnect));
            } catch (ApiException e) {
                e.printStackTrace();
            }
        }
        this.reset();
    }

    public void syncClientNTP(String ntpPool){
        try {
            byte[] data = Serialization.serializeObject(ntpPool);
            this.sendMessage(getString(R.string.message_path_client_service_sync_ntp), data);
        } catch (ApiException e) {
            e.printStackTrace();
        }
    }

    public void closeClientNTP(){
        try {
            this.sendMessage(getString(R.string.message_path_client_service_close_ntp));
        } catch (ApiException e) {
            e.printStackTrace();
        }
    }

    public void setCaptureCapability(boolean capable){
        if (capable){
            Wearable.getCapabilityClient(this).addLocalCapability(getString(R.string.capability_smartcap_capture));
        }else{
            Wearable.getCapabilityClient(this).removeLocalCapability(getString(R.string.capability_smartcap_capture));
        }
    }

    public void startCapture(CaptureConfig captureConfig){
        try {
            byte[] data = Serialization.serializeObject(captureConfig);
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

    public Map<SensorType, SensorInfo> getClientSensorInfo() {
        return mClientSensorInfo;
    }

    public boolean isConnected() {
        return this.mClientNode != null;
    }

    public String getClientID(){
        return (this.mClientNode != null) ? this.mClientNode.getId() : null;
    }

    public boolean hasWearOS(){
        return DeviceTools.isAppInstalled(this, getString(R.string.util_wear_package));
    }

    private boolean requestClientSensorInfo() throws ApiException {
        if (this.mRequestSensorInfoLatch != null)
            return false;
        this.mRequestSensorInfoLatch = new CountDownLatch(1);
        this.sendMessage(getString(R.string.message_path_client_service_request_watch_sensorinfo));
        try {
            this.mRequestSensorInfoLatch.await(10, TimeUnit.SECONDS);
        } catch (InterruptedException e) {
            e.printStackTrace();
            return false;
        }
        this.mRequestSensorInfoLatch = null;
        return true;
    }

    private void reset(){
        this.mClientNode = null;
        this.mClientSensorInfo = null;
        this.mRequestSensorInfoLatch = null;
    }

    private void sendMessage(final String path) throws ApiException {
        this.sendMessage(path, new byte[0]);
    }

    private void sendMessage(final String path, final byte[] data) throws ApiException {
        if (this.mClientNode == null)
            throw new ApiException(Status.RESULT_DEAD_CLIENT);
        Task<Integer> sendMessageTask = Wearable.getMessageClient(this).sendMessage(this.mClientNode.getId(), path, data);
        sendMessageTask.addOnSuccessListener(new OnSuccessListener<Integer>() {
            @Override
            public void onSuccess(Integer integer) {}
        });
    }

}
