package com.ssig.sensorsmanager;

import android.annotation.SuppressLint;
import android.hardware.Sensor;

import java.util.HashMap;
import java.util.Map;

public enum SensorType {

    TYPE_ACCELEROMETER(10),
    TYPE_GYROSCOPE(20), TYPE_GYROSCOPE_UNCALIBRATED(21),
    TYPE_MAGNETIC_FIELD(30), TYPE_MAGNETIC_FIELD_UNCALIBRATED(31),
    TYPE_PRESSURE(40),
    TYPE_HEART_RATE(50),
    TYPE_GRAVITY(101),
    TYPE_LINEAR_ACCELERATION(102),
    TYPE_ROTATION_VECTOR(103),
    TYPE_GAME_ROTATION_VECTOR(104),
    TYPE_GEOMAGNETIC_ROTATION_VECTOR(105),
    TYPE_STEP_DETECTOR(106),
    TYPE_PROXIMITY(107),
    TYPE_LIGHT(108);

    public enum SensorGroup {
        MOTION, POSITION, ENVIRONMENT
    }

    private final int code;

    SensorType(int code) {
        this.code = code;
    }

    public int code() {
        return code;
    }


    public Integer androidType() {

        @SuppressLint("UseSparseArrays") Map<Integer, Integer> codeMap = new HashMap<>();

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

        @SuppressLint("UseSparseArrays") Map<Integer, String> codeMap = new HashMap<>();

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

    public String abbrev() {

        @SuppressLint("UseSparseArrays") Map<Integer, String> codeMap = new HashMap<>();

        codeMap.put(SensorType.TYPE_ACCELEROMETER.code(),               "ACC");
        codeMap.put(SensorType.TYPE_GRAVITY.code(),                     "GVT");
        codeMap.put(SensorType.TYPE_GYROSCOPE.code(),                   "GYR");
        codeMap.put(SensorType.TYPE_GYROSCOPE_UNCALIBRATED.code(),      "GYU");
        codeMap.put(SensorType.TYPE_LINEAR_ACCELERATION.code(),         "LAC");
        codeMap.put(SensorType.TYPE_ROTATION_VECTOR.code(),             "ROT");
        codeMap.put(SensorType.TYPE_STEP_DETECTOR.code(),               "STP");
        codeMap.put(SensorType.TYPE_GAME_ROTATION_VECTOR.code(),        "GRT");
        codeMap.put(SensorType.TYPE_GEOMAGNETIC_ROTATION_VECTOR.code(), "MRT");
        codeMap.put(SensorType.TYPE_MAGNETIC_FIELD.code(),              "MAG");
        codeMap.put(SensorType.TYPE_MAGNETIC_FIELD_UNCALIBRATED.code(), "MAU");
        codeMap.put(SensorType.TYPE_PRESSURE.code(),                    "BAR");
        codeMap.put(SensorType.TYPE_HEART_RATE.code(),                  "HTR");
        codeMap.put(SensorType.TYPE_PROXIMITY.code(),                   "PRX");
        codeMap.put(SensorType.TYPE_LIGHT.code(),                       "LHT");

        return codeMap.get(this.code);

    }

    public String unit() {

        @SuppressLint("UseSparseArrays") Map<Integer, String> codeMap = new HashMap<>();

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

        @SuppressLint("UseSparseArrays") Map<Integer, SensorGroup> codeMap = new HashMap<>();

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
