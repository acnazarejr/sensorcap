package ssig.btmanager;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;

import java.io.IOException;
import java.util.Set;
import java.util.UUID;

public class ConnectionClient {

    private static final ConnectionClient ourInstance = new ConnectionClient();

    private BluetoothAdapter bluetoothAdapter;
    private BluetoothSocket socket;
    private BluetoothDevice connectedDevice;

    public static ConnectionClient getInstance() {
        return ourInstance;
    }

    private ConnectionClient() {
        this.bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        this.connectedDevice = null;
    }

    public Set<BluetoothDevice> listBondedDevices() {
        return  bluetoothAdapter.getBondedDevices();
    }

    public boolean connectToServer(BluetoothDevice btDevice) {
        UUID uuid = UUID.fromString("658fcda0-3433-11e8-b467-0ed5f89f718b");
        try {
            this.socket = btDevice.createRfcommSocketToServiceRecord(uuid);
            this.socket.connect();
            this.connectedDevice = btDevice;
            return true;
        } catch (IOException e) {
            return false;
        }
    }

    public boolean disconnect() {
        try {
            this.socket.close();
            return true;
        } catch (IOException e) {
            return false;
        }
    }

    public boolean isConnected() {
        return this.socket != null && this.socket.isConnected();
    }

    public BluetoothDevice getConnectedDevice() {
        return connectedDevice;
    }
}
