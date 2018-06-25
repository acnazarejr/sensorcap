package com.ssig.sensorsmanager.config;

import java.io.Serializable;


public class SensorConfig implements Serializable {

    static final long serialVersionUID = 567123789145623489L;

    private final String sensorConfigUUID;
    private Boolean enabled;
    private Integer frequency;

    public SensorConfig(String sensorConfigUUID){
        this.sensorConfigUUID = sensorConfigUUID;
        this.enabled = null;
        this.frequency = null;
    }

    public String getSensorConfigUUID() {
        return sensorConfigUUID;
    }

    public Boolean isEnabled() {
        return enabled;
    }

    public void setEnabled(Boolean enabled) {
        this.enabled = enabled;
    }

    public Integer getFrequency() {
        return frequency;
    }

    public void setFrequency(Integer frequency) {
        this.frequency = frequency;
    }

}
