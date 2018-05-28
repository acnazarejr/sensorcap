package com.ssig.smartcap.mobile.model;

import android.graphics.drawable.Drawable;

import com.ssig.sensorsmanager.SensorInfo;

import java.io.Serializable;

public class SensorListItem extends SensorInfo implements Serializable {

    static final long serialVersionUID = 123456789123456789L;

    public int image;
    public Drawable imageDrw;
    public String name;
    public boolean expanded = false;
    public boolean parent = false;

    // flag when item swiped
    public boolean swiped = false;

    public SensorListItem(SensorInfo sensorInfo) {
        super(sensorInfo.getSensorType());
        this.sensorType = sensorInfo.getSensorType();
        this.name = sensorInfo.getName();
        this.vendor = sensorInfo.getVendor();
        this.version = sensorInfo.getVersion();
        this.power = sensorInfo.getPower();
        this.maximunRange = sensorInfo.getMaximunRange();
        this.maxDelay = sensorInfo.getMaxDelay();
        this.minDelay = sensorInfo.getMinDelay();
        this.resolution = sensorInfo.getResolution();
    }

}
