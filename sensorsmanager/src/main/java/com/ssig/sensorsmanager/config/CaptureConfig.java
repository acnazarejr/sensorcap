package com.ssig.sensorsmanager.config;

import com.ssig.sensorsmanager.SensorType;
import com.ssig.sensorsmanager.info.PersonInfo;

import java.io.Serializable;
import java.util.HashMap;
import java.util.UUID;


public class CaptureConfig implements Serializable {

    static final long serialVersionUID = 914562348956712378L;

    public enum SmartphoneLocation {
        FRONT_POCKET, BACK_POCKET, SHIRT_POCKET, BAG, OTHER
    }

    public enum SmartphoneSide{
        LEFT, RIGHT
    }

    public enum SmartwatchSide {
        LEFT, RIGHT
    }

    private UUID uuid;
    private String alias;
    private PersonInfo personInfo;
    private String activityName;
    private SmartphoneLocation smartphoneLocation;
    private SmartphoneSide smartphoneSide;
    private SmartwatchSide smartwatchSide;

    private boolean smartphoneEnabled;
    private boolean smartwatchEnabled;

    private HashMap<SensorType, SensorConfig> smartphoneSensors;
    private HashMap<SensorType, SensorConfig> smartwatchSensors;
    private Integer countdownStart;
    private Boolean hasSound;
    private Boolean hasVibration;

    public CaptureConfig(UUID uuid, String alias){
        this.uuid = (uuid == null) ? UUID.randomUUID() : uuid;
        this.alias = (alias == null) ? this.uuid.toString() : alias;
        this.personInfo = null;
        this.activityName = null;
        this.smartphoneLocation = null;
        this.smartphoneSide = null;
        this.smartwatchSide = null;
        this.smartphoneEnabled = false;
        this.smartwatchEnabled = false;
        this.smartphoneSensors = new HashMap<>();
        this.smartwatchSensors = new HashMap<>();
        this.countdownStart = 3;
        this.hasSound = false;
        this.hasVibration = false;
    }

    public UUID getUuid() {
        return uuid;
    }

    public String getAlias() {
        return alias;
    }

    public void setAlias(String alias) {
        this.alias = alias;
    }

    public PersonInfo getPersonInfo() {
        return personInfo;
    }

    public void setPersonInfo(PersonInfo personInfo) {
        this.personInfo = personInfo;
    }

    public String getActivityName() {
        return activityName;
    }

    public void setActivityName(String activityName) {
        this.activityName = activityName;
    }

    public SmartphoneLocation getSmartphoneLocation() {
        return smartphoneLocation;
    }

    public void setSmartphoneLocation(SmartphoneLocation smartphoneLocation) {
        this.smartphoneLocation = smartphoneLocation;
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


    public boolean isSmartphoneEnabled() {
        return smartphoneEnabled;
    }

    public void setSmartphoneEnabled(boolean smartphoneEnabled) {
        this.smartphoneEnabled = smartphoneEnabled;
    }

    public boolean isSmartwatchEnabled() {
        return smartwatchEnabled;
    }

    public void setSmartwatchEnabled(boolean smartwatchEnabled) {
        this.smartwatchEnabled = smartwatchEnabled;
    }


    public HashMap<SensorType, SensorConfig> getSmartphoneSensors() {
        return smartphoneSensors;
    }

    public void addSmartphoneSensor(SensorType sensorType, SensorConfig sensorConfig) {
        this.smartphoneSensors.put(sensorType, sensorConfig);
    }

    public SensorConfig getSmartphoneSensor(SensorType sensorType){
        return this.smartphoneSensors.get(sensorType);
    }

    public HashMap<SensorType, SensorConfig> getSmartwatchSensors() {
        return smartwatchSensors;
    }

    public void addSmartwatchSensor(SensorType sensorType, SensorConfig sensorConfig) {
        this.smartwatchSensors.put(sensorType, sensorConfig);
    }

    public SensorConfig getSmartwatchSensor(SensorType sensorType){
        return this.smartwatchSensors.get(sensorType);
    }


    public Integer getCountdownStart() {
        return countdownStart;
    }

    public void setCountdownStart(Integer countdownStart) {
        this.countdownStart = countdownStart;
    }

    public Boolean hasSound() {
        return hasSound;
    }

    public void setHasSound(Boolean hasSound) {
        this.hasSound = hasSound;
    }

    public Boolean hasVibration() {
        return hasVibration;
    }

    public void setHasVibration(Boolean hasVibration) {
        this.hasVibration = hasVibration;
    }

}
