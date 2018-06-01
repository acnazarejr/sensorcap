package com.ssig.smartcap.mobile.fragment;

import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import com.ssig.btmanager.BTConnectorServer;
import com.ssig.smartcap.mobile.R;
import com.vlonjatg.progressactivity.ProgressLayout;

public class SmartwatchFragment extends AbstractMainFragment {


    private Button buttonListenConnections;

    private ProgressDialog listenDialog;
    private Runnable listenRunnable;
    private Thread listenThread;

    public SmartwatchFragment(){
        super(R.layout.fragment_smartwatch);
    }


    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        buttonListenConnections = getActivity().findViewById(R.id.button_listen_connection);



        this.listenThread = null;
        this.listenRunnable = new Runnable() {
            @Override
            public void run() {
                BTConnectorServer connServer = BTConnectorServer.getInstance();
                connServer.listen();
                listenDialog.dismiss();
            }
        };
        this.registerListeners();
        this.createListenDialog();

    }

    @Override
    public void setViews() {
        this.progressView = getActivity().findViewById(R.id.layout_smartwatch_progress);
        this.contentView = getActivity().findViewById(R.id.layout_smartwatch_content);
        this.errorView = getActivity().findViewById(R.id.layout_smartwatch_error);
    }

    @Override
    public boolean makeContent() {
        if (!BTConnectorServer.getInstance().isConnected())
            return false;
        return true;
    }

    @Override
    public String getTitle() {
        return "Capture Mode";
    }

    @Override
    public int getIcon() {
        return R.drawable.ic_smartwatch;
    }

    @Override
    public int getPrimaryColor() {
        return R.color.smartwatch_primary;
    }

    @Override
    public int getSecondaryColor() {
        return R.color.smartwatch_secondary;
    }

    private void registerListeners() {

        this.buttonListenConnections.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                listenDialog.show();
                listenThread = new Thread(listenRunnable);
                listenThread.start();
            }
        });
    }

    private void createListenDialog() {

        this.listenDialog = new ProgressDialog(this.getContext());
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
                Toast.makeText(getContext(), "Cancel", Toast.LENGTH_LONG).show();
            }
        });
    }

}

