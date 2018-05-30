package com.ssig.smartcap.mobile.adapter;

import android.content.Context;
import android.content.res.ColorStateList;
import android.graphics.Typeface;
import android.hardware.Sensor;
import android.support.v4.content.ContextCompat;
import android.support.v4.graphics.drawable.DrawableCompat;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.SwitchCompat;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CompoundButton;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.TextView;


import com.github.ivbaranov.mli.MaterialLetterIcon;
import com.ssig.smartcap.mobile.R;
import com.ssig.smartcap.mobile.model.SensorListItem;
import com.ssig.smartcap.mobile.utils.Tools;
import com.ssig.smartcap.mobile.utils.ViewAnimation;
import com.warkiz.widget.IndicatorSeekBar;

import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.util.List;



public class AdapterListSensor extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    public class SensorViewHolder extends RecyclerView.ViewHolder {

        public View layout;
        public View layoutExpand;
        public View layoutTitle;

        public MaterialLetterIcon icon;
        public TextView sensorType;
        public ImageButton buttonExpand;
        public SwitchCompat sensorEnabled;

        public TextView sensorModel;
        public TextView sensorVendor;
        public TextView sensorPower;
        public TextView sensorMaxRange;
        public TextView sensorResolution;
        public IndicatorSeekBar sensorFrequency;
        public TextView sensorMaxFrequency;
        public TextView sensorMinFrequency;
        public LinearLayout frequencyNotAvailable;
        public LinearLayout frequencyAvailable;
        public TextView frequencyNotAvailableText;


        public SensorViewHolder(View v) {
            super(v);

            layout = v.findViewById(R.id.lyt_parent);
            layoutTitle = v.findViewById(R.id.lyt_title);
            layoutExpand = v.findViewById(R.id.lyt_expand);

            icon = v.findViewById(R.id.icon);
            sensorType = v.findViewById(R.id.title);
            buttonExpand = v.findViewById(R.id.bt_expand);
            sensorEnabled = v.findViewById(R.id.sensor_enable);

            sensorModel = v.findViewById(R.id.sensor_model);
            sensorVendor = v.findViewById(R.id.sensor_vendor);
            sensorPower = v.findViewById(R.id.sensor_power);
            sensorMaxRange = v.findViewById(R.id.sensor_max_range);
            sensorResolution = v.findViewById(R.id.sensor_resolution);

            sensorFrequency = v.findViewById(R.id.sensor_frequency);
            sensorMaxFrequency = v.findViewById(R.id.sensor_max_frequency);
            sensorMinFrequency = v.findViewById(R.id.sensor_min_frequency);
            frequencyAvailable = v.findViewById(R.id.frequency_available);
            frequencyNotAvailable = v.findViewById(R.id.frequency_not_available);
            frequencyNotAvailableText = v.findViewById(R.id.frequency_not_available_text);
        }
    }


    private List<SensorListItem> sensorListItems;
    private Context ctx;
    private int color;

    public AdapterListSensor(Context context, List<SensorListItem> sensorListItems, int color) {
        this.sensorListItems = sensorListItems;
        this.ctx = context;
        this.color = color;
    }

    public List<SensorListItem> getSensorListItems() {
        return sensorListItems;
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        RecyclerView.ViewHolder viewHolder;
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_sensor, parent, false);
        viewHolder = new SensorViewHolder(view);
        return viewHolder;
    }

    // Replace the contents of a view (invoked by the layout manager)
    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder holder, final int position) {

        if (holder instanceof SensorViewHolder) {

            final SensorViewHolder view = (SensorViewHolder) holder;
            final SensorListItem sensorListItem = sensorListItems.get(position);

            // CONFIGURE TITLE LAYOUT
            this.displayTextIcon(ctx, view.icon, sensorListItem.getSensorType().abbrev(), sensorListItem.enabled);
            view.sensorType.setText(sensorListItem.getSensorType().toString());
            view.sensorEnabled.setChecked(sensorListItem.enabled);
            this.changeSwitchColor(view.sensorEnabled, this.color);

            // CONFIGURE EXPAND LAYOUT
            view.sensorModel.setText(sensorListItem.getModel() + " (" + "v" + String.valueOf(sensorListItem.getVersion()) + ")");
            view.sensorModel.setTextColor(ContextCompat.getColor(this.ctx, this.color));
            view.sensorVendor.setText(sensorListItem.getVendor());
            view.sensorVendor.setTextColor(ContextCompat.getColor(this.ctx, this.color));

            DecimalFormatSymbols decimalFormatSymbols = new DecimalFormatSymbols();
            decimalFormatSymbols.setDecimalSeparator('.');
            DecimalFormat decimalFormat = new DecimalFormat("0.00", decimalFormatSymbols);
            String maxRange = decimalFormat.format(sensorListItem.getMaximumRange());
            String sensorUnit = sensorListItem.getSensorType().unit();
            if (sensorUnit != null){
                maxRange += " " + sensorUnit;
            }
            view.sensorPower.setText(decimalFormat.format(sensorListItem.getPower()) + " mA");
            view.sensorMaxRange.setText(maxRange);
            view.sensorResolution.setText(String.valueOf(sensorListItem.getResolution()));

            if ((sensorListItem.getReportingMode() == Sensor.REPORTING_MODE_CONTINUOUS || sensorListItem.getReportingMode() == Sensor.REPORTING_MODE_ON_CHANGE) && (sensorListItem.getMaxFrequency() > sensorListItem.getMinFrequency()) ){
                view.sensorMinFrequency.setText(String.valueOf(sensorListItem.getMinFrequency())  + " Hz");
                view.sensorMaxFrequency.setText(String.valueOf(sensorListItem.getMaxFrequency())  + " Hz");
                view.sensorFrequency.getBuilder()
                        .setMin(sensorListItem.getMinFrequency())
                        .setMax(sensorListItem.getMaxFrequency())
                        .setProgress(sensorListItem.frequency)
                        .setIndicatorColor(ContextCompat.getColor(this.ctx, this.color))
                        .setThumbColor(ContextCompat.getColor(this.ctx, this.color))
                        .setProgressTrackColor(ContextCompat.getColor(this.ctx, this.color))
                        .apply();
            }else{
                view.frequencyAvailable.setVisibility(View.GONE);
                view.frequencyNotAvailable.setVisibility(View.VISIBLE);
                if (sensorListItem.getReportingMode() == Sensor.REPORTING_MODE_CONTINUOUS || sensorListItem.getReportingMode() == Sensor.REPORTING_MODE_ON_CHANGE){
                    view.frequencyNotAvailableText.setText("Fixed frequency of " + String.valueOf(sensorListItem.getMinFrequency()) + " Hz");
                } else {
                    view.frequencyNotAvailableText.setText("Not Available");
                }
            }



            view.layoutTitle.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    boolean show = toggleLayoutExpand(!sensorListItem.expanded, view.buttonExpand, view.layoutExpand);
                    sensorListItems.get(position).expanded = show;
                }
            });

            view.buttonExpand.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    boolean show = toggleLayoutExpand(!sensorListItem.expanded, view.buttonExpand, view.layoutExpand);
                    sensorListItems.get(position).expanded = show;
                }
            });

            view.sensorEnabled.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                @Override
                public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                    displayTextIcon(ctx, view.icon, sensorListItem.getSensorType().abbrev(), isChecked);
                    sensorListItems.get(position).enabled = isChecked;
                }
            });

            view.sensorFrequency.setOnSeekChangeListener(new IndicatorSeekBar.OnSeekBarChangeListener() {
                @Override
                public void onProgressChanged(IndicatorSeekBar seekBar, int progress, float progressFloat, boolean fromUserTouch) {
                    sensorListItems.get(position).frequency = progress;
                }

                @Override
                public void onSectionChanged(IndicatorSeekBar seekBar, int thumbPosOnTick, String textBelowTick, boolean fromUserTouch) {

                }

                @Override
                public void onStartTrackingTouch(IndicatorSeekBar seekBar, int thumbPosOnTick) {

                }

                @Override
                public void onStopTrackingTouch(IndicatorSeekBar seekBar) {

                }
            });


            // void recycling view
            if(sensorListItem.expanded){
                view.layoutExpand.setVisibility(View.VISIBLE);
            } else {
                view.layoutExpand.setVisibility(View.GONE);
            }
            Tools.toggleArrow(sensorListItem.expanded, view.buttonExpand, false);

//            setAnimation(holder.itemView, position);

        }
    }

//    private int lastPosition = -1;
//    private boolean on_attach = true;
//
//    @Override
//    public void onAttachedToRecyclerView(RecyclerView recyclerView) {
//        recyclerView.addOnScrollListener(new RecyclerView.OnScrollListener() {
//            @Override
//            public void onScrollStateChanged(RecyclerView recyclerView, int newState) {
//                on_attach = false;
//                super.onScrollStateChanged(recyclerView, newState);
//            }
//        });
//        super.onAttachedToRecyclerView(recyclerView);
//    }
//
//    private void setAnimation(View view, int position) {
//        if (position > lastPosition) {
//            ItemAnimation.animate(view, on_attach ? position : -1, ItemAnimation.FADE_IN);
//            lastPosition = position;
//        }
//    }

    private void changeSwitchColor(SwitchCompat switchCompat, int Color){
        int[][] states = new int[][] {
                new int[] {-android.R.attr.state_checked},
                new int[] {android.R.attr.state_checked},
        };

        int[] thumbColors = new int[] {
                ContextCompat.getColor(this.ctx, R.color.grey_800),
                ContextCompat.getColor(this.ctx, color),
        };

        int[] trackColors = new int[] {
                ContextCompat.getColor(this.ctx, R.color.grey_300),
                Tools.manipulateColor(ContextCompat.getColor(this.ctx, color), (float)1.5),
        };

        DrawableCompat.setTintList(DrawableCompat.wrap(switchCompat.getThumbDrawable()), new ColorStateList(states, thumbColors));
        DrawableCompat.setTintList(DrawableCompat.wrap(switchCompat.getTrackDrawable()), new ColorStateList(states, trackColors));
    }


    private void displayTextIcon(Context ctx, MaterialLetterIcon icon, String text, boolean enabled){
        icon.setLetterSize(14);
        icon.setLetterTypeface(Typeface.defaultFromStyle(Typeface.BOLD));
        if (enabled)
            icon.setShapeColor(ContextCompat.getColor(ctx, R.color.green_600));
        else
            icon.setShapeColor(ContextCompat.getColor(ctx, R.color.red_600));
        icon.setLettersNumber(3);
        icon.setLetter(text);
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
        return sensorListItems.size();
    }

}