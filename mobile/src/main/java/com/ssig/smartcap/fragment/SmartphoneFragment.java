package com.ssig.smartcap.fragment;


import com.ssig.sensorsmanager.info.DeviceInfo;
import com.ssig.smartcap.R;

import java.util.Objects;

public class SmartphoneFragment extends AbstractDeviceFragment {

    public SmartphoneFragment(){
        super(R.layout.fragment_smartphone, R.string.preference_smartphone_file_id);
    }

    @Override
    protected void initUI() {
        DeviceInfo deviceInfo = DeviceInfo.get(this.getContext());
        this.initDeviceInfo(Objects.requireNonNull(this.getView()).findViewById(R.id.device_info_layout), deviceInfo);
        this.initDeviceSensors(this.getView().findViewById(R.id.available_sensors_layout));
        this.configureAvailableSensors(deviceInfo);
    }

    @Override
    public void refresh() {

    }

}
