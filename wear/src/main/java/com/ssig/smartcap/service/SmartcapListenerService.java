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

import android.content.Intent;
import android.net.Uri;
import android.util.Log;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.android.gms.wearable.DataEvent;
import com.google.android.gms.wearable.DataEventBuffer;
import com.google.android.gms.wearable.MessageEvent;
import com.google.android.gms.wearable.Node;
import com.google.android.gms.wearable.Wearable;
import com.google.android.gms.wearable.WearableListenerService;
import com.ssig.sensorsmanager.SensorInfo;
import com.ssig.smartcap.MainActivity;
import com.ssig.smartcap.R;
import com.ssig.smartcap.common.Serialization;

import java.util.concurrent.TimeUnit;

/**
 * Listens to DataItems and Messages from the local node.
 */
public class SmartcapListenerService extends WearableListenerService {

//    private static final String START_ACTIVITY_PATH = "/start-activity";
//    private static final String DATA_ITEM_RECEIVED_PATH = "/data-item-received";
//    public static final String COUNT_PATH = "/count";
//    public static final String IMAGE_PATH = "/image";
//    public static final String IMAGE_KEY = "photo";
    GoogleApiClient mGoogleApiClient;

    @Override
    public void onCreate() {
        mGoogleApiClient = new GoogleApiClient.Builder(this)
                .addApi(Wearable.API)
                .build();
        mGoogleApiClient.connect();
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

    @Override
    public void onMessageReceived(MessageEvent messageEvent) {
        String path = messageEvent.getPath();
        if (path.equals(getString(R.string.message_path_open_watch_activity))) {
            Intent startIntent = new Intent(this, MainActivity.class);
            startIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            startActivity(startIntent);
        }else if (path.equals(getString(R.string.message_path_request_watch_sensorinfo))) {
            byte[] data = Serialization.serializeObject(SensorInfo.getAll(this));
            this.sendMessageData(messageEvent.getSourceNodeId(), this.getString(R.string.message_path_response_watch_sensorinfo), data);
        }
    }
//
//    public static void LOGD(final String tag, String message) {
//        if (Log.isLoggable(tag, Log.DEBUG)) {
//            Log.d(tag, message);
//        }
//    }


    public void sendMessageData(String nodeID, final String path, byte[] data){
        Task<Integer> sendMessageTask = Wearable.getMessageClient(this).sendMessage(nodeID, path, data);
        sendMessageTask.addOnSuccessListener(new OnSuccessListener<Integer>() {
            @Override
            public void onSuccess(Integer integer) {}
        });
    }


}