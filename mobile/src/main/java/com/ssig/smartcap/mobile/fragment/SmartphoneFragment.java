package com.ssig.smartcap.mobile.fragment;

import android.content.res.TypedArray;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffColorFilter;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.support.design.widget.Snackbar;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.ssig.smartcap.mobile.R;
import com.ssig.smartcap.mobile.adapter.AdapterListExpand;
import com.ssig.smartcap.mobile.model.SensorListItem;
import com.ssig.smartcap.mobile.widget.LineItemDecoration;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class SmartphoneFragment extends AbstractMainFragment {

    private View parent_view;
    private RecyclerView recyclerView;
    private AdapterListExpand mAdapter;

    public SmartphoneFragment(){
        super("Smartphone Settings", R.layout.fragment_smartphone);
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        this.setTextViewDrawableColor((TextView) getActivity().findViewById(R.id.text_sensor_title));
        parent_view = getActivity().findViewById(android.R.id.content);
        initComponent();
    }


    private void initComponent() {
        recyclerView = (RecyclerView) getActivity().findViewById(R.id.sensorsRecyclerView);
        recyclerView.setLayoutManager(new LinearLayoutManager(this.getContext()));
        recyclerView.addItemDecoration(new LineItemDecoration(this.getContext(), LinearLayout.VERTICAL));
        recyclerView.setHasFixedSize(true);

        List<SensorListItem> items = new ArrayList<>();
        TypedArray drw_arr = this.getContext().getResources().obtainTypedArray(R.array.social_images);
        String name_arr[] = this.getContext().getResources().getStringArray(R.array.social_names);

        for (int i = 0; i < drw_arr.length(); i++) {
            SensorListItem obj = new SensorListItem();
            obj.image = drw_arr.getResourceId(i, -1);
            obj.name = name_arr[i];
            obj.imageDrw = this.getContext().getResources().getDrawable(obj.image);
            items.add(obj);
        }
        Collections.shuffle(items);

        //set data and list adapter
        mAdapter = new AdapterListExpand(this.getContext(), items);
        recyclerView.setAdapter(mAdapter);

        // on item list clicked
        mAdapter.setOnItemClickListener(new AdapterListExpand.OnItemClickListener() {
            @Override
            public void onItemClick(View view, SensorListItem obj, int position) {
                Snackbar.make(parent_view, "Item " + obj.name + " clicked", Snackbar.LENGTH_SHORT).show();
            }
        });

    }


    private void setTextViewDrawableColor(TextView textView) {
        for (Drawable drawable : textView.getCompoundDrawables()) {
            if (drawable != null) {
                drawable.setColorFilter(new PorterDuffColorFilter(ContextCompat.getColor(this.getContext(), R.color.teal_800), PorterDuff.Mode.SRC_IN));
            }
        }
    }

}
