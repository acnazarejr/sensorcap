package com.ssig.sensorsmanager;

import android.content.Context;
import android.hardware.Sensor;
import android.hardware.SensorManager;

import java.util.HashMap;
import java.util.Map;

public class SensorInfoFactory {


    private SensorInfoFactory(){
    }

    public static SensorInfo getSensorInfo(Context context, SensorType sensorType){

        SensorManager sensorManager = (SensorManager)context.getSystemService(Context.SENSOR_SERVICE);

        Sensor defaultSensor = sensorManager.getDefaultSensor(sensorType.androidType());

        if (defaultSensor != null){
            SensorInfo sensorInfo = new SensorInfo((sensorType));
            sensorInfo.setName(defaultSensor.getName());
            sensorInfo.setVendor(defaultSensor.getVendor());
            sensorInfo.setVersion(defaultSensor.getVersion());
            sensorInfo.setPower(defaultSensor.getPower());
            sensorInfo.setMaximunRange(defaultSensor.getMaximumRange());
            sensorInfo.setMaxDelay(defaultSensor.getMaxDelay());
            sensorInfo.setMinDelay(defaultSensor.getMinDelay());
            sensorInfo.setResolution(defaultSensor.getResolution());
            return sensorInfo;
        }
        return null;
    }

    public static Map<SensorType, SensorInfo> getAllSensorInfo(Context context) {

        Map<SensorType, SensorInfo> allSensors = new HashMap<>();

        for(SensorType sensorType : SensorType.values()){
            allSensors.put(sensorType, SensorInfoFactory.getSensorInfo(context, sensorType));
        }

        return  allSensors;
    }

}
