package com.ssig.smartcap.fragment;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.LinearLayout;


import com.ssig.sensorsmanager.SensorInfo;
import com.ssig.sensorsmanager.SensorType;
import com.ssig.smartcap.R;
import com.ssig.smartcap.adapter.AdapterListSensor;
import com.ssig.smartcap.model.SensorListItem;
import com.ssig.smartcap.mobile.widget.LineItemDecoration;
import com.ssig.smartcap.utils.Tools;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;

public class SmartphoneFragment extends AbstractMainFragment {

    private AdapterListSensor adapterListSensor;

    public SmartphoneFragment(){
        super(R.layout.fragment_smartphone);
    }


    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        this.initUI();
    }


    public void initUI() {
        Map<SensorType, SensorInfo> smartphoneSensors = SensorInfo.getAll(this.getContext());
        RecyclerView recyclerView = this.getView().findViewById(R.id.sensors_recycler_view);
        this.adapterListSensor = Tools.populateSensorsList(getContext(), recyclerView, getString(R.string.preference_smartphone_file_id), smartphoneSensors);
        this.getView().findViewById(R.id.button_reset_defaults).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Tools.resetSensorsPreferences(getActivity(), adapterListSensor);
            }
        });
    }

    @Override
    public void onHide() {
        super.onHide();
        Tools.saveSensorsPreferences(getContext(), this.adapterListSensor, getString(R.string.preference_smartphone_file_id));
    }

}
