package com.ssig.sensorsmanager;

import java.io.Serializable;
import java.util.HashMap;

/**
 * Created by flabe on 01/06/2018.
 */

public class CaptureConfig implements Serializable {
    private PersonInfo person = null;
    private SmartphonePosition smartphonePosition = null;
    private SmartphoneSide smartphoneSide = null;
    private SmartwatchSide smartwatchSide = null;
    private HashMap<SensorType, SensorConfig> sensors = new HashMap<>();
    private String activity = null;
    private Integer delayStart = null;
    private Boolean bip = null;
    private Boolean vibration = null;

    public HashMap<SensorType, SensorConfig> getSensors() {
        return sensors;
    }

    public PersonInfo getPerson() {
        return person;
    }

    public enum SmartphonePosition{
        FRONT, BACK;
    }
    public enum SmartphoneSide{
        LEFT, RIGHT;
    }
    public enum SmartwatchSide {
        LEFT, RIGHT;
    }

    public CaptureConfig(PersonInfo p){
        this.person = p;
        //TODO//
    }

    public void addSensorConfig(SensorConfig sensorConfig){
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

    public SmartphonePosition getSmartphonePosition() {
        return smartphonePosition;
    }

    public void setSmartphonePosition(SmartphonePosition smartphonePosition) {
        this.smartphonePosition = smartphonePosition;
    }

    public SmartphoneSide getSmartphoneSide() {
        return smartphoneSide;
    }

    public void setSmartphoneSide(SmartphoneSide smartphoneSide) {
        this.smartphoneSide = smartphoneSide;
    }

    public SmartwatchSide getSmartwatchSide() {
        return smartwatchSide;
    }

    public void setSmartwatchSide(SmartwatchSide smartwatchSide) {
        this.smartwatchSide = smartwatchSide;
    }
}
