package com.ssig.smartcap.utils;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.PorterDuff;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.support.annotation.ColorInt;
import android.support.annotation.ColorRes;
import android.support.annotation.NonNull;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.Menu;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.LinearLayout;

import com.afollestad.materialdialogs.DialogAction;
import com.afollestad.materialdialogs.MaterialDialog;
import com.ssig.sensorsmanager.info.SensorInfo;
import com.ssig.sensorsmanager.SensorType;
import com.ssig.smartcap.R;
import com.ssig.smartcap.adapter.AdapterSensorsList;
import com.ssig.smartcap.model.SensorsListItem;
import com.ssig.smartcap.widget.LineItemDecoration;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;


public class Tools {

    public static void setSystemBarColor(Activity act, @ColorRes int color) {
        Window window = act.getWindow();
        window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
        window.clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
        window.setStatusBarColor(ContextCompat.getColor(act, color));
    }

    public static void setSystemBarLight(Activity act) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            View view = act.findViewById(android.R.id.content);
            int flags = view.getSystemUiVisibility();
            flags |= View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR;
            view.setSystemUiVisibility(flags);
        }
    }

    public static void toggleArrow(boolean show, View view) {
        toggleArrow(show, view, true);
    }

    public static void toggleArrow(boolean show, View view, boolean delay) {
        if (show) {
            view.animate().setDuration(delay ? 200 : 0).rotation(180);
        } else {
            view.animate().setDuration(delay ? 200 : 0).rotation(0);
        }
    }

    public static void changeMenuIconColor(Menu menu, @ColorInt int color) {
        for (int i = 0; i < menu.size(); i++) {
            Drawable drawable = menu.getItem(i).getIcon();
            if (drawable == null) continue;
            drawable.mutate();
            drawable.setColorFilter(color, PorterDuff.Mode.SRC_ATOP);
        }
    }

    public static Drawable changeDrawableColor(Drawable drawable, @ColorInt int color){
        drawable.mutate();
        drawable.setColorFilter(color, PorterDuff.Mode.SRC_ATOP);
        return drawable;
    }

    public static AdapterSensorsList populateSensorsList(Context context, RecyclerView recyclerView, String preferencesName, Map<SensorType, SensorInfo> sensors){

        SharedPreferences sharedPreferences = context.getSharedPreferences(preferencesName, Context.MODE_PRIVATE);

        List<SensorsListItem> items = new ArrayList<>();
        for (Map.Entry<SensorType, SensorInfo> entry : sensors.entrySet()) {
            SensorInfo sensorInfo = entry.getValue();
            if (sensorInfo != null) {
                SensorsListItem sensorsListItem = new SensorsListItem(sensorInfo);
                sensorsListItem.enabled = sharedPreferences.getBoolean(sensorsListItem.getSensorType().abbrev() + context.getString(R.string.preference_sensor_enabled_suffix), true);
                sensorsListItem.frequency = sharedPreferences.getInt(sensorsListItem.getSensorType().abbrev() + context.getString(R.string.preference_sensor_frequency_suffix), sensorsListItem.getDefaultFrequency());
                items.add(sensorsListItem);
            }
        }

        AdapterSensorsList adapterSensorsList = new AdapterSensorsList(context, items);
        recyclerView.setAdapter(adapterSensorsList);
        return adapterSensorsList;
    }

    public static void saveSensorsPreferences(Context context, AdapterSensorsList adapterSensorsList, String preferencesName){
        SharedPreferences sharedPreferences = Objects.requireNonNull(context).getSharedPreferences(preferencesName, Context.MODE_PRIVATE);
        List<SensorsListItem> sensorsSensorsListItems = adapterSensorsList.getSensorsListItems();
        SharedPreferences.Editor editor = sharedPreferences.edit();
        for(SensorsListItem sensorsListItem : sensorsSensorsListItems){
            editor.putBoolean(sensorsListItem.getSensorType().abbrev() + context.getString(R.string.preference_sensor_enabled_suffix), sensorsListItem.enabled);
            editor.putInt(sensorsListItem.getSensorType().abbrev() + context.getString(R.string.preference_sensor_frequency_suffix), sensorsListItem.frequency);
        }
        editor.apply();
    }

    public static void resetSensorsPreferences(Context context, final AdapterSensorsList adapterSensorsList){
        new MaterialDialog.Builder(context)
                .title(R.string.dialog_reset_defaults_title)
                .content(R.string.dialog_reset_defaults_content)
                .icon(Tools.changeDrawableColor(Objects.requireNonNull(context.getDrawable(R.drawable.ic_smartphone)), ContextCompat.getColor(context, R.color.colorPrimary)))
                .cancelable(true)
                .positiveText(R.string.button_yes)
                .onPositive(new MaterialDialog.SingleButtonCallback() {
                    @Override
                    public void onClick(@NonNull MaterialDialog dialog, @NonNull DialogAction which) {
                        List<SensorsListItem> sensorsSensorsListItems = adapterSensorsList.getSensorsListItems();
                        for(SensorsListItem sensorsListItem : sensorsSensorsListItems){
                            sensorsListItem.enabled = true;
                            sensorsListItem.frequency = sensorsListItem.getDefaultFrequency();
                        }
                        adapterSensorsList.notifyDataSetChanged();
                    }
                })
                .negativeText(R.string.button_no)
                .show();
    }

}
