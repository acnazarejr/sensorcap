package br.ufmg.dcc.ssig.sensorcap.fragment;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.SharedPreferences;
import android.hardware.Sensor;
import android.os.Bundle;
import android.support.annotation.LayoutRes;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.annotation.StringRes;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.AppCompatImageButton;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.PopupMenu;
import android.support.v7.widget.RecyclerView;
import android.view.MenuItem;
import android.view.View;
import android.widget.RadioGroup;
import android.widget.TabHost;
import android.widget.TabWidget;
import android.widget.TextView;
import com.afollestad.materialdialogs.DialogAction;
import com.afollestad.materialdialogs.MaterialDialog;
import com.warkiz.widget.IndicatorSeekBar;

import br.ufmg.dcc.ssig.sensorsmanager.SensorType;
import br.ufmg.dcc.ssig.sensorsmanager.info.DeviceInfo;
import br.ufmg.dcc.ssig.sensorsmanager.info.SensorInfo;
import br.ufmg.dcc.ssig.sensorcap.R;
import br.ufmg.dcc.ssig.sensorcap.adapter.AdapterSensorsGrid;
import br.ufmg.dcc.ssig.sensorcap.model.SensorsGridItem;

import java.util.*;

@SuppressWarnings("WeakerAccess")
public abstract class AbstractDeviceFragment extends AbstractMainFragment {

    protected static final int FREQUENCY_MAXIMUM = 0;
    protected static final int FREQUENCY_MEDIUM = 1;
    protected static final int FREQUENCY_LOW = 2;
    protected static final int FREQUENCY_MINIMUM = 3;
    protected static final int FREQUENCY_INDIVIDUAL = 4;
    protected static final int FREQUENCY_GLOBAL = 5;

    private Map<SensorType.SensorGroup, AdapterSensorsGrid> adapterSensorsGridMap;
    private MaterialDialog dialogSensorOptions;
    @StringRes private final int preferencesFileName;

    private int sensorFrequencyConfig;
    private int sensorFrequencyGlobal;

    protected AbstractDeviceFragment(@LayoutRes int layout, @StringRes int preferencesFileName) {
        super(layout);
        this.clearAdapterSensorsGridMap();
        this.preferencesFileName = preferencesFileName;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        this.initUI();
        final SharedPreferences sharedPreferences = Objects.requireNonNull(getContext()).getSharedPreferences(getString(preferencesFileName), Context.MODE_PRIVATE);
        this.sensorFrequencyConfig = sharedPreferences.getInt(getString(R.string.preference_sensor_frequency_config_key), getResources().getInteger(R.integer.preference_sensor_frequency_config_default));
        this.sensorFrequencyGlobal = sharedPreferences.getInt(getString(R.string.preference_sensor_frequency_global_key), getResources().getInteger(R.integer.preference_sensor_frequency_global_default));
        this.initSensorOptions();
    }

    protected void clearAdapterSensorsGridMap(){
        if (this.adapterSensorsGridMap != null)
            this.adapterSensorsGridMap.clear();
        this.adapterSensorsGridMap = new HashMap<>();
    }

    public List<SensorsGridItem> getValidSensorGridItemList(){
        List<SensorsGridItem> sensorsGridItemList = new LinkedList<>();
        for (AdapterSensorsGrid adapterSensorsGrid : this.adapterSensorsGridMap.values()){
            for (SensorsGridItem sensorsGridItem : adapterSensorsGrid.getSensorsGridItems()){
                if(sensorsGridItem.isValid())
                    sensorsGridItemList.add(sensorsGridItem);
            }
        }
        return sensorsGridItemList;
    }


    @SuppressLint("DefaultLocale")
    protected void initDeviceInfo(View layoutDeviceInfo, DeviceInfo deviceInfo) {
        ((TextView)layoutDeviceInfo.findViewById(R.id.device_name_text)).setText(deviceInfo.getDeviceName());
        ((TextView)layoutDeviceInfo.findViewById(R.id.device_manufacturer_model_text)).setText(String.format("%s %s (%s)", deviceInfo.getManufacturer().toUpperCase(), deviceInfo.getMarketName(), deviceInfo.getModel()));
        ((TextView)layoutDeviceInfo.findViewById(R.id.android_version_text)).setText(String.format("%s (API %s)", deviceInfo.getAndroidVersion(), deviceInfo.getAndroidSDK()));
        ((TextView)layoutDeviceInfo.findViewById(R.id.device_uuid_text)).setText(deviceInfo.getDeviceKey());
    }

    protected void initDeviceSensors(View layoutAvailableSensors) {

        TabHost tabHostAvailableSensors = layoutAvailableSensors.findViewById(R.id.available_sensors_tab_host);
        tabHostAvailableSensors.setup();
        this.initTab(tabHostAvailableSensors, R.id.motion_sensors_tab, SensorType.SensorGroup.MOTION);
        this.initTab(tabHostAvailableSensors, R.id.position_sensors_tab, SensorType.SensorGroup.POSITION);
        this.initTab(tabHostAvailableSensors, R.id.environment_sensors_tab, SensorType.SensorGroup.ENVIRONMENT);

        TabWidget tabWidget = tabHostAvailableSensors.findViewById(android.R.id.tabs);
        for (int i = 0; i < tabWidget.getTabCount(); i++){
            View tabView = tabWidget.getChildTabViewAt(i);
            tabView.setPadding(0, 0, 0, 0);
            TextView textView = tabView.findViewById(android.R.id.title);
            textView.setTextSize(14);
        }

        final AppCompatImageButton frequencyMenuButton = layoutAvailableSensors.findViewById(R.id.sensor_options_button);
        frequencyMenuButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                PopupMenu popupMenu = new PopupMenu(Objects.requireNonNull(getContext()), frequencyMenuButton);
                popupMenu.inflate(R.menu.menu_sensors_options);
                popupMenu.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
                    @Override
                    public boolean onMenuItemClick(MenuItem item) {
                        int itemID = item.getItemId();
                        switch (itemID){
                            case R.id.device_option_frequencies:
                                dialogSensorOptions.show();
                                break;
                            case R.id.device_option_reset:
                                resetToDefaults();
                                break;
                        }
                        return true;
                    }
                });
                popupMenu.show();
            }
        });

    }

    protected void initTab(TabHost tabHostAvailableSensors, int tabLayout, SensorType.SensorGroup sensorGroup){

        String tabName = sensorGroup.name().toLowerCase();
        tabName = String.format("%s%s", tabName.substring(0, 1).toUpperCase(), tabName.substring(1));

        TabHost.TabSpec tabSpec = tabHostAvailableSensors.newTabSpec(tabName);
        tabSpec.setContent(tabLayout);
        tabSpec.setIndicator(tabName, ContextCompat.getDrawable(Objects.requireNonNull(this.getContext()), R.drawable.ic_smartwatch_off));
        tabHostAvailableSensors.addTab(tabSpec);

        RecyclerView recyclerView = Objects.requireNonNull(this.getView()).findViewById(tabLayout);
        recyclerView.setLayoutManager(new GridLayoutManager(this.getContext(), 3, GridLayoutManager.VERTICAL, false));
        recyclerView.setHasFixedSize(true);

    }

    protected void initSensorOptions(){
        this.dialogSensorOptions =  new MaterialDialog.Builder(Objects.requireNonNull(this.getContext()))
                .customView(R.layout.dialog_sensors_frequencies, true)
                .cancelable(true)
                .canceledOnTouchOutside(true)
                .positiveText(R.string.button_apply)
                .onPositive(new MaterialDialog.SingleButtonCallback() {
                    @Override
                    public void onClick(@NonNull MaterialDialog dialog, @NonNull DialogAction which) {

                        final SharedPreferences sharedPreferences = Objects.requireNonNull(getContext()).getSharedPreferences(getString(preferencesFileName), Context.MODE_PRIVATE);
                        SharedPreferences.Editor editor = sharedPreferences.edit();

                        View view = Objects.requireNonNull(dialog.getCustomView());
                        RadioGroup radioGroupFrequencies = view.findViewById(R.id.frequencies_radio_group);

                        switch (radioGroupFrequencies.getCheckedRadioButtonId()){
                            case R.id.frequency_maximum:
                                sensorFrequencyConfig = AbstractDeviceFragment.FREQUENCY_MAXIMUM;
                                break;
                            case R.id.frequency_medium:
                                sensorFrequencyConfig = AbstractDeviceFragment.FREQUENCY_MEDIUM;
                                break;
                            case R.id.frequency_low:
                                sensorFrequencyConfig = AbstractDeviceFragment.FREQUENCY_LOW;
                                break;
                            case R.id.frequency_minimum:
                                sensorFrequencyConfig = AbstractDeviceFragment.FREQUENCY_MINIMUM;
                                break;
                            case R.id.frequency_individual:
                                sensorFrequencyConfig = AbstractDeviceFragment.FREQUENCY_INDIVIDUAL;
                                break;
                            case R.id.frequency_global:
                                IndicatorSeekBar indicatorSeekBar = view.findViewById(R.id.sensor_global_frequency);
                                sensorFrequencyConfig = AbstractDeviceFragment.FREQUENCY_GLOBAL;
                                sensorFrequencyGlobal = indicatorSeekBar.getProgress();
                                break;
                        }

                        editor.putInt(getString(R.string.preference_sensor_frequency_config_key), sensorFrequencyConfig);
                        editor.putInt(getString(R.string.preference_sensor_frequency_global_key), sensorFrequencyGlobal);
                        editor.apply();

                        setSensorsFrequencies();

                    }
                })
                .neutralText(R.string.button_cancel)
                .onNeutral(new MaterialDialog.SingleButtonCallback() {
                    @Override
                    public void onClick(@NonNull MaterialDialog dialog, @NonNull DialogAction which) {
                        dialog.cancel();
                    }
                })
                .build();

        final View view = Objects.requireNonNull(this.dialogSensorOptions.getCustomView());
        RadioGroup radioGroupFrequencies = view.findViewById(R.id.frequencies_radio_group);

        radioGroupFrequencies.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(RadioGroup group, int checkedId) {
                if (checkedId == R.id.frequency_global){
                    view.findViewById(R.id.sensor_global_frequency_layout).setVisibility(View.VISIBLE);
                    IndicatorSeekBar indicatorSeekBar = view.findViewById(R.id.sensor_global_frequency);
                    indicatorSeekBar.setProgress(sensorFrequencyGlobal);
                }else{
                    view.findViewById(R.id.sensor_global_frequency_layout).setVisibility(View.GONE);
                }
            }
        });

        this.resetDialogSensorsFrequencies(radioGroupFrequencies);

    }

    protected void resetDialogSensorsFrequencies(RadioGroup radioGroupFrequencies){
        switch (this.sensorFrequencyConfig){
            case AbstractDeviceFragment.FREQUENCY_MAXIMUM:
                radioGroupFrequencies.check(R.id.frequency_maximum);
                break;
            case AbstractDeviceFragment.FREQUENCY_MEDIUM:
                radioGroupFrequencies.check(R.id.frequency_medium);
                break;
            case AbstractDeviceFragment.FREQUENCY_LOW:
                radioGroupFrequencies.check(R.id.frequency_low);
                break;
            case AbstractDeviceFragment.FREQUENCY_MINIMUM:
                radioGroupFrequencies.check(R.id.frequency_minimum);
                break;
            case AbstractDeviceFragment.FREQUENCY_INDIVIDUAL:
                radioGroupFrequencies.check(R.id.frequency_individual);
                break;
            case AbstractDeviceFragment.FREQUENCY_GLOBAL:
                radioGroupFrequencies.check(R.id.frequency_global);
                break;
        }
    }

    protected void configureAvailableSensors(DeviceInfo deviceInfo){
        this.configureRecyclerView((RecyclerView)Objects.requireNonNull(this.getView()).findViewById(R.id.motion_sensors_tab), deviceInfo, SensorType.SensorGroup.MOTION);
        this.configureRecyclerView((RecyclerView)Objects.requireNonNull(this.getView()).findViewById(R.id.position_sensors_tab), deviceInfo, SensorType.SensorGroup.POSITION);
        this.configureRecyclerView((RecyclerView)Objects.requireNonNull(this.getView()).findViewById(R.id.environment_sensors_tab), deviceInfo, SensorType.SensorGroup.ENVIRONMENT);
    }

    protected void configureRecyclerView(RecyclerView recyclerView, DeviceInfo deviceInfo, SensorType.SensorGroup sensorGroup){
        SharedPreferences sharedPreferences = Objects.requireNonNull(getContext()).getSharedPreferences(getString(preferencesFileName), Context.MODE_PRIVATE);
        List<SensorsGridItem> items = new ArrayList<>();
        for (Map.Entry<SensorType, SensorInfo> entry : deviceInfo.getSensorsInfo(sensorGroup).entrySet()) {
            SensorsGridItem sensorsGridItem;
            SensorInfo sensorInfo = entry.getValue();
            if (sensorInfo != null) {
                sensorsGridItem = new SensorsGridItem(sensorInfo);
                sensorsGridItem.setEnabled(sharedPreferences.getBoolean(sensorsGridItem.getSensorType().code() + getString(R.string.preference_sensor_enabled_suffix), true));
                sensorsGridItem.setFrequency(sharedPreferences.getInt(sensorsGridItem.getSensorType().code() + getString(R.string.preference_sensor_frequency_suffix), sensorsGridItem.getDefaultFrequency()));
                sensorsGridItem.setFrequencyEditable(this.sensorFrequencyConfig == AbstractDeviceFragment.FREQUENCY_INDIVIDUAL);
            } else {
                sensorsGridItem = new SensorsGridItem(entry.getKey());
            }
            items.add(sensorsGridItem);
        }
        Collections.sort(items, new Comparator<SensorsGridItem>() {
            @Override
            public int compare(SensorsGridItem o1, SensorsGridItem o2) {
                Boolean b1 = o1.isValid();
                Boolean b2 = o2.isValid();
                int comp = b1.compareTo(b2);
                if (comp != 0)
                    return -comp;
                return o1.getSensorType().toString().compareTo(o2.getSensorType().toString());
            }
        });

        AdapterSensorsGrid adapterSensorsGrid = new AdapterSensorsGrid(getContext(), items, getString(preferencesFileName));
        recyclerView.setAdapter(adapterSensorsGrid);
        this.adapterSensorsGridMap.put(sensorGroup, adapterSensorsGrid);
    }

    protected void setSensorsFrequencies(){
        SharedPreferences sharedPreferences = Objects.requireNonNull(getContext()).getSharedPreferences(getString(preferencesFileName), Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        for (AdapterSensorsGrid adapterSensorsGrid : this.adapterSensorsGridMap.values()){
            for (SensorsGridItem sensorsGridItem : adapterSensorsGrid.getSensorsGridItems()){
                if(sensorsGridItem.isValid()) {
                    if ((sensorsGridItem.getReportingMode() == Sensor.REPORTING_MODE_CONTINUOUS || sensorsGridItem.getReportingMode() == Sensor.REPORTING_MODE_ON_CHANGE) && (sensorsGridItem.getMaxFrequency() > sensorsGridItem.getMinFrequency())) {
                        int frequency;
                        sensorsGridItem.setFrequencyEditable(false);
                        switch (sensorFrequencyConfig){
                            case AbstractDeviceFragment.FREQUENCY_MAXIMUM:
                                frequency = sensorsGridItem.getMaxFrequency();
                                break;
                            case AbstractDeviceFragment.FREQUENCY_MEDIUM:
                                frequency = (int) (0.5 * sensorsGridItem.getMaxFrequency());
                                break;
                            case AbstractDeviceFragment.FREQUENCY_LOW:
                                frequency = (int) (0.25 * sensorsGridItem.getMaxFrequency());
                                break;
                            case AbstractDeviceFragment.FREQUENCY_MINIMUM:
                                frequency = sensorsGridItem.getMinFrequency();
                                break;
                            case AbstractDeviceFragment.FREQUENCY_GLOBAL:
                                frequency = Math.min(sensorsGridItem.getMaxFrequency(), sensorFrequencyGlobal);
                                break;
                            case AbstractDeviceFragment.FREQUENCY_INDIVIDUAL:
                                frequency = sensorsGridItem.getFrequency();
                                sensorsGridItem.setFrequencyEditable(true);
                                break;
                            default:
                                frequency = sensorsGridItem.getMaxFrequency();
                                break;

                        }
                        sensorsGridItem.setFrequency(frequency);
                        editor.putInt(sensorsGridItem.getSensorType().code() + getContext().getString(R.string.preference_sensor_frequency_suffix), sensorsGridItem.getFrequency());
                    }
                }
            }
            adapterSensorsGrid.notifyDataSetChanged();
        }
        editor.apply();
    }

    protected void resetToDefaults(){
        SharedPreferences sharedPreferences = Objects.requireNonNull(getContext()).getSharedPreferences(getString(preferencesFileName), Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();

        this.sensorFrequencyConfig = getResources().getInteger(R.integer.preference_sensor_frequency_config_default);
        this.sensorFrequencyGlobal = getResources().getInteger(R.integer.preference_sensor_frequency_global_default);
        editor.putInt(getString(R.string.preference_sensor_frequency_config_key), sensorFrequencyConfig);
        editor.putInt(getString(R.string.preference_sensor_frequency_global_key), sensorFrequencyGlobal);

        for (AdapterSensorsGrid adapterSensorsGrid : this.adapterSensorsGridMap.values()){
            for (SensorsGridItem sensorsGridItem : adapterSensorsGrid.getSensorsGridItems()){
                if(sensorsGridItem.isValid()) {
                    sensorsGridItem.setEnabled(true);
                    editor.putBoolean(sensorsGridItem.getSensorType().code() + getContext().getString(R.string.preference_sensor_enabled_suffix), sensorsGridItem.isEnabled());
                    if ((sensorsGridItem.getReportingMode() == Sensor.REPORTING_MODE_CONTINUOUS || sensorsGridItem.getReportingMode() == Sensor.REPORTING_MODE_ON_CHANGE) && (sensorsGridItem.getMaxFrequency() > sensorsGridItem.getMinFrequency())) {
                        sensorsGridItem.setFrequencyEditable(false);
                        sensorsGridItem.setFrequency(sensorsGridItem.getDefaultFrequency());
                        editor.putInt(sensorsGridItem.getSensorType().code() + getContext().getString(R.string.preference_sensor_frequency_suffix), sensorsGridItem.getFrequency());
                    }
                }
            }
            adapterSensorsGrid.notifyDataSetChanged();
        }
        editor.apply();

        final View view = Objects.requireNonNull(this.dialogSensorOptions.getCustomView());
        RadioGroup radioGroupFrequencies = view.findViewById(R.id.frequencies_radio_group);
        this.resetDialogSensorsFrequencies(radioGroupFrequencies);

    }

    protected abstract void initUI();


}
