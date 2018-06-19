//package com.ssig.sensorsmanager;
//
//import android.content.Context;
//import android.hardware.Sensor;
//import android.hardware.SensorEvent;
//import android.hardware.SensorEventListener;
//import android.hardware.SensorManager;
//
//import com.ssig.sensorsmanager.config.CaptureConfig;
//import com.ssig.sensorsmanager.config.SensorConfig;
//
//import java.io.Serializable;
//import java.util.ArrayList;
//import java.util.Map;
//
///**
// * Created by flabe on 01/06/2018.
// */
//
//public class Capture implements Serializable, SensorEventListener {
//    private SensorManager manager;
//    private CaptureConfig captureConfig = null;
//    private ArrayList<String> filesPath = null;
//
//    public Capture(CaptureConfig captureConfig, Context c){
//        this.manager = (SensorManager)c.getSystemService(Context.SENSOR_SERVICE);
//        this.captureConfig = captureConfig;
//    }
//
//    public void start(){
//        saveSensors();
//        registerSensorListeners();
//    }
//
//    private void saveSensors() {
//        for (Map.Entry<SensorType, SensorConfig> sensor : captureConfig.getSensors().entrySet()) {
//            String path = captureConfig.getPersonInfo().getDeviceName() + "_" + sensor.getValue().getSensorType().abbrev() + "_" + System.currentTimeMillis() + ".txt";
//            filesPath.add(path);
//        }
//    }
//
//    private void registerSensorListeners() {
//        for (Map.Entry<SensorType, SensorConfig> entry : captureConfig.getSensors().entrySet()) {
//            SensorType sensorType = entry.getKey();
//            SensorConfig sensorConfig = entry.getValue();
//            this.manager.registerListener(this, this.manager.getDefaultSensor(sensorType.code()), sensorConfig.getFrequency());
//        }
//    }
//
//    public ArrayList<String> stop(){
//        this.manager.unregisterListener(this);
//        return this.filesPath;
//    }
//
//    @Override
//    public void onSensorChanged(SensorEvent event) {
//        /*     TODO - Know which sensor was activated     */
//    }
//
//    @Override
//    public void onAccuracyChanged(Sensor sensor, int accuracy) {
//    }
//}
