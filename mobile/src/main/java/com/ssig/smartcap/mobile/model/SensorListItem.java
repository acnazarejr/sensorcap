package com.ssig.smartcap.mobile.model;

import com.ssig.sensorsmanager.SensorInfo;

import java.io.Serializable;

public class SensorListItem extends SensorInfo implements Serializable {

    static final long serialVersionUID = 123456789123456789L;

    public boolean expanded = false;
    public boolean parent = false;
    public boolean enabled = true;
    public int frequency;

    // flag when item swiped
    public boolean swiped = false;

    public SensorListItem(SensorInfo sensorInfo) {
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
