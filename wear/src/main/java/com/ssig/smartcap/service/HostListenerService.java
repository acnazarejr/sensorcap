/*
 * Copyright (C) 2014 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.ssig.smartcap.service;

import android.content.Context;
import android.content.Intent;

import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.android.gms.wearable.MessageEvent;
import com.google.android.gms.wearable.Wearable;
import com.google.android.gms.wearable.WearableListenerService;
import com.ssig.sensorsmanager.info.SensorInfo;
import com.ssig.sensorsmanager.time.NTPTime;
import com.ssig.smartcap.activity.MainActivity;
import com.ssig.smartcap.R;
import com.ssig.smartcap.common.Serialization;

import java.io.IOException;


public class HostListenerService extends WearableListenerService {

    public static boolean connectedOnHost = false;
    public static String hostNodeId = null;
    public static Context context = null;
    private Intent mainActivityIntent;

    @Override
    public void onCreate() {
        this.mainActivityIntent = new Intent(this, MainActivity.class);
        this.mainActivityIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_SINGLE_TOP );
        HostListenerService.context = this;
        super.onCreate();
    }

//    @Override
//    public void onDataChanged(DataEventBuffer dataEvents) {
//
//        // Loop through the events and send a message back to the node that created the data item.
//        for (DataEvent event : dataEvents) {
//            Uri uri = event.getDataItem().getUri();
//            String path = uri.getPath();
//            if (COUNT_PATH.equals(path)) {
//                // Get the node id of the node that created the data item from the host portion of
//                // the uri.
//                String nodeId = uri.getHost();
//                // Set the data of the message to be the bytes of the Uri.
//                byte[] payload = uri.toString().getBytes();
//
//                // Send the rpc
//                Wearable.MessageApi.sendMessage(mGoogleApiClient, nodeId, DATA_ITEM_RECEIVED_PATH,
//                        payload);
//            }
//        }
//    }

    public static void disconnect(){
        sendMessageToHost(HostListenerService.context.getString(R.string.message_path_host_activity_disconnect));
        HostListenerService.connectedOnHost = false;
        HostListenerService.hostNodeId = null;
    }

    public static void startHostCapture(){
        sendMessageToHost(HostListenerService.context.getString(R.string.message_path_host_capture_fragment_start_capture));
    }

    public static void stopHostCapture(){
        sendMessageToHost(HostListenerService.context.getString(R.string.message_path_host_capture_fragment_stop_capture));
    }

    public static void sendMessageToHost(String path){
        if (HostListenerService.connectedOnHost){
            Task<Integer> sendMessageTask = Wearable.getMessageClient(HostListenerService.context).sendMessage(HostListenerService.hostNodeId, path, new byte[0]);
            sendMessageTask.addOnSuccessListener(new OnSuccessListener<Integer>() {
                @Override
                public void onSuccess(Integer integer) {}
            });
        }
    }

    @Override
    public void onMessageReceived(MessageEvent messageEvent) {
        String path = messageEvent.getPath();

        if (path.equals(getString(R.string.message_path_client_service_connection_done))) {
            HostListenerService.hostNodeId = messageEvent.getSourceNodeId();
            HostListenerService.connectedOnHost = true;
            startActivity(mainActivityIntent);
        }

        if (path.equals(getString(R.string.message_path_client_service_request_watch_sensorinfo))) {
            byte[] data = Serialization.serializeObject(SensorInfo.getAll(this));
            this.sendMessageData(messageEvent.getSourceNodeId(), this.getString(R.string.message_path_host_service_response_watch_sensorinfo), data);
        }

        if (path.equals(getString(R.string.message_path_client_service_sync_ntp))){
            byte[] data = messageEvent.getData();
            String ntpPool = Serialization.deserializeObject(data);
            try {
                NTPTime.initialize(this, ntpPool);
            } catch (IOException e) {
                e.printStackTrace();
            }
            startActivity(mainActivityIntent);
        }

        if (path.equals(getString(R.string.message_path_client_service_close_ntp))){
            NTPTime.close(this);
            startActivity(mainActivityIntent);
        }

        if (path.equals(getString(R.string.message_path_client_service_disconnect))) {
            HostListenerService.connectedOnHost = false;
            NTPTime.close(this);
            startActivity(mainActivityIntent);
        }
    }


    public void sendMessageData(String nodeID, final String path, byte[] data){
        Task<Integer> sendMessageTask = Wearable.getMessageClient(this).sendMessage(nodeID, path, data);
        sendMessageTask.addOnSuccessListener(new OnSuccessListener<Integer>() {
            @Override
            public void onSuccess(Integer integer) {}
        });
    }




}