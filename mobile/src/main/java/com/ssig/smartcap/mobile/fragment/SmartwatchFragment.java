package com.ssig.smartcap.mobile.fragment;

import android.os.Bundle;
import android.view.View;
import android.widget.Toast;

import com.ssig.btmanager.BTConnectorServer;
import com.ssig.smartcap.mobile.R;
import com.vlonjatg.progressactivity.ProgressLayout;

public class SmartwatchFragment extends AbstractMainFragment {

    private ProgressLayout progressLayout;

    public SmartwatchFragment(){
        super("Smartwatch Settings", R.layout.fragment_smartwatch);
    }


    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        this.progressLayout = getActivity().findViewById(R.id.progressSmartwatchFragment);
        if (!BTConnectorServer.getInstance().isConnected()){
            progressLayout.showError(R.drawable.ic_bluetooth_disabled, "No Devices!",
                    "None wear device connected with this phone. To establish a new connection, start the listening for devices process.",
                    "Listen Devices", errorClickListener);
        }
    }

    private View.OnClickListener errorClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            Toast.makeText(getActivity().getApplication(), "Try again button clicked", Toast.LENGTH_LONG).show();
        }
    };

}
