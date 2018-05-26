package ssig.smartcapture;

import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import java.io.IOException;

import ssig.btmanager.BTConnectorServer;
import ssig.btmanager.Message;
import ssig.common.MessageType;

public class WatchManagerActivity extends AppCompatActivity {

    private Button btListenConnections, teste;
    private TextView textStatus, textConnectedDevice;
    private ProgressDialog listenDialog;
    private Runnable listenRunnable;
    private Thread listenThread;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_watch_manager);

        this.btListenConnections = findViewById(R.id.btListenConnections);
        this.teste = findViewById(R.id.test);
        this.textStatus = findViewById(R.id.textStatus);
        this.textConnectedDevice = findViewById(R.id.textConnectedDevice);


        this.listenRunnable = new Runnable() {
            @Override
            public void run() {
                BTConnectorServer connServer = BTConnectorServer.getInstance();
                connServer.listen();
                listenDialog.dismiss();
            }
        };

        this.listenThread = null;

        this.buttonListeners();
        this.createListenDialog();

    }

    @Override
    protected void onResume() {
        super.onResume();
        this.updateScreen();
    }

    private void buttonListeners() {

        this.btListenConnections.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                listenDialog.show();
                listenThread = new Thread(listenRunnable);
                listenThread.start();
            }
        });

        this.teste.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                try {
                    BTConnectorServer.getInstance().writeMessage(new Message(MessageType.REQUEST_SENSOR_INFO.code(), null));
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        });

    }

    private void createListenDialog() {

        this.listenDialog = new ProgressDialog(this);
        this.listenDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
        this.listenDialog.setMessage("Listen for Connections.");
        this.listenDialog.setButton(DialogInterface.BUTTON_NEGATIVE, "Cancel", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                listenThread.interrupt();
                listenThread = null;
                dialog.dismiss();
            }
        });
        this.listenDialog.setOnDismissListener(new DialogInterface.OnDismissListener() {
            @Override
            public void onDismiss(DialogInterface dialog) {
                updateScreen();
            }
        });
    }

    private void updateScreen() {
        BTConnectorServer connServer = BTConnectorServer.getInstance();
        boolean isConnected = connServer.isConnected();
        textStatus.setText(isConnected ? "Connected" : "Disconnected" );
        String device_text = isConnected ? connServer.getRemoteDevice().getName() : "None";
        textConnectedDevice.setText(device_text);
        btListenConnections.setEnabled(!isConnected);
    }


}
