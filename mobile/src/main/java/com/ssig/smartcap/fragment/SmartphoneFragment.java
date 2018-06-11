package com.ssig.smartcap.fragment;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.LinearLayout;


import com.ssig.sensorsmanager.info.SensorInfo;
import com.ssig.sensorsmanager.SensorType;
import com.ssig.smartcap.R;
import com.ssig.smartcap.adapter.AdapterSensorsList;
import com.ssig.smartcap.utils.Tools;
import com.ssig.smartcap.widget.LineItemDecoration;

import java.util.Map;
import java.util.Objects;

public class SmartphoneFragment extends AbstractMainFragment {

    private RecyclerView mRecyclerView;

    private AdapterSensorsList adapterSensorsList;

    public SmartphoneFragment(){
        super(R.layout.fragment_smartphone);
    }


    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        this.initUI();
    }

    @Override
    public void onHide() {
        super.onHide();
        Tools.saveSensorsPreferences(getContext(), this.adapterSensorsList, getString(R.string.preference_smartphone_file_id));
    }

    public AdapterSensorsList getAdapterSensorsList() {
        return adapterSensorsList;
    }

    public void initUI() {

        this.mRecyclerView = Objects.requireNonNull(this.getView()).findViewById(R.id.sensors_recycler_view);
        this.mRecyclerView.setLayoutManager(new LinearLayoutManager(this.getContext()));
        this.mRecyclerView.addItemDecoration(new LineItemDecoration(Objects.requireNonNull(this.getContext()), LinearLayout.VERTICAL));
        this.mRecyclerView.setHasFixedSize(true);

        Map<SensorType, SensorInfo> smartphoneSensors = SensorInfo.getAll(this.getContext());
        this.adapterSensorsList = Tools.populateSensorsList(getContext(), this.mRecyclerView, getString(R.string.preference_smartphone_file_id), smartphoneSensors);
        this.getView().findViewById(R.id.button_reset_defaults).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Tools.resetSensorsPreferences(getActivity(), adapterSensorsList);
            }
        });

    }
}
