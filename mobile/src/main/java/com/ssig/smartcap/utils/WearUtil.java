package com.ssig.smartcap.utils;

import android.content.Context;
import android.support.annotation.NonNull;

import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.android.gms.tasks.Tasks;
import com.google.android.gms.wearable.CapabilityClient;
import com.google.android.gms.wearable.CapabilityInfo;
import com.google.android.gms.wearable.MessageClient;
import com.google.android.gms.wearable.MessageEvent;
import com.google.android.gms.wearable.Node;
import com.google.android.gms.wearable.Wearable;
import com.ssig.sensorsmanager.SensorInfo;
import com.ssig.sensorsmanager.SensorType;
import com.ssig.smartcap.R;
import com.ssig.smartcap.common.Serialization;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutionException;

public class WearUtil implements MessageClient.OnMessageReceivedListener{

    public enum SynchronizationResponse{
        SUCCESS,
        NO_WEAR_APP,
        BLUETOOTH_DISABLED,
        NO_PAIRED_DEVICES,
        NO_CAPABLE_DEVICES,
        UNKNOWN_ERROR
    }

    private static final WearUtil ourInstance = new WearUtil();
    private Context context;

    private List<Node> clientNodes;
    private Node node;

    private Map<SensorType, SensorInfo> clientSensorInfo;
    private CountDownLatch requestSensorInfoLatch;

    public static WearUtil get() {
        if (ourInstance.context == null)
            return null;
        return ourInstance;
    }

    private WearUtil() {
        this.clientNodes = new ArrayList<>();
        this.node = null;
        this.clientSensorInfo = null;
        this.requestSensorInfoLatch = null;
        this.context = null;
    }

    private void setContext(Context context){
        if (this.context != null)
            return;
        this.context = context;
        Wearable.getMessageClient(this.context).addListener(this);
    }


    public static void initialize(Context context){
        ourInstance.setContext(context);
    }

    public boolean hasWearOS(){
        return DeviceTools.hasApp(context, context.getString(R.string.util_wear_package));
    }

    public boolean isConnected() {
        return (this.node != null);
    }

    public String getClientID(){
        return (this.node != null) ? this.node.getId() : null;
    }


    public SynchronizationResponse synchronize(){
        this.clientNodes = new ArrayList<>();
        this.node = null;

        if (!DeviceTools.isBlueetothEnabled())
            return SynchronizationResponse.BLUETOOTH_DISABLED;

        if (!this.hasWearOS())
            return SynchronizationResponse.NO_WEAR_APP;

        try {

            Task<CapabilityInfo> capabilityInfoTask = Wearable.getCapabilityClient(context).getCapability(context.getString(R.string.capability_smartcap_wear), CapabilityClient.FILTER_REACHABLE);
            CapabilityInfo capabilityInfo = Tasks.await(capabilityInfoTask);
            if (!capabilityInfo.getNodes().isEmpty()){
                this.clientNodes = new ArrayList<>(capabilityInfo.getNodes());
                this.node = this.clientNodes.get(0);
                return SynchronizationResponse.SUCCESS;
            }

            Task<List<Node>> nodeListTask = Wearable.getNodeClient(context).getConnectedNodes();
            List<Node> nodeList = Tasks.await(nodeListTask);
            return nodeList.isEmpty() ?  SynchronizationResponse.NO_PAIRED_DEVICES : SynchronizationResponse.NO_CAPABLE_DEVICES;

        } catch (ExecutionException | InterruptedException e) {
            return SynchronizationResponse.UNKNOWN_ERROR;
        }

    }

    public void sendMessage(final String path){
        if (this.node == null)
            return;
        Task<Integer> sendMessageTask = Wearable.getMessageClient(context).sendMessage(this.node.getId(), path, new byte[0]);
        sendMessageTask.addOnSuccessListener(new OnSuccessListener<Integer>() {
            @Override
            public void onSuccess(Integer integer) {}
        });
    }

    public void openClientActivity(){
        this.sendMessage(this.context.getString(R.string.message_path_open_watch_activity));
    }

    public Map<SensorType, SensorInfo> requestClientSensorInfo() {
        this.requestSensorInfoLatch = new CountDownLatch(1);
        this.sendMessage(this.context.getString(R.string.message_path_request_watch_sensorinfo));
        try {
            this.requestSensorInfoLatch.await();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        return this.clientSensorInfo;
    }

    @Override
    public void onMessageReceived(@NonNull MessageEvent messageEvent) {
        String path = messageEvent.getPath();
        if (path.equals(this.context.getString(R.string.message_path_response_watch_sensorinfo))) {
            byte[] data = messageEvent.getData();
            this.clientSensorInfo = Serialization.deserializeObject(data);
            this.requestSensorInfoLatch.countDown();
        }
    }

}
