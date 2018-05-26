package ssig.btmanager;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothServerSocket;

import java.io.IOException;


public class BTConnectorServer extends BTConnector {

    private static final BTConnectorServer ourInstance = new BTConnectorServer();
    private BluetoothAdapter bluetoothAdapter;


    public static BTConnectorServer getInstance() {
        return ourInstance;
    }


    private BTConnectorServer() {
        this.bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
    }

    public void listen(){
        try {
            BluetoothServerSocket serverSocket = this.bluetoothAdapter.listenUsingRfcommWithServiceRecord("APP_NAME", btUUID);
            this.setSocket(serverSocket.accept());
        } catch (IOException e) {
            e.printStackTrace();
        }
    }


    @Override
    public boolean disconnect() {
        return false;
    }


}
