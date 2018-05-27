package com.ssig.btmanager;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;

import java.io.IOException;
import java.util.Set;



public class BTConnectorClient extends BTConnector {

    private static final BTConnectorClient ourInstance = new BTConnectorClient();
    private BluetoothAdapter bluetoothAdapter;


    public static BTConnectorClient getInstance() {
        return ourInstance;
    }



    private BTConnectorClient() {
        this.bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
    }

    public Set<BluetoothDevice> listBondedDevices() {
        return  bluetoothAdapter.getBondedDevices();
    }

    public boolean connectToServer(BluetoothDevice btDevice) {
        try {
            BluetoothSocket socket = btDevice.createRfcommSocketToServiceRecord(this.btUUID);
            socket.connect();
            this.setSocket(socket);
            return true;
        } catch (IOException e) {
            return false;
        }
    }

    @Override
    public boolean disconnect() {
        try {
            this.getSocket().close();
            return true;
        } catch (IOException e) {
            return false;
        }
    }


}
