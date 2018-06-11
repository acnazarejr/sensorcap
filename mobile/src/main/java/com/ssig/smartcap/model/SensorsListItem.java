package com.ssig.smartcap.model;

import com.ssig.sensorsmanager.info.SensorInfo;

import java.io.Serializable;

public class SensorsListItem extends SensorInfo implements Serializable {

    static final long serialVersionUID = 123456789123456789L;

    public boolean expanded = false;
    public boolean parent = false;
    public boolean enabled = true;
    public int frequency;

    public SensorsListItem(SensorInfo sensorInfo) {
        super(sensorInfo.getSensorType());
        this.sensorType = sensorInfo.getSensorType();
        this.model = sensorInfo.getModel();
        this.vendor = sensorInfo.getVendor();
        this.version = sensorInfo.getVersion();
        this.power = sensorInfo.getPower();
        this.maximumRange = sensorInfo.getMaximumRange();
        this.maxDelay = sensorInfo.getMaxDelay();
        this.minDelay = sensorInfo.getMinDelay();
        this.resolution = sensorInfo.getResolution();
        this.reportingMode = sensorInfo.getReportingMode();
        this.frequency = sensorInfo.getDefaultFrequency();
    }

}
