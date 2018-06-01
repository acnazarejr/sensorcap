package com.ssig.sensorsmanager;

import java.io.Serializable;

/**
 * Created by flabe on 01/06/2018.
 */

public class SensorConfig implements Serializable {
    private SensorType sensortype = null;
    private Boolean enabled = false;
    private Double frequency = 0.0;

    public SensorConfig(SensorType type, Boolean enable, Double freq){
        this.sensortype = type;
        this.enabled = enable;
        this.frequency = freq;
    }

    public SensorType getSensortype() {
        return sensortype;
    }

    public Boolean getEnabled() {
        return enabled;
    }

    public Double getFrequency() {
        return frequency;
    }
}
