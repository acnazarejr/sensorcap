//package com.ssig.sensorsmanager;
//
//import java.io.Serializable;
//import java.lang.reflect.Array;
//import java.util.ArrayList;
//import java.util.HashMap;
//
///**
// * Created by flabe on 02/06/2018.
// */
//
//public class DeviceData implements Serializable {
//    public enum DeviceType{
//        MOBILE, WEAR;
//    }
//    private DeviceType deviceType;
//    private String model;
//    private String vendor;
//    private HashMap<SensorType, SensorData> sensorsData = new HashMap<>();
//
//    public DeviceData(DeviceType deviceType, HashMap<SensorType, SensorData> sensorsDevice) {
//        this.deviceType = deviceType;
//        this.sensorsData = sensorsDevice;
//    }
//
//    public String getModel() {
//        return model;
//    }
//
//    public String getVendor() {
//        return vendor;
//    }
//
//    public DeviceType getDeviceType() {
//        return deviceType;
//    }
//
//
//}
