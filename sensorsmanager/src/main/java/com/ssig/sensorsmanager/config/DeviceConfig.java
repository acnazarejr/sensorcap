package com.ssig.sensorsmanager.config;

import com.ssig.sensorsmanager.SensorType;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

public class DeviceConfig implements Serializable {

    static final long serialVersionUID = 56712229445623489L;


    private String deviceConfigUUID;
    private String captureConfigUUID;
    private String deviceKey;
    private SensorType.DeviceType deviceType;
    private Boolean enable;
    private SensorType.DeviceLocation deviceLocation;
    private SensorType.DeviceSide deviceSide;
    private Map<SensorType, SensorConfig> sensorsConfig;

    private Integer countdownStart;
    private Boolean sound;
    private Boolean vibration;

    public DeviceConfig(String deviceConfigUUID, String captureConfigUUID, String deviceKey, SensorType.DeviceType deviceType){
        this.deviceConfigUUID = deviceConfigUUID;
        this.captureConfigUUID = captureConfigUUID;
        this.deviceType = deviceType;
        this.deviceKey = deviceKey;
        this.enable = true;
        this.deviceLocation = null;
        this.deviceSide = null;
        this.sensorsConfig = new HashMap<>();

        this.countdownStart = 3;
        this.sound = false;
        this.vibration = false;
    }

    public String getDeviceConfigUUID() {
        return deviceConfigUUID;
    }

    public String getDeviceKey() {
        return deviceKey;
    }

    public SensorType.DeviceType getDeviceType() {
        return deviceType;
    }

    public Boolean isEnable() {
        return enable;
    }

    public void setEnable(Boolean enable) {
        this.enable = enable;
    }

    public SensorType.DeviceLocation getDeviceLocation() {
        return deviceLocation;
    }

    public void setDeviceLocation(SensorType.DeviceLocation deviceLocation) {
        this.deviceLocation = deviceLocation;
    }

    public SensorType.DeviceSide getDeviceSide() {
        return deviceSide;
    }

    public void setDeviceSide(SensorType.DeviceSide deviceSide) {
        this.deviceSide = deviceSide;
    }

    public SensorConfig getSensorConfig(SensorType sensorType){
        return sensorsConfig.get(sensorType);
    }

    public Map<SensorType, SensorConfig> getAllSensorsConfig() {
        return sensorsConfig;
    }

    public void putSensorConfig(SensorType sensorType, SensorConfig sensorConfig){
        sensorsConfig.put(sensorType, sensorConfig);
    }

    public Integer getCountdownStart() {
        return countdownStart;
    }

    public void setCountdownStart(Integer countdownStart) {
        this.countdownStart = countdownStart;
    }

    public Boolean isSound() {
        return sound;
    }

    public void setSound(Boolean sound) {
        this.sound = sound;
    }

    public Boolean isVibration() {
        return vibration;
    }

    public void setVibration(Boolean vibration) {
        this.vibration = vibration;
    }

    public String getCaptureConfigUUID() {
        return captureConfigUUID;
    }

}