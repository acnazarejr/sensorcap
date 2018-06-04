package com.ssig.smartcap.fragment;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.LinearLayout;


import com.ssig.sensorsmanager.SensorInfo;
import com.ssig.sensorsmanager.SensorInfoFactory;
import com.ssig.sensorsmanager.SensorType;
import com.ssig.smartcap.R;
import com.ssig.smartcap.adapter.AdapterListSensor;
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
    public void onViewCreated(@NonNull View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        this.initUI();
    }

    @Override
    public void refresh() {

    }


    public void initUI() {

        Map<SensorType, SensorInfo> smartphoneSensors = SensorInfoFactory.getAllSensorInfo(this.getContext());
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

        //set data and list adapter
        this.adapterListSensor = new AdapterListSensor(this.getContext(), items);
        recyclerView.setAdapter(this.adapterListSensor);

    }

    @Override
    public void onStop() {
        super.onStop();
        SharedPreferences sharedPreferences = Objects.requireNonNull(getContext()).getSharedPreferences("smartphone", Context.MODE_PRIVATE);
        List<SensorListItem> sensorsSensorListItems = this.adapterListSensor.getSensorListItems();
        SharedPreferences.Editor editor = sharedPreferences.edit();
        for(SensorListItem sensorListItem : sensorsSensorListItems){
            editor.putBoolean(sensorListItem.getSensorType().abbrev() + "_ENABLED", sensorListItem.enabled);
            editor.putInt(sensorListItem.getSensorType().abbrev() + "_FREQUENCY", sensorListItem.frequency);
        }
        editor.apply();
    }


//    private void setTextViewDrawableColor(TextView textView) {
//        for (Drawable drawable : textView.getCompoundDrawables()) {
//            if (drawable != null) {
//                drawable.setColorFilter(new PorterDuffColorFilter(ContextCompat.getColor(this.getContext(), R.color.teal_800), PorterDuff.Mode.SRC_IN));
//            }
//        }
//    }

}
