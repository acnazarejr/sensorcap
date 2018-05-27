package com.ssig.smartcap.wear;

import android.app.AlertDialog;
import android.bluetooth.BluetoothDevice;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.wearable.activity.WearableActivity;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;

import java.util.Set;

import com.ssig.btmanager.BTConnectorClient;

public class ConnectionActivity extends WearableActivity {

    private BluetoothDevice[] devicesArray;
    private ListView device_list;
    private Context context = this;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_connect);

        // Enables Always-on
        setAmbientEnabled();

        device_list = findViewById(R.id.devices);

        //setDevices
        this.getDevices();
        this.listListeners();
    }

    private void getDevices(){
        Set<BluetoothDevice> bondedDevices = BTConnectorClient.getInstance().listBondedDevices();
        this.devicesArray = new BluetoothDevice[bondedDevices.size()];
        String[] strings = new String[bondedDevices.size()];
        int index = 0;

        if (bondedDevices.size() > 0) {
            for (BluetoothDevice device : bondedDevices) {
                this.devicesArray[index] = device;
                strings[index] = device.getName();
                index++;
            }
            ArrayAdapter<String> arrayAdapter = new ArrayAdapter<String>(getApplicationContext(), android.R.layout.simple_list_item_1, strings);
            device_list.setAdapter(arrayAdapter);
        }
    }

    private void listListeners() {
        device_list.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                BluetoothDevice deviceToConnect = devicesArray[position];
                BTConnectorClient connClient = BTConnectorClient.getInstance();
                boolean success = connClient.connectToServer(deviceToConnect);
                if (!success){
                    AlertDialog.Builder builder = new AlertDialog.Builder(context);
                    builder.setTitle("Connection Error")
                            .setMessage("Are you want to try agin?")
                            .setNegativeButton("No", new DialogInterface.OnClickListener() {
                                public void onClick(DialogInterface dialog, int which) {
                                    finish();
                                }
                            })
                            .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                                public void onClick(DialogInterface dialog, int which) {
                                }
                            })
                            //.setIcon(android.R.drawable.ic_dialog_alert)
                            .show();
                }
                else {
                    finish();
                }
            }
        });
    }
}
