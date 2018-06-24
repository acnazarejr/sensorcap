package com.ssig.sensorsmanager.util;

import android.content.Context;
import android.hardware.Sensor;
import android.hardware.SensorManager;
import android.os.Build;

import com.ssig.sensorsmanager.SensorType;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

public class SensorsValuesLength {

    private final static Map<SensorType, Integer> lengths = new HashMap<>();


    private SensorsValuesLength() {}

    public static int get(Context context, SensorType sensorType){
        if (lengths.isEmpty()){
            SensorManager sensorManager = (SensorManager)context.getSystemService(Context.SENSOR_SERVICE);
            for (SensorType type : SensorType.values()){
                Sensor sensor = Objects.requireNonNull(sensorManager).getDefaultSensor(type.androidType());
                SensorsValuesLength.lengths.put(type, sensor != null ? SensorsValuesLength.queryValueLength(sensor) : 0);
            }
        }
        return SensorsValuesLength.lengths.get(sensorType);
    }

    private static Integer queryValueLength(Sensor sensor){
        Integer valuesLength = 0;
        for (Method method : Sensor.class.getDeclaredMethods()) {
            if (method.getName().equals("getMaxLengthValuesArray")) {
                method.setAccessible(true);
                try {
                    valuesLength = (Integer) method.invoke(sensor, sensor, Build.VERSION.SDK_INT);
                    break;
                } catch (IllegalAccessException | InvocationTargetException e) {
                    e.printStackTrace();
                }
            }
        }
        return valuesLength;
    }

}
