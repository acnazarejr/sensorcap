package com.ssig.sensorsmanager.config;

import com.ssig.sensorsmanager.SensorType;

import java.io.Serializable;



public class SensorConfig implements Serializable {

    static final long serialVersionUID = 567123789145623489L;

    private SensorType sensorType;
    private Boolean enabled;
    private Integer frequency;

    public SensorConfig(SensorType type, Boolean enable, Integer frequency){
        this.sensorType = type;
        this.enabled = enable;
        this.frequency = frequency;
    }

    public SensorType getSensorType() {
        return sensorType;
    }

    public Boolean isEnabled() {
        return enabled;
    }

    public Integer getFrequency() {
        return frequency;
    }
}
