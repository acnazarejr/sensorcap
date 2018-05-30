package com.ssig.smartcap.mobile.fragment;

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

    public SmartphoneFragment(){
        super("Smartphone Settings", R.drawable.ic_smartphone, R.color.smartphone, R.layout.fragment_smartphone);
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        parent_view = getActivity().findViewById(android.R.id.content);
    }


//    private void loadingAndDisplayContent() {
//        final LinearLayout layout_progress = getActivity().findViewById(R.id.layout_smartphone_progress);
//        final LinearLayout layout_content = getActivity().findViewById(R.id.layout_smartphone_content);
//        layout_progress.setVisibility(View.VISIBLE);
//        layout_progress.setAlpha(1.0f);
//        layout_content.setVisibility(View.GONE);
//
//        initComponent();
//        ViewAnimation.fadeOut(layout_progress);
//        ViewAnimation.fadeIn(layout_content);
//
////        new Handler().postDelayed(new Runnable() {
////            @Override
////            public void run() {
////                ViewAnimation.fadeOut(layout_progress);
////            }
////        }, LOADING_DURATION);
////
////        new Handler().postDelayed(new Runnable() {
////            @Override
////            public void run() {
////                initComponent();
////            }
////        }, LOADING_DURATION + 400);
//    }


    @Override
    public boolean makeContent() {
        recyclerView = (RecyclerView) getActivity().findViewById(R.id.sensorsRecyclerView);
        recyclerView.setLayoutManager(new LinearLayoutManager(this.getContext()));
        recyclerView.addItemDecoration(new LineItemDecoration(this.getContext(), LinearLayout.VERTICAL));
        recyclerView.setHasFixedSize(true);

        Map<SensorType, SensorInfo> sensors = SensorInfoFactory.getAllSensorInfo(this.getContext());
        List<SensorListItem> items = new ArrayList<>();


        for (Map.Entry<SensorType, SensorInfo> entry : sensors.entrySet()) {
            SensorInfo sensorInfo = entry.getValue();
            if (sensorInfo != null) {
                SensorListItem sensorListItem = new SensorListItem(sensorInfo);
                items.add(sensorListItem);
            }
        }

        //set data and list adapter
        adapterListSensor = new AdapterListSensor(this.getContext(), items, this.color);
        recyclerView.setAdapter(adapterListSensor);

        return true;

    }

    @Override
    public void onStart() {
        super.onStart();
        final LinearLayout layout_progress = getActivity().findViewById(R.id.layout_smartphone_progress);
        final LinearLayout layout_content = getActivity().findViewById(R.id.layout_smartphone_content);
        this.reload(layout_progress, layout_content, null);
    }

    @Override
    public void onStop() {
        super.onStop();
        Toast.makeText(getContext(), "STOP", Toast.LENGTH_LONG).show();
    }


    private void setTextViewDrawableColor(TextView textView) {
        for (Drawable drawable : textView.getCompoundDrawables()) {
            if (drawable != null) {
                drawable.setColorFilter(new PorterDuffColorFilter(ContextCompat.getColor(this.getContext(), R.color.teal_800), PorterDuff.Mode.SRC_IN));
            }
        }
    }

}
