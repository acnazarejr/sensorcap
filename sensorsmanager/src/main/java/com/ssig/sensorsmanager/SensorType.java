package com.ssig.sensorsmanager;

import android.annotation.SuppressLint;
import android.hardware.Sensor;

import java.util.HashMap;
import java.util.Map;

public enum SensorType {

    TYPE_ACCELEROMETER("ACC"),
    TYPE_GRAVITY("GVT"),
    TYPE_GYROSCOPE("GYR"),
    TYPE_GYROSCOPE_UNCALIBRATED("GYU"),
    TYPE_LINEAR_ACCELERATION("LAC"),
    TYPE_ROTATION_VECTOR("ROT"),
    TYPE_STEP_DETECTOR("STP"),
    TYPE_GAME_ROTATION_VECTOR("GRT"),
    TYPE_GEOMAGNETIC_ROTATION_VECTOR("MRT"),
    TYPE_MAGNETIC_FIELD("MAG"),
    TYPE_MAGNETIC_FIELD_UNCALIBRATED("MAU"),
    TYPE_PRESSURE("BAR"),
    TYPE_HEART_RATE("HTR"),
    TYPE_PROXIMITY("PRX"),
    TYPE_LIGHT("LHT");

    public enum SensorGroup {
        MOTION, POSITION, ENVIRONMENT
    }

    public enum DeviceType {
        SMARTPHONE, SMARTWATCH
    }

    public enum DeviceLocation {
        FRONT_POCKET, BACK_POCKET, SHIRT_POCKET, BAG, WRIST, OTHER
    }

    public enum DeviceSide{
        LEFT, RIGHT
    }

    private final String code;

    SensorType(String code) {
        this.code = code;
    }

    public String code() {
        return code;
    }

    public static SensorType fromCode(String code) {
        for(SensorType type : SensorType.values()) {
            String typecode = type.code();
            if(code.equals(type.code())) {
                return type;
            }
        }
        return null;
    }


    public Integer androidType() {

        @SuppressLint("UseSparseArrays") Map<String, Integer> codeMap = new HashMap<>();

        codeMap.put(SensorType.TYPE_ACCELEROMETER.code(),               Sensor.TYPE_ACCELEROMETER);
        codeMap.put(SensorType.TYPE_GRAVITY.code(),                     Sensor.TYPE_GRAVITY);
        codeMap.put(SensorType.TYPE_GYROSCOPE.code(),                   Sensor.TYPE_GYROSCOPE);
        codeMap.put(SensorType.TYPE_GYROSCOPE_UNCALIBRATED.code(),      Sensor.TYPE_GYROSCOPE_UNCALIBRATED);
        codeMap.put(SensorType.TYPE_LINEAR_ACCELERATION.code(),         Sensor.TYPE_LINEAR_ACCELERATION);
        codeMap.put(SensorType.TYPE_ROTATION_VECTOR.code(),             Sensor.TYPE_ROTATION_VECTOR);
        codeMap.put(SensorType.TYPE_STEP_DETECTOR.code(),               Sensor.TYPE_STEP_DETECTOR);
        codeMap.put(SensorType.TYPE_GAME_ROTATION_VECTOR.code(),        Sensor.TYPE_GAME_ROTATION_VECTOR);
        codeMap.put(SensorType.TYPE_GEOMAGNETIC_ROTATION_VECTOR.code(), Sensor.TYPE_GEOMAGNETIC_ROTATION_VECTOR);
        codeMap.put(SensorType.TYPE_MAGNETIC_FIELD.code(),              Sensor.TYPE_MAGNETIC_FIELD);
        codeMap.put(SensorType.TYPE_MAGNETIC_FIELD_UNCALIBRATED.code(), Sensor.TYPE_MAGNETIC_FIELD_UNCALIBRATED);
        codeMap.put(SensorType.TYPE_PRESSURE.code(),                    Sensor.TYPE_PRESSURE);
        codeMap.put(SensorType.TYPE_HEART_RATE.code(),                  Sensor.TYPE_HEART_RATE);
        codeMap.put(SensorType.TYPE_PROXIMITY.code(),                   Sensor.TYPE_PROXIMITY);
        codeMap.put(SensorType.TYPE_LIGHT.code(),                       Sensor.TYPE_LIGHT);

        return codeMap.get(this.code);

    }

    public String toString() {

        @SuppressLint("UseSparseArrays") Map<String, String> codeMap = new HashMap<>();

        codeMap.put(SensorType.TYPE_ACCELEROMETER.code(),               "Accelerometer");
        codeMap.put(SensorType.TYPE_GRAVITY.code(),                     "Gravity");
        codeMap.put(SensorType.TYPE_GYROSCOPE.code(),                   "Gyroscope");
        codeMap.put(SensorType.TYPE_GYROSCOPE_UNCALIBRATED.code(),      "Gyroscope Uncalibrated");
        codeMap.put(SensorType.TYPE_LINEAR_ACCELERATION.code(),         "Linear Acceleration");
        codeMap.put(SensorType.TYPE_ROTATION_VECTOR.code(),             "Rotation Vector");
        codeMap.put(SensorType.TYPE_STEP_DETECTOR.code(),               "Step Detector");
        codeMap.put(SensorType.TYPE_GAME_ROTATION_VECTOR.code(),        "Game Rotation Vector");
        codeMap.put(SensorType.TYPE_GEOMAGNETIC_ROTATION_VECTOR.code(), "Geomagnetic Rotation Vector");
        codeMap.put(SensorType.TYPE_MAGNETIC_FIELD.code(),              "Magnetic Field");
        codeMap.put(SensorType.TYPE_MAGNETIC_FIELD_UNCALIBRATED.code(), "Magnetic Field Uncalibrated");
        codeMap.put(SensorType.TYPE_PRESSURE.code(),                    "Pressure");
        codeMap.put(SensorType.TYPE_HEART_RATE.code(),                  "Heart Rate");
        codeMap.put(SensorType.TYPE_PROXIMITY.code(),                   "Proximity");
        codeMap.put(SensorType.TYPE_LIGHT.code(),                       "Light");

        return codeMap.get(this.code);

    }


    public String unit() {

        @SuppressLint("UseSparseArrays") Map<String, String> codeMap = new HashMap<>();

        codeMap.put(SensorType.TYPE_ACCELEROMETER.code(),               "m/s²");
        codeMap.put(SensorType.TYPE_GRAVITY.code(),                     "m/s²");
        codeMap.put(SensorType.TYPE_GYROSCOPE.code(),                   "rad/s");
        codeMap.put(SensorType.TYPE_GYROSCOPE_UNCALIBRATED.code(),      "rad/s");
        codeMap.put(SensorType.TYPE_LINEAR_ACCELERATION.code(),         "m/s²");
        codeMap.put(SensorType.TYPE_ROTATION_VECTOR.code(),             null);
        codeMap.put(SensorType.TYPE_STEP_DETECTOR.code(),               null);
        codeMap.put(SensorType.TYPE_GAME_ROTATION_VECTOR.code(),        null);
        codeMap.put(SensorType.TYPE_GEOMAGNETIC_ROTATION_VECTOR.code(), null);
        codeMap.put(SensorType.TYPE_MAGNETIC_FIELD.code(),              "μT");
        codeMap.put(SensorType.TYPE_MAGNETIC_FIELD_UNCALIBRATED.code(), "μT");
        codeMap.put(SensorType.TYPE_PRESSURE.code(),                    "hPa");
        codeMap.put(SensorType.TYPE_HEART_RATE.code(),                  "BPM");
        codeMap.put(SensorType.TYPE_PROXIMITY.code(),                   "cm");
        codeMap.put(SensorType.TYPE_LIGHT.code(),                       "lx");

        return codeMap.get(this.code);

    }

    public SensorGroup group() {

        @SuppressLint("UseSparseArrays") Map<String, SensorGroup> codeMap = new HashMap<>();

        codeMap.put(SensorType.TYPE_ACCELEROMETER.code(),               SensorGroup.MOTION);
        codeMap.put(SensorType.TYPE_GRAVITY.code(),                     SensorGroup.MOTION);
        codeMap.put(SensorType.TYPE_GYROSCOPE.code(),                   SensorGroup.MOTION);
        codeMap.put(SensorType.TYPE_GYROSCOPE_UNCALIBRATED.code(),      SensorGroup.MOTION);
        codeMap.put(SensorType.TYPE_LINEAR_ACCELERATION.code(),         SensorGroup.MOTION);
        codeMap.put(SensorType.TYPE_ROTATION_VECTOR.code(),             SensorGroup.MOTION);
        codeMap.put(SensorType.TYPE_STEP_DETECTOR.code(),               SensorGroup.MOTION);
        codeMap.put(SensorType.TYPE_GAME_ROTATION_VECTOR.code(),        SensorGroup.POSITION);
        codeMap.put(SensorType.TYPE_GEOMAGNETIC_ROTATION_VECTOR.code(), SensorGroup.POSITION);
        codeMap.put(SensorType.TYPE_MAGNETIC_FIELD.code(),              SensorGroup.POSITION);
        codeMap.put(SensorType.TYPE_MAGNETIC_FIELD_UNCALIBRATED.code(), SensorGroup.POSITION);
        codeMap.put(SensorType.TYPE_PROXIMITY.code(),                   SensorGroup.POSITION);
        codeMap.put(SensorType.TYPE_PRESSURE.code(),                    SensorGroup.ENVIRONMENT);
        codeMap.put(SensorType.TYPE_HEART_RATE.code(),                  SensorGroup.ENVIRONMENT);
        codeMap.put(SensorType.TYPE_LIGHT.code(),                       SensorGroup.ENVIRONMENT);

        return codeMap.get(this.code);

    }

}
