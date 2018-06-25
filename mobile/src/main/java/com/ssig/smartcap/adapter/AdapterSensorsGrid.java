package com.ssig.smartcap.adapter;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.content.res.ColorStateList;
import android.hardware.Sensor;
import android.support.annotation.NonNull;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import com.afollestad.materialdialogs.DialogAction;
import com.afollestad.materialdialogs.MaterialDialog;
import com.ssig.smartcap.R;
import com.ssig.smartcap.model.SensorsGridItem;
import com.warkiz.widget.IndicatorSeekBar;

import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.util.List;
import java.util.Objects;


public class AdapterSensorsGrid extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    private class SensorsListItemViewHolder extends RecyclerView.ViewHolder {

        private final View viewItemIcon;
        private final TextView textItemTitle;
        private final FloatingActionButton buttonItemIcon;

        SensorsListItemViewHolder(View view) {
            super(view);

            viewItemIcon = view.findViewById(R.id.sensor_grid_item_icon);
            textItemTitle = view.findViewById(R.id.sensor_grid_item_title);
            buttonItemIcon = viewItemIcon.findViewById(R.id.sensor_grid_item_icon_fab);
        }
    }

    private final List<SensorsGridItem> sensorsGridItems;
    private final Context context;
    private final SharedPreferences sharedPreferences;


    public AdapterSensorsGrid(Context context, List<SensorsGridItem> sensorsGridItems, String preferencesName) {
        this.sensorsGridItems = sensorsGridItems;
        this.context = context;
        this.sharedPreferences = Objects.requireNonNull(context).getSharedPreferences(preferencesName, Context.MODE_PRIVATE);
    }

    public List<SensorsGridItem> getSensorsGridItems() {
        return sensorsGridItems;
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        RecyclerView.ViewHolder viewHolder;
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.layout_sensor_grid_item, parent, false);
        viewHolder = new SensorsListItemViewHolder(view);
        return viewHolder;
    }

    @SuppressLint("RecyclerView")
    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, final int position) {


        if (holder instanceof SensorsListItemViewHolder) {

            final SensorsListItemViewHolder view = (SensorsListItemViewHolder) holder;
            final SensorsGridItem sensorsGridItem = sensorsGridItems.get(position);

            updateItemIcon(view.viewItemIcon, sensorsGridItem);
            view.textItemTitle.setText(sensorsGridItem.getSensorType().toString());

            if (sensorsGridItem.isValid()) {
                view.buttonItemIcon.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        makeSensorDetailsDialog(sensorsGridItem).show();
                    }
                });


                view.buttonItemIcon.setOnLongClickListener(new View.OnLongClickListener() {
                    @Override
                    public boolean onLongClick(View v) {
                        sensorsGridItem.setEnabled(!sensorsGridItem.isEnabled());
                        updateItemIcon(view.viewItemIcon, sensorsGridItem);

                        SharedPreferences.Editor editor = sharedPreferences.edit();
                        editor.putBoolean(sensorsGridItem.getSensorType().code() + context.getString(R.string.preference_sensor_enabled_suffix), sensorsGridItem.isEnabled());
                        editor.apply();

                        return true;
                    }
                });
            }

        }
    }

    @Override
    public int getItemCount() {
        return sensorsGridItems.size();
    }

//    public void clear() {
//        final int size = sensorsGridItems.size();
//        sensorsGridItems.clear();
//        notifyItemRangeRemoved(0, size);
//    }

    private void updateItemIcon(View viewItemIcon, SensorsGridItem sensorsGridItem){

        FloatingActionButton iconButton = viewItemIcon.findViewById(R.id.sensor_grid_item_icon_fab);
        int color = sensorsGridItem.isValid() ? (sensorsGridItem.isEnabled() ? R.color.colorPrimaryLight : R.color.colorGreyMediumLight) : R.color.colorAlert;
        iconButton.setBackgroundTintList(ColorStateList.valueOf(ContextCompat.getColor(this.context, color)));
        iconButton.setClickable(sensorsGridItem.isValid());
        iconButton.setFocusable(sensorsGridItem.isValid());

        TextView iconText = viewItemIcon.findViewById(R.id.sensor_grid_item_icon_text);
        iconText.setText(sensorsGridItem.getSensorType().code());
        if (!sensorsGridItem.isValid())
            iconText.setTextColor(ContextCompat.getColor(this.context, R.color.colorGreyLight));

    }

    private MaterialDialog makeSensorDetailsDialog(final SensorsGridItem sensorsGridItem){


        DecimalFormatSymbols decimalFormatSymbols = new DecimalFormatSymbols();
        decimalFormatSymbols.setDecimalSeparator('.');
        DecimalFormat twoDecimalFormat = new DecimalFormat("0.00", decimalFormatSymbols);
        DecimalFormat eightDecimalFormat = new DecimalFormat("0.00000000", decimalFormatSymbols);


        MaterialDialog materialDialog = new MaterialDialog.Builder(Objects.requireNonNull(this.context))
                .customView(R.layout.layout_device_dialog_sensor_details, true)
                .cancelable(true)
                .canceledOnTouchOutside(true)
                .positiveText(R.string.button_done)
                .onPositive(new MaterialDialog.SingleButtonCallback() {
                    @Override
                    public void onClick(@NonNull MaterialDialog dialog, @NonNull DialogAction which) {
                        dialog.dismiss();
                    }
                }).cancelListener(new DialogInterface.OnCancelListener() {
                    @Override
                    public void onCancel(DialogInterface dialog) {
                        dialog.dismiss();
                    }
                }).dismissListener(new DialogInterface.OnDismissListener() {
                    @Override
                    public void onDismiss(DialogInterface dialog) {
                        SharedPreferences.Editor editor = sharedPreferences.edit();
                        editor.putInt(sensorsGridItem.getSensorType().code() + context.getString(R.string.preference_sensor_frequency_suffix), sensorsGridItem.getFrequency());
                        editor.apply();
                    }
                })
                .build();

        View viewSensorDetails = Objects.requireNonNull(materialDialog.getCustomView());

        TextView textSensorName = viewSensorDetails.findViewById(R.id.sensor_name);
        TextView textSensorModel = viewSensorDetails.findViewById(R.id.sensor_model);
        TextView textSensorVendor = viewSensorDetails.findViewById(R.id.sensor_vendor);
        TextView textSensorVersion = viewSensorDetails.findViewById(R.id.sensor_version);
        TextView textSensorPower = viewSensorDetails.findViewById(R.id.sensor_power);
        TextView textSensorMaxRange = viewSensorDetails.findViewById(R.id.sensor_max_range);
        TextView textSensorResolution = viewSensorDetails.findViewById(R.id.sensor_resolution);

        String sensorUnit = sensorsGridItem.getSensorType().unit();
        String maxRange = String.format("%s %s", twoDecimalFormat.format(sensorsGridItem.getMaximumRange()), sensorUnit != null ? sensorUnit : "");

        textSensorName.setText(sensorsGridItem.getSensorType().toString());
        textSensorModel.setText(sensorsGridItem.getModel());
        textSensorVendor.setText(sensorsGridItem.getVendor());
        textSensorVersion.setText(String.format("v%s", String.valueOf(sensorsGridItem.getVersion())));
        textSensorPower.setText(String.format("%s mA", twoDecimalFormat.format(sensorsGridItem.getPower())));
        textSensorMaxRange.setText(maxRange);
        textSensorResolution.setText(eightDecimalFormat.format(sensorsGridItem.getResolution()));


        TextView textSensorMinFrequency = viewSensorDetails.findViewById(R.id.sensor_min_frequency);
        TextView textSensorMaxFrequency = viewSensorDetails.findViewById(R.id.sensor_max_frequency);
        IndicatorSeekBar seekSensorFrequency = viewSensorDetails.findViewById(R.id.sensor_frequency);
        View viewFrequencyNotAvailable = viewSensorDetails.findViewById(R.id.frequency_not_available);
        View viewFrequencyAvailable = viewSensorDetails.findViewById(R.id.frequency_available);
        TextView textFrequencyNotAvailableText = viewSensorDetails.findViewById(R.id.frequency_not_available_text);

        if ((sensorsGridItem.getReportingMode() == Sensor.REPORTING_MODE_CONTINUOUS || sensorsGridItem.getReportingMode() == Sensor.REPORTING_MODE_ON_CHANGE) && (sensorsGridItem.getMaxFrequency() > sensorsGridItem.getMinFrequency()) ){
            textSensorMinFrequency.setText(String.format("%s Hz", String.valueOf(sensorsGridItem.getMinFrequency())));
            textSensorMaxFrequency.setText(String.format("%s Hz", String.valueOf(sensorsGridItem.getMaxFrequency())));
            seekSensorFrequency.getBuilder()
                    .setMin(sensorsGridItem.getMinFrequency())
                    .setMax(sensorsGridItem.getMaxFrequency())
                    .setProgress(sensorsGridItem.getFrequency())
                    .apply();
            seekSensorFrequency.setOnSeekChangeListener(new IndicatorSeekBar.OnSeekBarChangeListener() {
                @Override
                public void onProgressChanged(IndicatorSeekBar seekBar, int progress, float progressFloat, boolean fromUserTouch) {
                    sensorsGridItem.setFrequency(progress);
                }

                @Override
                public void onSectionChanged(IndicatorSeekBar seekBar, int thumbPosOnTick, String textBelowTick, boolean fromUserTouch) {}

                @Override
                public void onStartTrackingTouch(IndicatorSeekBar seekBar, int thumbPosOnTick) {}

                @Override
                public void onStopTrackingTouch(IndicatorSeekBar seekBar) {}
            });
        }else{
            viewFrequencyAvailable.setVisibility(View.GONE);
            viewFrequencyNotAvailable.setVisibility(View.VISIBLE);
            if (sensorsGridItem.getReportingMode() == Sensor.REPORTING_MODE_CONTINUOUS || sensorsGridItem.getReportingMode() == Sensor.REPORTING_MODE_ON_CHANGE){
                textFrequencyNotAvailableText.setText(String.format("Fixed frequency of %s Hz", String.valueOf(sensorsGridItem.getMinFrequency())));
            } else {
                textFrequencyNotAvailableText.setText(this.context.getString(R.string.devices_sensor_details_frequency_not_available));
            }
        }


        return materialDialog;
    }

}