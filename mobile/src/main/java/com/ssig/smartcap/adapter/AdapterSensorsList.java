package com.ssig.smartcap.adapter;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Typeface;
import android.hardware.Sensor;
import android.support.annotation.NonNull;
import android.support.v4.content.ContextCompat;
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
import com.ssig.smartcap.R;
import com.ssig.smartcap.model.SensorsListItem;
import com.ssig.smartcap.utils.Tools;

import com.ssig.smartcap.utils.ViewAnimation;
import com.warkiz.widget.IndicatorSeekBar;

import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.util.List;



public class AdapterSensorsList extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    private class SensorsListItemViewHolder extends RecyclerView.ViewHolder {

        private View mContainerView;
        private View mLayoutExpand;
        private View mLayoutTitle;

        private MaterialLetterIcon mMaterialLetterIcon;
        private TextView mTextSensorType;
        private ImageButton mButtonExpand;
        private SwitchCompat mSwitchSensorEnabled;

        private TextView mTextSensorModel;
        private TextView mTextSensorVendor;
        private TextView mTextSensorPower;
        private TextView mTextSensorMaxRange;
        private TextView mTextSensorResolution;
        private IndicatorSeekBar mSeekSensorFrequency;
        private TextView mTextSensorMaxFrequency;
        private TextView mTextSensorMinFrequency;
        private LinearLayout mLayoutFrequencyNotAvailable;
        private LinearLayout mLayoutFrequencyAvailable;
        private TextView mTextFrequencyNotAvailableText;


        SensorsListItemViewHolder(View view) {
            super(view);

            mContainerView = view.findViewById(R.id.layout_parent);
            mLayoutTitle = view.findViewById(R.id.layout_title);
            mLayoutExpand = view.findViewById(R.id.layout_expand);

            mMaterialLetterIcon = view.findViewById(R.id.icon);
            mTextSensorType = view.findViewById(R.id.title);
            mButtonExpand = view.findViewById(R.id.buttom_expand);
            mSwitchSensorEnabled = view.findViewById(R.id.sensor_enable);

            mTextSensorModel = view.findViewById(R.id.sensor_model);
            mTextSensorVendor = view.findViewById(R.id.sensor_vendor);
            mTextSensorPower = view.findViewById(R.id.sensor_power);
            mTextSensorMaxRange = view.findViewById(R.id.sensor_max_range);
            mTextSensorResolution = view.findViewById(R.id.sensor_resolution);

            mSeekSensorFrequency = view.findViewById(R.id.sensor_frequency);
            mTextSensorMaxFrequency = view.findViewById(R.id.sensor_max_frequency);
            mTextSensorMinFrequency = view.findViewById(R.id.sensor_min_frequency);
            mLayoutFrequencyAvailable = view.findViewById(R.id.frequency_available);
            mLayoutFrequencyNotAvailable = view.findViewById(R.id.frequency_not_available);
            mTextFrequencyNotAvailableText = view.findViewById(R.id.frequency_not_available_text);
        }
    }

    private List<SensorsListItem> mSensorsListItems;
    private Context mContext;

    public AdapterSensorsList(Context context, List<SensorsListItem> mSensorsListItems) {
        this.mSensorsListItems = mSensorsListItems;
        this.mContext = context;
    }

    public List<SensorsListItem> getSensorsListItems() {
        return mSensorsListItems;
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        RecyclerView.ViewHolder viewHolder;
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_sensor, parent, false);
        viewHolder = new SensorsListItemViewHolder(view);
        return viewHolder;
    }

    // Replace the contents of a view (invoked by the layout manager)
    @SuppressLint("RecyclerView")
    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, final int position) {


        if (holder instanceof SensorsListItemViewHolder) {

            final SensorsListItemViewHolder view = (SensorsListItemViewHolder) holder;
            final SensorsListItem sensorsListItem = mSensorsListItems.get(position);

            // CONFIGURE TITLE LAYOUT
            this.displayTextIcon(view.mMaterialLetterIcon, sensorsListItem.getSensorType().abbrev(), sensorsListItem.enabled);
            view.mTextSensorType.setText(sensorsListItem.getSensorType().toString());
            view.mSwitchSensorEnabled.setChecked(sensorsListItem.enabled);

            // CONFIGURE EXPAND LAYOUT
            view.mTextSensorModel.setText(String.format("%s (v%s)", sensorsListItem.getModel(), String.valueOf(sensorsListItem.getVersion())));
            view.mTextSensorVendor.setText(sensorsListItem.getVendor());

            DecimalFormatSymbols decimalFormatSymbols = new DecimalFormatSymbols();
            decimalFormatSymbols.setDecimalSeparator('.');
            DecimalFormat twoDecimalFormat = new DecimalFormat("0.00", decimalFormatSymbols);
            DecimalFormat eightDecimalFormat = new DecimalFormat("0.00000000", decimalFormatSymbols);
            String maxRange = twoDecimalFormat.format(sensorsListItem.getMaximumRange());
            String sensorUnit = sensorsListItem.getSensorType().unit();
            if (sensorUnit != null){
                maxRange += " " + sensorUnit;
            }
            view.mTextSensorPower.setText(String.format("%s mA", twoDecimalFormat.format(sensorsListItem.getPower())));
            view.mTextSensorMaxRange.setText(maxRange);
            view.mTextSensorResolution.setText(eightDecimalFormat.format(sensorsListItem.getResolution()));

            if ((sensorsListItem.getReportingMode() == Sensor.REPORTING_MODE_CONTINUOUS || sensorsListItem.getReportingMode() == Sensor.REPORTING_MODE_ON_CHANGE) && (sensorsListItem.getMaxFrequency() > sensorsListItem.getMinFrequency()) ){
                view.mTextSensorMinFrequency.setText(String.format("%s Hz", String.valueOf(sensorsListItem.getMinFrequency())));
                view.mTextSensorMaxFrequency.setText(String.format("%s Hz", String.valueOf(sensorsListItem.getMaxFrequency())));
                view.mSeekSensorFrequency.getBuilder()
                        .setMin(sensorsListItem.getMinFrequency())
                        .setMax(sensorsListItem.getMaxFrequency())
                        .setProgress(sensorsListItem.frequency)
                        .apply();
            }else{
                view.mLayoutFrequencyAvailable.setVisibility(View.GONE);
                view.mLayoutFrequencyNotAvailable.setVisibility(View.VISIBLE);
                if (sensorsListItem.getReportingMode() == Sensor.REPORTING_MODE_CONTINUOUS || sensorsListItem.getReportingMode() == Sensor.REPORTING_MODE_ON_CHANGE){
                    view.mTextFrequencyNotAvailableText.setText(String.format("Fixed frequency of %s Hz", String.valueOf(sensorsListItem.getMinFrequency())));
                } else {
                    view.mTextFrequencyNotAvailableText.setText(((SensorsListItemViewHolder) holder).mContainerView.getResources().getString(R.string.sensor_frequency_not_available));
                }
            }



            view.mLayoutTitle.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    mSensorsListItems.get(position).expanded = toggleLayoutExpand(!sensorsListItem.expanded, view.mButtonExpand, view.mLayoutExpand);
                }
            });

            view.mButtonExpand.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    mSensorsListItems.get(position).expanded = toggleLayoutExpand(!sensorsListItem.expanded, view.mButtonExpand, view.mLayoutExpand);
                }
            });

            view.mSwitchSensorEnabled.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                @Override
                public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                    displayTextIcon(view.mMaterialLetterIcon, sensorsListItem.getSensorType().abbrev(), isChecked);
                    mSensorsListItems.get(position).enabled = isChecked;
                }
            });

            view.mSeekSensorFrequency.setOnSeekChangeListener(new IndicatorSeekBar.OnSeekBarChangeListener() {
                @Override
                public void onProgressChanged(IndicatorSeekBar seekBar, int progress, float progressFloat, boolean fromUserTouch) {
                    mSensorsListItems.get(position).frequency = progress;
                }

                @Override
                public void onSectionChanged(IndicatorSeekBar seekBar, int thumbPosOnTick, String textBelowTick, boolean fromUserTouch) {}

                @Override
                public void onStartTrackingTouch(IndicatorSeekBar seekBar, int thumbPosOnTick) {}

                @Override
                public void onStopTrackingTouch(IndicatorSeekBar seekBar) {}
            });


            // void recycling view
            if(sensorsListItem.expanded){
                view.mLayoutExpand.setVisibility(View.VISIBLE);
            } else {
                view.mLayoutExpand.setVisibility(View.GONE);
            }
            Tools.toggleArrow(sensorsListItem.expanded, view.mButtonExpand, false);

        }
    }

    @Override
    public int getItemCount() {
        return mSensorsListItems.size();
    }

    public void clear() {
        final int size = mSensorsListItems.size();
        mSensorsListItems.clear();
        notifyItemRangeRemoved(0, size);
    }

    private void displayTextIcon(MaterialLetterIcon icon, String text, boolean enabled){
        icon.setLetterSize(11);
        icon.setLetterTypeface(Typeface.defaultFromStyle(Typeface.BOLD));
        if (enabled)
            icon.setShapeColor(ContextCompat.getColor(this.mContext, R.color.colorPrimaryLight));
        else
            icon.setShapeColor(ContextCompat.getColor(this.mContext, R.color.colorGreyMediumLight));
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

}