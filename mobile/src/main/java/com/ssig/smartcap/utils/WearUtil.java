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
import com.ssig.sensorsmanager.info.SensorInfo;
import com.ssig.sensorsmanager.SensorType;
import com.ssig.smartcap.R;
import com.ssig.smartcap.common.Serialization;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutionException;

public class WearUtil implements MessageClient.OnMessageReceivedListener{

    public enum ConnectionResponse {
        SUCCESS,
        NO_WEAR_APP,
        BLUETOOTH_DISABLED,
        NO_PAIRED_DEVICES,
        NO_CAPABLE_DEVICES,
        UNKNOWN_ERROR
    }

    private static Node smartcapClientNode = null;

    private static Map<SensorType, SensorInfo> clientSensorInfoResponse;
    private static CountDownLatch requestSensorInfoLatch;

    private static String MESSAGE_PATH_CONNECTION_DONE;
    private static String MESSAGE_PATH_REQUEST_WATCH_SENSORINFO;
    private static String MESSAGE_PATH_RESPONSE_WATCH_SENSORINFO;
    private static String MESSAGE_PATH_SYNC_NTP;
    private static String MESSAGE_PATH_DISCONNECT;

    @Override
    public void onMessageReceived(@NonNull MessageEvent messageEvent) {
        String path = messageEvent.getPath();

        if (path.equals(WearUtil.MESSAGE_PATH_RESPONSE_WATCH_SENSORINFO)) {
            byte[] data = messageEvent.getData();
            WearUtil.clientSensorInfoResponse = Serialization.deserializeObject(data);
            WearUtil.requestSensorInfoLatch.countDown();
        }
    }

    public static void initialize(Context context){
        MESSAGE_PATH_CONNECTION_DONE = context.getString(R.string.message_path_connection_done);
        MESSAGE_PATH_REQUEST_WATCH_SENSORINFO = context.getString(R.string.message_path_request_watch_sensorinfo);
        MESSAGE_PATH_RESPONSE_WATCH_SENSORINFO = context.getString(R.string.message_path_response_watch_sensorinfo);
        MESSAGE_PATH_SYNC_NTP = context.getString(R.string.message_path_sync_ntp);
        MESSAGE_PATH_DISCONNECT = context.getString(R.string.message_path_disconnect);
        Wearable.getMessageClient(context).addListener(new WearUtil());
    }

    public static boolean hasWearOS(Context context){
        return DeviceTools.hasApp(context, context.getString(R.string.util_wear_package));
    }

    public static boolean isConnected() {
        return (WearUtil.smartcapClientNode != null);
    }

    public static String getClientID(){
        return (WearUtil.smartcapClientNode != null) ? WearUtil.smartcapClientNode.getId() : null;
    }

    public static ConnectionResponse synchronize(Context context){
        WearUtil.smartcapClientNode = null;

        if (!DeviceTools.isBlueetothEnabled())
            return ConnectionResponse.BLUETOOTH_DISABLED;

        if (!WearUtil.hasWearOS(context))
            return ConnectionResponse.NO_WEAR_APP;

        try {

            Task<CapabilityInfo> capabilityInfoTask = Wearable.getCapabilityClient(context).getCapability(context.getString(R.string.capability_smartcap_wear), CapabilityClient.FILTER_REACHABLE);
            CapabilityInfo capabilityInfo = Tasks.await(capabilityInfoTask);
            if (!capabilityInfo.getNodes().isEmpty()){
                List<Node> foundedCientNodes = new ArrayList<>(capabilityInfo.getNodes());
                WearUtil.smartcapClientNode = foundedCientNodes.get(0);
                return ConnectionResponse.SUCCESS;
            }

            Task<List<Node>> connectedNodesTask = Wearable.getNodeClient(context).getConnectedNodes();
            List<Node> nodeList = Tasks.await(connectedNodesTask);
            return nodeList.isEmpty() ?  ConnectionResponse.NO_PAIRED_DEVICES : ConnectionResponse.NO_CAPABLE_DEVICES;

        } catch (ExecutionException | InterruptedException e) {
            return ConnectionResponse.UNKNOWN_ERROR;
        }

    }

    public static void sendMessage(Context context, final String path, final byte[] data){
        if (WearUtil.smartcapClientNode == null)
            return;
        Task<Integer> sendMessageTask = Wearable.getMessageClient(context).sendMessage(WearUtil.smartcapClientNode.getId(), path, data != null ? data : new byte[0]);
        sendMessageTask.addOnSuccessListener(new OnSuccessListener<Integer>() {
            @Override
            public void onSuccess(Integer integer) {}
        });
    }

    public static void connectionDone(Context context){
        WearUtil.sendMessage(context, WearUtil.MESSAGE_PATH_CONNECTION_DONE, null);
    }

    public static void disconnect(Context context){
        WearUtil.sendMessage(context, WearUtil.MESSAGE_PATH_DISCONNECT, null);
        WearUtil.smartcapClientNode = null;
    }

    public static void syncClientNTP(Context context, String ntpPool){
        if (DeviceTools.isNetworkConnected(context)) {
            byte[] data = Serialization.serializeObject(ntpPool);
            WearUtil.sendMessage(context, WearUtil.MESSAGE_PATH_SYNC_NTP, data);
        }
    }

    public static Map<SensorType, SensorInfo> requestClientSensorInfo(Context context) {
        if (WearUtil.requestSensorInfoLatch != null)
            return null;

        WearUtil.requestSensorInfoLatch = new CountDownLatch(1);

        WearUtil.sendMessage(context, WearUtil.MESSAGE_PATH_REQUEST_WATCH_SENSORINFO, null);
        try {
            WearUtil.requestSensorInfoLatch.await();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        WearUtil.requestSensorInfoLatch = null;
        return WearUtil.clientSensorInfoResponse;
    }

}
