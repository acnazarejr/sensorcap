package com.ssig.smartcap.mobile.fragment;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.res.TypedArray;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffColorFilter;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.os.Handler;
import android.support.design.widget.Snackbar;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.ssig.sensorsmanager.SensorInfo;
import com.ssig.sensorsmanager.SensorInfoFactory;
import com.ssig.sensorsmanager.SensorType;
import com.ssig.smartcap.mobile.R;
import com.ssig.smartcap.mobile.adapter.AdapterListSensor;
import com.ssig.smartcap.mobile.model.SensorListItem;
import com.ssig.smartcap.mobile.utils.ViewAnimation;
import com.ssig.smartcap.mobile.widget.LineItemDecoration;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class SmartphoneFragment extends AbstractMainFragment {

    private View parent_view;
    private final static int LOADING_DURATION = 3500;

    private RecyclerView recyclerView;
    private AdapterListSensor adapterListSensor;
    Map<SensorType, SensorInfo> smartphoneSensors;

    public SmartphoneFragment(){
        super(R.layout.fragment_smartphone);
    }

    @Override
    public void setViews() {
        this.progressView = getActivity().findViewById(R.id.layout_smartphone_progress);
        this.contentView = getActivity().findViewById(R.id.layout_smartphone_content);
        this.errorView = null;
    }

    @Override
    public String getTitle() {
        return "Capture Mode";
    }

    @Override
    public int getIcon() {
        return R.drawable.ic_running;
    }

    @Override
    public int getPrimaryColor() {
        return R.color.smartphone_primary;
    }

    @Override
    public int getSecondaryColor() {
        return R.color.smartphone_secondary;
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        parent_view = getActivity().findViewById(android.R.id.content);
        smartphoneSensors = SensorInfoFactory.getAllSensorInfo(this.getContext());
    }



    @Override
    public boolean makeContent() {
        recyclerView = getActivity().findViewById(R.id.sensorsRecyclerView);
        recyclerView.setLayoutManager(new LinearLayoutManager(this.getContext()));
        recyclerView.addItemDecoration(new LineItemDecoration(this.getContext(), LinearLayout.VERTICAL));
        recyclerView.setHasFixedSize(true);

        SharedPreferences sharedPreferences = getContext().getSharedPreferences("smartphone", Context.MODE_PRIVATE);

        Map<SensorType, SensorInfo> sensors = this.smartphoneSensors;
        List<SensorListItem> items = new ArrayList<>();

        for (Map.Entry<SensorType, SensorInfo> entry : sensors.entrySet()) {
            SensorInfo sensorInfo = entry.getValue();
            if (sensorInfo != null) {
                SensorListItem sensorListItem = new SensorListItem(sensorInfo);
                sensorListItem.enabled = sharedPreferences.getBoolean(sensorListItem.getSensorType().abbrev() + "_ENABLED", true);
                sensorListItem.frequency = sharedPreferences.getInt(sensorListItem.getSensorType().abbrev() + "_FREQUENCY", sensorListItem.getDefaultFrequency());
                items.add(sensorListItem);
            }
        }

        //set data and list adapter
        adapterListSensor = new AdapterListSensor(this.getContext(), items, this.getPrimaryColor());
        recyclerView.setAdapter(adapterListSensor);

        return true;

    }

    @Override
    public void onStop() {
        super.onStop();
        SharedPreferences sharedPreferences = getContext().getSharedPreferences("smartphone", Context.MODE_PRIVATE);
        List<SensorListItem> sensorsSensorListItems = this.adapterListSensor.getSensorListItems();
        SharedPreferences.Editor editor = sharedPreferences.edit();
        for(SensorListItem sensorListItem : sensorsSensorListItems){
            editor.putBoolean(sensorListItem.getSensorType().abbrev() + "_ENABLED", sensorListItem.enabled);
            editor.putInt(sensorListItem.getSensorType().abbrev() + "_FREQUENCY", sensorListItem.frequency);
        }
        editor.commit();
    }


//    private void setTextViewDrawableColor(TextView textView) {
//        for (Drawable drawable : textView.getCompoundDrawables()) {
//            if (drawable != null) {
//                drawable.setColorFilter(new PorterDuffColorFilter(ContextCompat.getColor(this.getContext(), R.color.teal_800), PorterDuff.Mode.SRC_IN));
//            }
//        }
//    }

}
