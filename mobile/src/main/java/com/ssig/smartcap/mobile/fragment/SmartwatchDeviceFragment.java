package com.ssig.smartcap.mobile.fragment;


import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.ssig.smartcap.mobile.R;

/**
 * A simple {@link Fragment} subclass.
 */
public class SmartwatchDeviceFragment extends Fragment {


    public SmartwatchDeviceFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_smartwatch_device, container, false);
    }

}
