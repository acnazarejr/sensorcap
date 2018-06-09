//package com.ssig.sensorsmanager;
//
//import com.ssig.sensorsmanager.config.SensorConfig;
//import com.ssig.sensorsmanager.info.PersonInfo;
//import com.ssig.sensorsmanager.info.SensorInfo;
//
//import java.io.Serializable;
//import java.util.HashMap;
//
///**
// * Created by flabe on 02/06/2018.
// */
//
//public class CaptureFactory implements Serializable {
//    private SensorData sensorData;
//    private DeviceData deviceData;
//    private CaptureData captureData;
//    private HashMap<SensorType, SensorData> sensorsDevice;
//    private DeviceData.DeviceType deviceType;
//
//    public CaptureFactory(){
//    }
//
//    public SensorData makeSensorData(SensorConfig config, SensorInfo sensorInfo, String file){
//        this.sensorData = new SensorData(config, sensorInfo, file);
//        return this.sensorData;
//    }
//
//    public DeviceData makeDeviceData(DeviceData.DeviceType deviceType, HashMap<SensorType, SensorData> sensorsDevice){
//        this.deviceType = deviceType;
//        this.sensorsDevice = sensorsDevice;
//        this.deviceData = new DeviceData(deviceType, sensorsDevice);
//        return deviceData;
//    }
//
//    public CaptureData makeCaptureData(PersonInfo personInfo, String activity, DeviceData smartphoneData, DeviceData smartwatchData){
//        this.captureData = new CaptureData(personInfo, activity, smartphoneData, smartwatchData);
//        return this.captureData;
//    }
//
//
//}
