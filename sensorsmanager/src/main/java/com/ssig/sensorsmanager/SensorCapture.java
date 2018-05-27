//package com.example.flabe.featureimp;
//
//import android.app.Service;
//import android.content.Context;
//import android.content.Intent;
//import android.hardware.Sensor;
//import android.hardware.SensorEvent;
//import android.hardware.SensorEventListener;
//import android.hardware.SensorManager;
//import android.os.IBinder;
//import android.support.annotation.Nullable;
//
//import java.util.HashMap;
//import java.util.Map;
//
///**
// * Created by flabe on 25/5/2018.
// */
//
//public class SensorCapture extends Service implements SensorEventListener {
//
//    private SensorManager manager;
//    private Map<String, Boolean> sensors_required = new HashMap<>();
//    private Map<String, Integer> sensors_speed = new HashMap<>();
//
//    public SensorCapture(Context c, SensorConfig config){
//        this.manager = (SensorManager)c.getSystemService(Context.SENSOR_SERVICE);
//        this.sensors_required = config.getSensorsRequired();
//        this.sensors_speed = config.getSensorSpeed();
//    }
//
//    public void start(){
//        if(sensors_required.get("ACC")){ startAcc(); }
//        if(sensors_required.get("LAC")){ startLac(); }
//        if(sensors_required.get("GYR")){ startGyr(); }
//        if(sensors_required.get("BAR")){ startBar(); }
//        if(sensors_required.get("MAG")){ startMag(); }
//    }
//
//    public void stop(){
//        this.manager.unregisterListener(this);
//    }
//
//    private void startAcc() {
//        this.manager.registerListener(this, manager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER), sensors_speed.get("ACC"));
//    }
//
//    private void startLac() {
//        this.manager.registerListener(this, manager.getDefaultSensor(Sensor.TYPE_LINEAR_ACCELERATION), sensors_speed.get("LAC"));
//    }
//
//    private void startGyr() {
//        this.manager.registerListener(this, manager.getDefaultSensor(Sensor.TYPE_GYROSCOPE), sensors_speed.get("GYR"));
//    }
//
//    private void startMag() {
//        this.manager.registerListener(this, manager.getDefaultSensor(Sensor.TYPE_MAGNETIC_FIELD), sensors_speed.get("MAG"));
//    }
//
//    private void startBar() {
//        this.manager.registerListener(this, manager.getDefaultSensor(Sensor.TYPE_PRESSURE), sensors_speed.get("BAR"));
//    }
//
//
//    @Nullable
//    @Override
//    public IBinder onBind(Intent intent) {
//        return null;
//    }
//
//    @Override
//    public void onAccuracyChanged(Sensor sensor, int accuracy) {
//
//    }
//
//    @Override
//    public void onSensorChanged(SensorEvent event) {
//        Sensor sensor = event.sensor;
//        if (sensor.getType() == Sensor.TYPE_ACCELEROMETER) {
//            getAccelerometer(event);
//        }else if(sensor.getType() == Sensor.TYPE_LINEAR_ACCELERATION){
//            getLinearAcceleration(event);
//        }else if (sensor.getType() == Sensor.TYPE_GYROSCOPE) {
//            getGyroscope(event);
//        }else if(sensor.getType() == Sensor.TYPE_MAGNETIC_FIELD){
//            getMagnetometer(event);
//        }else if(sensor.getType() == Sensor.TYPE_PRESSURE) {
//            getBarometer(event);
//        }
//    }
//
//    public void getAccelerometer(SensorEvent event){
//    }
//    public void getGyroscope(SensorEvent event){
//    }
//    public void getMagnetometer(SensorEvent event){
//    }
//    public void getBarometer(SensorEvent event){
//    }
//    public void getLinearAcceleration(SensorEvent event) {
//    }
//
//}
