package ssig.btmanager;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothServerSocket;
import android.bluetooth.BluetoothSocket;

import java.io.IOException;
import java.util.UUID;

public class ConnectionServer {

    private static final ConnectionServer ourInstance = new ConnectionServer();
    private BluetoothSocket socket;
    private BluetoothAdapter bluetoothAdapter;


    public static ConnectionServer getInstance() {
        return ourInstance;
    }



    private ConnectionServer() {
        this.bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
    }

    public void listen(){
        try {
            UUID uuid = UUID.fromString("658fcda0-3433-11e8-b467-0ed5f89f718b");
            BluetoothServerSocket serverSocket = this.bluetoothAdapter.listenUsingRfcommWithServiceRecord("APP_NAME", uuid);
            this.socket = serverSocket.accept();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public boolean isConnected() {
        return this.socket != null && this.socket.isConnected();
    }

    public BluetoothDevice getRemoteDevice() {
        return this.socket.getRemoteDevice();
    }

}
