package com.ssig.smartcap.mobile.fragment;


import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.ssig.smartcap.mobile.R;

public class SmartphoneDeviceFragment extends Fragment {

    public SmartphoneDeviceFragment() {
        Log.e("teste", "build");
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_smartphone_device, container, false);
    }

    @Override
    public void onResume() {
        super.onResume();
        this.getActivity();
        Log.e("teste", "here");
    }
}