package com.ssig.sensorsmanager;

import java.io.Serializable;

public class SensorInfo implements Serializable{

    static final long serialVersionUID = 123456789123456789L;

    protected SensorType sensorType;
    protected String name;
    protected String vendor;
    protected int version;
    protected float power;
    protected float maximunRange;
    protected int maxDelay;
    protected int minDelay;
    protected float resolution;


    public SensorInfo(SensorType sensorType){
        this.sensorType = sensorType;
        this.name = null;
        this.vendor = null;
        this.version = -1;
        this.maximunRange = -1;
        this.maxDelay = -1;
        this.minDelay = -1;
        this.resolution = -1;
    }

    public SensorType getSensorType() {
        return sensorType;
    }

    public void setSensorType(SensorType sensorType) {
        this.sensorType = sensorType;
    }

    public float getMaximunRange() {
        return maximunRange;
    }

    public void setMaximunRange(float maximunRange) {
        this.maximunRange = maximunRange;
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

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
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
}
