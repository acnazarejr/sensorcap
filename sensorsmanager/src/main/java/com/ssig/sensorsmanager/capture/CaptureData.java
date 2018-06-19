package com.ssig.sensorsmanager.capture;

import com.google.gson.GsonBuilder;
import com.ssig.sensorsmanager.SensorType;
import com.ssig.sensorsmanager.config.CaptureConfig;
import com.ssig.sensorsmanager.info.DeviceInfo;
import com.ssig.sensorsmanager.info.SensorInfo;

import java.io.FileWriter;
import java.io.IOException;
import java.io.Serializable;
import java.io.Writer;
import java.util.Map;

public class CaptureData implements Serializable {

    static final long serialVersionUID = 719145489623562378L;

    private CaptureConfig captureConfig;
    private DeviceInfo smartphoneDeviceInfo;
    private DeviceInfo smartwatchDeviceInfo;

    public CaptureData(){
        this.captureConfig = null;
        this.smartphoneDeviceInfo = null;
        this.smartwatchDeviceInfo = null;
    }

    public CaptureConfig getCaptureConfig() {
        return captureConfig;
    }

    public void setCaptureConfig(CaptureConfig captureConfig) {
        this.captureConfig = captureConfig;
    }

    public DeviceInfo getSmartphoneDeviceInfo() {
        return smartphoneDeviceInfo;
    }

    public void setSmartphoneDeviceInfo(DeviceInfo smartphoneDeviceInfo) {
        this.smartphoneDeviceInfo = smartphoneDeviceInfo;
    }

    public DeviceInfo getSmartwatchDeviceInfo() {
        return smartwatchDeviceInfo;
    }

    public void setSmartwatchDeviceInfo(DeviceInfo smartwatchDeviceInfo) {
        this.smartwatchDeviceInfo = smartwatchDeviceInfo;
    }

    public void toJson(String fileName){
        try {
            Writer writer = new FileWriter(fileName);
            new GsonBuilder().create().toJson(this, writer);
            writer.flush();
            writer.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
