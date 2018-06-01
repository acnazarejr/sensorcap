package com.ssig.sensorsmanager;

import java.io.Serializable;
import java.util.HashMap;

/**
 * Created by flabe on 01/06/2018.
 */

public class CaptureConfig implements Serializable {
    private PersonInfo person = null;
    private HashMap<SensorType, SensorConfig> sensors = new HashMap<SensorType, SensorConfig>();
    private String activity = null;
    private Integer delayStart = null;
    private Boolean bip = null;
    private Boolean vibration = null;
    public enum SmartphonePosition{
        FRONT, BACK;
    }
    public enum SmartphoneSide{
        LEFT, RIGHT;
    }
    public enum SmartwatchSide {
        LEFT, RIGHT;
    }

    public CaptureConfig(){

        //TODO//

    }

    public void addSensors(SensorConfig sensorConfig){
        sensors.put(sensorConfig.getSensortype(), sensorConfig);
    }

    public Boolean getVibration() {
        return vibration;
    }

    public void setVibration(Boolean vibration) {
        this.vibration = vibration;
    }

    public Boolean getBip() {
        return bip;
    }

    public Integer getDelayStart() {
        return delayStart;
    }

    public void setDelayStart(Integer delayStart) {
        this.delayStart = delayStart;
    }

    public String getActivity() {
        return activity;
    }

    public void setActivity(String activity) {
        this.activity = activity;
    }
}
