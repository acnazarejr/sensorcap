package com.ssig.sensorsmanager;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;


public class CaptureConfig implements Serializable{

    Map<SensorType, Integer> sensors;


    public CaptureConfig(){
        this.sensors = new HashMap<>();
        this.sensors.put(SensorType.ACC, null);
        this.sensors.put(SensorType.GYR, null);
        this.sensors.put(SensorType.BAR, null);
        this.sensors.put(SensorType.MAG, null);
        this.sensors.put(SensorType.LAC, null);
    }

    public void enableSensor(SensorType sensorType, int delay){
        this.sensors.put(sensorType, delay);
    }

    public void disableSensor(SensorType sensorType){
        this.sensors.put(sensorType, null);
    }

    public boolean isSensorEnable(SensorType sensorType){
        return this.sensors.get(sensorType) != null;
    }

    public Integer getSensorDelay(SensorType sensorType){
        if (this.isSensorEnable(sensorType))
            return this.sensors.get(sensorType);
        return null;
    }

}
