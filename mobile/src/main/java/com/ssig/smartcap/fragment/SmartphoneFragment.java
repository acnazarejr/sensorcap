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
import com.ssig.smartcap.common.Serialization;
import com.ssig.smartcap.model.SensorListItem;
import com.ssig.smartcap.mobile.widget.LineItemDecoration;

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
        RecyclerView recyclerView = Objects.requireNonNull(getActivity()).findViewById(R.id.sensorsRecyclerView);

        recyclerView.setLayoutManager(new LinearLayoutManager(this.getContext()));
        recyclerView.addItemDecoration(new LineItemDecoration(Objects.requireNonNull(this.getContext()), LinearLayout.VERTICAL));
        recyclerView.setHasFixedSize(true);

        SharedPreferences sharedPreferences = getContext().getSharedPreferences("smartphone", Context.MODE_PRIVATE);

        List<SensorListItem> items = new ArrayList<>();
        for (Map.Entry<SensorType, SensorInfo> entry : smartphoneSensors.entrySet()) {
            SensorInfo sensorInfo = entry.getValue();
            if (sensorInfo != null) {
                SensorListItem sensorListItem = new SensorListItem(sensorInfo);
                sensorListItem.enabled = sharedPreferences.getBoolean(sensorListItem.getSensorType().abbrev() + "_ENABLED", true);
                sensorListItem.frequency = sharedPreferences.getInt(sensorListItem.getSensorType().abbrev() + "_FREQUENCY", sensorListItem.getDefaultFrequency());
                items.add(sensorListItem);
            }
        }

        this.adapterListSensor = new AdapterListSensor(this.getContext(), items);
        recyclerView.setAdapter(this.adapterListSensor);

    }

    @Override
    public void onHide() {
        super.onHide();
        SharedPreferences sharedPreferences = Objects.requireNonNull(getContext()).getSharedPreferences("smartphone", Context.MODE_PRIVATE);
        List<SensorListItem> sensorsSensorListItems = this.adapterListSensor.getSensorListItems();
        SharedPreferences.Editor editor = sharedPreferences.edit();
        for(SensorListItem sensorListItem : sensorsSensorListItems){
            editor.putBoolean(sensorListItem.getSensorType().abbrev() + "_ENABLED", sensorListItem.enabled);
            editor.putInt(sensorListItem.getSensorType().abbrev() + "_FREQUENCY", sensorListItem.frequency);
        }
        editor.apply();
    }

}
