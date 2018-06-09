//package com.ssig.sensorsmanager;
//
//import com.google.gson.Gson;
//import com.google.gson.GsonBuilder;
//import com.ssig.sensorsmanager.info.PersonInfo;
//
//import java.io.FileWriter;
//import java.io.IOException;
//import java.io.Serializable;
//import java.io.Writer;
//
///**
// * Created by flabe on 02/06/2018.
// */
//
//public class CaptureData implements Serializable {
//    //private LocalDate captureDate = LocalDate.now();
//    private DeviceData smartwatchDeviceData = null;
//    private DeviceData smartphoneDeviceData = null;
//    private PersonInfo personInfo;
//    private String captureActivity;
//    private Gson gson = new GsonBuilder().create();
//
//    public CaptureData(PersonInfo p, String activity, DeviceData smartphone, DeviceData smartwatch){
//        this.personInfo = p;
//        this.captureActivity = activity;
//        this.smartphoneDeviceData = smartphone;
//        this.smartwatchDeviceData = smartwatch;
//    }
//
//    class Header{
//        String name = personInfo.getName();
//        Integer age = personInfo.getAge();
//        Float weight = personInfo.getWeight();
//        Float height = personInfo.getHeight();
//        String activity = captureActivity;
//        //String date = String.valueOf(captureDate);
//    }
//
//    public DeviceData getSmartphoneDeviceData() {
//        return smartphoneDeviceData;
//    }
//
//    public DeviceData getSmartwatchDeviceData() {
//        return smartwatchDeviceData;
//    }
//
//    public PersonInfo getPersonInfo() {
//        return personInfo;
//    }
//
//    public String getActivity() {
//        return captureActivity;
//    }
//
//    public void save(String fileToSave){
//
//    }
//
//    public void infoToJson(String fileToSave){
//        try {
//            Writer writer = new FileWriter(fileToSave);
//            gson.toJson(new Header(), writer);
//        } catch (IOException e) {
//            e.printStackTrace();
//        }
//    }
//}
