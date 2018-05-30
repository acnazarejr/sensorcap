package com.ssig.sensorsmanager;

import android.hardware.Sensor;

import java.io.Serializable;

public class SensorInfo implements Serializable{

    static final long serialVersionUID = 123456789123456789L;

    protected SensorType sensorType;
    protected String model;
    protected String vendor;
    protected int version;
    protected float power;
    protected float maximumRange;
    protected int maxDelay;
    protected int minDelay;
    protected float resolution;
    protected int reportingMode;


    public SensorInfo(SensorType sensorType){
        this.sensorType = sensorType;
        this.model = null;
        this.vendor = null;
        this.version = -1;
        this.power = -1;
        this.maximumRange = -1;
        this.maxDelay = -1;
        this.minDelay = -1;
        this.resolution = -1;
        this.reportingMode = -1;
    }

    public SensorType getSensorType() {
        return sensorType;
    }

    public float getMaximumRange() {
        return maximumRange;
    }

    public void setMaximumRange(float maximumRange) {
        this.maximumRange = maximumRange;
    }


    public String getVendor() {
        return vendor;
    }

    public void setVendor(String vendor) {
        this.vendor = vendor;
    }

    public float getResolution() {
        return resolution;
    }

    public void setResolution(float resolution) {
        this.resolution = resolution;
    }

    public String getModel() {
        return this.model;
    }

    public void setModel(String model) {
        this.model = model;
    }

    public int getVersion() {
        return version;
    }

    public void setVersion(int version) {
        this.version = version;
    }

    public float getPower() {
        return power;
    }

    public void setPower(float power) {
        this.power = power;
    }

    public int getMaxDelay() {
        return maxDelay;
    }

    public void setMaxDelay(int maxDelay) {
        this.maxDelay = maxDelay;
    }

    public int getMinDelay() {
        return minDelay;
    }

    public void setMinDelay(int minDelay) {
        this.minDelay = minDelay;
    }

    public int getMinFrequency() {
        if (reportingMode == Sensor.REPORTING_MODE_CONTINUOUS || reportingMode == Sensor.REPORTING_MODE_ON_CHANGE)
            return maxDelay>0?1000000/maxDelay:1;
        return -1;
    }

    public int getMaxFrequency() {
        if (reportingMode == Sensor.REPORTING_MODE_CONTINUOUS || reportingMode == Sensor.REPORTING_MODE_ON_CHANGE)
            return minDelay>0?1000000/minDelay:1;
        return -1;
    }

    public int getDefaultFrequency() {
        if (reportingMode == Sensor.REPORTING_MODE_CONTINUOUS || reportingMode == Sensor.REPORTING_MODE_ON_CHANGE)
            return (int)(0.9*getMaxFrequency());
        return -1;
    }

    public int getReportingMode() {
        return reportingMode;
    }

    public void setReportingMode(int reportingMode) {
        this.reportingMode = reportingMode;
    }

}
