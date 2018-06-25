package com.ssig.sensorsmanager.data;

import com.ssig.sensorsmanager.SensorType;
import com.ssig.sensorsmanager.config.SensorConfig;
import com.ssig.sensorsmanager.info.SensorInfo;

import java.io.Serializable;

@SuppressWarnings("ALL")
public final class SensorData implements Serializable {

    static final long serialVersionUID = 961237891451623489L;

    private final String sensorDataUUID;
    private final SensorType sensorType;
    private String model;
    private String vendor;
    private int version;
    private float power;
    private float maximumRange;
    private int maxDelay;
    private int minDelay;
    private float resolution;
    private int reportingMode;
    private int valuesLength;
    private Integer frequency;
    private boolean enable;

    private SensorData(String sensorDataUUID, SensorType sensorType){
        this.sensorType = sensorType;
        this.sensorDataUUID = sensorDataUUID;
        this.frequency = -1;
        this.enable = false;
    }

    public SensorData(String sensorDataUUID, SensorInfo sensorInfo, SensorConfig sensorConfig){
        this(sensorDataUUID, sensorInfo.getSensorType());

        this.model = sensorInfo.getModel();
        this.vendor = sensorInfo.getVendor();
        this.version = sensorInfo.getVersion();
        this.power = sensorInfo.getPower();
        this.maximumRange = sensorInfo.getMaximumRange();
        this.maxDelay = sensorInfo.getMaxDelay();
        this.minDelay = sensorInfo.getMinDelay();
        this.resolution = sensorInfo.getResolution();
        this.reportingMode = sensorInfo.getReportingMode();
        this.valuesLength = sensorInfo.getValuesLength();

        this.frequency = sensorConfig.getFrequency();
        this.enable = sensorConfig.isEnabled();
    }

    public String getSensorDataUUID() {
        return sensorDataUUID;
    }

    public SensorType getSensorType() {
        return sensorType;
    }

    public boolean isEnable() {
        return enable;
    }
}
