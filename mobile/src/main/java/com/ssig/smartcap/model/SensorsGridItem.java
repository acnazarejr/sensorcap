package com.ssig.smartcap.model;

import com.ssig.sensorsmanager.SensorType;
import com.ssig.sensorsmanager.info.SensorInfo;

import java.io.Serializable;

public class SensorsGridItem extends SensorInfo implements Serializable {

    static final long serialVersionUID = 123456789123456789L;

    private final boolean valid ;
    private boolean enabled;
    private int frequency;

    public SensorsGridItem(SensorType sensorType) {
        super(sensorType);
        this.valid = false;
        this.enabled = false;
    }

    public SensorsGridItem(SensorInfo sensorInfo) {
        super(sensorInfo.getSensorType());
        this.valid = true;
        this.enabled = true;
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

    public boolean isValid() {
        return valid;
    }

    public boolean isEnabled() {
        return enabled;
    }

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }

    public int getFrequency() {
        return frequency;
    }

    public void setFrequency(int frequency) {
        this.frequency = frequency;
    }
}
