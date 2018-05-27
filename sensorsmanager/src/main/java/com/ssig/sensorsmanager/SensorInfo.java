package com.ssig.sensorsmanager;

import android.content.Context;
import android.content.pm.PackageManager;
import android.hardware.Sensor;
import android.hardware.SensorManager;

import java.io.Serializable;

public class SensorInfo implements Serializable{

    static final long serialVersionUID = 123456789123456789L;

    private SensorType sensorType;
    private int maxValue;
    private int minValue;
    private String vendor;
    private float resolution;

    public SensorInfo(SensorType sensorType){
        this.sensorType = sensorType;
        this.maxValue = 0;
        this.minValue = 0;
        this.vendor = null;
        this.resolution = 0;
    }


    public SensorType getSensorType() {
        return sensorType;
    }

    public void setSensorType(SensorType sensorType) {
        this.sensorType = sensorType;
    }

    public int getMaxValue() {
        return maxValue;
    }

    public void setMaxValue(int maxValue) {
        this.maxValue = maxValue;
    }

    public int getMinValue() {
        return minValue;
    }

    public void setMinValue(int minValue) {
        this.minValue = minValue;
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
}
