package com.ssig.smartcap.mobile.adapter;

import android.content.Context;
import android.graphics.Typeface;
import android.hardware.Sensor;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.RecyclerView;
import android.text.Html;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;


import com.github.ivbaranov.mli.MaterialLetterIcon;
import com.ssig.smartcap.mobile.R;
import com.ssig.smartcap.mobile.model.SensorListItem;
import com.ssig.smartcap.mobile.utils.Tools;
import com.ssig.smartcap.mobile.utils.ViewAnimation;
import com.warkiz.widget.IndicatorSeekBar;

import java.util.ArrayList;
import java.util.List;



public class AdapterListExpand extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    private List<SensorListItem> items = new ArrayList<>();


    private Context ctx;
    private OnItemClickListener mOnItemClickListener;

    public interface OnItemClickListener {
        void onItemClick(View view, SensorListItem obj, int position);
    }

    public void setOnItemClickListener(final OnItemClickListener mItemClickListener) {
        this.mOnItemClickListener = mItemClickListener;
    }

    public AdapterListExpand(Context context, List<SensorListItem> items) {
        this.items = items;
        ctx = context;
    }

    public class OriginalViewHolder extends RecyclerView.ViewHolder {
        public MaterialLetterIcon icon;
        public TextView title;
        public ImageButton bt_expand;
        public View lyt_expand;
        public View lyt_parent;
        public View lyt_title;

        public TextView sensorName;
        public TextView sensorVendor;
        public TextView sensorPower;
        public TextView sensorMaxRange;
        public TextView sensorResolution;
        public IndicatorSeekBar sensorDelay;


        public OriginalViewHolder(View v) {
            super(v);
            icon = v.findViewById(R.id.icon);
            title = v.findViewById(R.id.name);
            bt_expand = v.findViewById(R.id.bt_expand);
            lyt_expand = v.findViewById(R.id.lyt_expand);
            lyt_parent = v.findViewById(R.id.lyt_parent);
            lyt_title = v.findViewById(R.id.lyt_title);

            sensorName = v.findViewById(R.id.sensor_name);
//            sensorVersion = v.findViewById(R.id.sensor_version);
            sensorVendor = v.findViewById(R.id.sensor_vendor);
            sensorPower = v.findViewById(R.id.sensor_power);
            sensorMaxRange = v.findViewById(R.id.sensor_max_range);
            sensorResolution = v.findViewById(R.id.sensor_resolution);
            sensorDelay = v.findViewById(R.id.sensor_delay);
        }
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        RecyclerView.ViewHolder vh;
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_expand, parent, false);
        vh = new OriginalViewHolder(v);
        return vh;
    }

    // Replace the contents of a view (invoked by the layout manager)
    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder holder, final int position) {
        if (holder instanceof OriginalViewHolder) {
            final OriginalViewHolder view = (OriginalViewHolder) holder;

            final SensorListItem p = items.get(position);
            this.displayIcon(ctx, view.icon, p);
            view.title.setText(p.getSensorType().toString());

            view.sensorName.setText(p.getName() + " (" + "v" + String.valueOf(p.getVersion()) + ")");
            view.sensorVendor.setText(p.getVendor());

            view.sensorPower.setText(String.valueOf(p.getPower()) + "mA");
            String maxRange = String.valueOf(p.getMaximunRange());
            String sensorUnit = p.getSensorType().unit();
            if (sensorUnit != null){
                maxRange += " " + sensorUnit;
            }

            view.sensorMaxRange.setText(Html.fromHtml(maxRange));
            view.sensorResolution.setText(String.valueOf(p.getResolution()));


            if (p.getReportingMode() == Sensor.REPORTING_MODE_CONTINUOUS || p.getReportingMode() == Sensor.REPORTING_MODE_ON_CHANGE){
                view.sensorDelay.getBuilder()
                        .setMin(p.getMinFrequency())
                        .setMax(p.getMaxFrequency())
                        .setProgress(p.getDefaultFrequency())
                        .apply();
            }else{
                view.sensorDelay.setVisibility(View.GONE);
            }



            view.lyt_title.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    boolean show = toggleLayoutExpand(!p.expanded, view.bt_expand, view.lyt_expand);
                    items.get(position).expanded = show;
                }
            });

            view.bt_expand.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    boolean show = toggleLayoutExpand(!p.expanded, view.bt_expand, view.lyt_expand);
                    items.get(position).expanded = show;
                }
            });


            // void recycling view
            if(p.expanded){
                view.lyt_expand.setVisibility(View.VISIBLE);
            } else {
                view.lyt_expand.setVisibility(View.GONE);
            }
            Tools.toggleArrow(p.expanded, view.bt_expand, false);

        }
    }

    private void displayIcon(Context ctx, MaterialLetterIcon icon, SensorListItem sensorListItem){
//        icon.setInitials(false);
//        icon.setInitialsNumber(3);
        icon.setLetterSize(14);
        icon.setLetterTypeface(Typeface.defaultFromStyle(Typeface.BOLD));
        icon.setShapeColor(ContextCompat.getColor(ctx, R.color.green_600));
        icon.setLettersNumber(3);
        icon.setLetter(sensorListItem.getSensorType().abbrev());
    }

    private boolean toggleLayoutExpand(boolean show, View view, View lyt_expand) {
        Tools.toggleArrow(show, view);
        if (show) {
            ViewAnimation.expand(lyt_expand);
        } else {
            ViewAnimation.collapse(lyt_expand);
        }
        return show;
    }

    @Override
    public int getItemCount() {
        return items.size();
    }

}