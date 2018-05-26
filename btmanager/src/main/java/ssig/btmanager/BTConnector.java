package ssig.btmanager;

import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.UUID;

public abstract class BTConnector {

    private BluetoothSocket socket;
    private ObjectOutputStream outStream;
    private ObjectInputStream inStream;
    protected UUID btUUID = UUID.fromString("658fcda0-3433-11e8-b467-0ed5f89f718b");

    public BTConnector(){
        this.socket = null;
        this.outStream = null;
    }

    public abstract boolean disconnect();


    protected BluetoothSocket getSocket() {
        return this.socket;
    }

    protected void setSocket(BluetoothSocket socket) {
        this.socket = socket;
        try {
            this.outStream = new ObjectOutputStream(this.socket.getOutputStream());
            this.inStream = new ObjectInputStream(this.socket.getInputStream());
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public boolean isConnected() {
        return this.socket != null && this.socket.isConnected();
    }

    public BluetoothDevice getRemoteDevice() {
        if(this.socket == null)
            return null;
        return this.socket.getRemoteDevice();
    }

    public void writeMessage(Message message) throws IOException {
        this.outStream.writeObject(message);
    }

    public Message readMessage() throws IOException, ClassNotFoundException {
        Message message = (Message) this.inStream.readObject();
        return message;
    }

}
