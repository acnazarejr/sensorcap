package com.ssig.smartcap.wear_old;

import android.content.Intent;
import android.graphics.Color;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.wearable.activity.WearableActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import java.io.IOException;

import com.ssig.btmanager.BTConnectorClient;
import com.ssig.btmanager.BTMessage;

public class MainActivity extends WearableActivity {

    private TextView textStatus;
    private Button btConnect;
    private Button btCapture;
    private MessageControl messageControl;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Enables Always-on
        setAmbientEnabled();

        this.messageControl = null;

        textStatus = findViewById(R.id.textStatus);
        btConnect = findViewById(R.id.btConnect);
        btCapture = findViewById(R.id.btCapture);

        implementListeners();
    }

    @Override
    protected void onResume() {
        super.onResume();
        this.updateScreen();
    }

    private void implementListeners() {
        btConnect.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                BTConnectorClient connClient = BTConnectorClient.getInstance();
                if (!connClient.isConnected()){
                    Intent watchIntent = new Intent(MainActivity.this, ConnectionActivity.class);
                    startActivity(watchIntent);
                } else {
                    connClient.disconnect();
                    messageControl.cancel(true);
                    updateScreen();
                }
            }
        });
    }

    private void updateScreen() {
        BTConnectorClient connClient = BTConnectorClient.getInstance();
        boolean isConnected = connClient.isConnected();
        btConnect.setText(isConnected ? "Disconnect" : "Connect" );
        if(isConnected){
            textStatus.setText(connClient.getRemoteDevice().getName());
            textStatus.setTextColor(Color.GREEN);
        }else{
            textStatus.setText("Disconnected");
            textStatus.setTextColor(Color.RED);
        }
        btCapture.setEnabled(isConnected);

        if (messageControl == null && isConnected){
            messageControl = new MessageControl();
            messageControl.execute();
        }
    }


    private class MessageControl extends AsyncTask<Void, Void, Void> {

        @Override
        protected Void doInBackground(Void... voids) {
            BTConnectorClient connClient = BTConnectorClient.getInstance();
            while(true){

                Log.wtf("waiting", "waiting");

                try {
                    BTMessage m = connClient.readMessage();
                    String s = (String) m.getContent();
                    Log.wtf("teste", String.valueOf(s));
                } catch (IOException | ClassNotFoundException e) {
                    e.printStackTrace();
                }


                if (isCancelled())
                    return null;
            }
        }

    }

}
