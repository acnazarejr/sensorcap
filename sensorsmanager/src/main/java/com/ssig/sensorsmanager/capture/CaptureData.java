package com.ssig.sensorsmanager.capture;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.ssig.sensorsmanager.SensorType;
import com.ssig.sensorsmanager.config.CaptureConfig;
import com.ssig.sensorsmanager.info.SensorInfo;

import java.io.FileWriter;
import java.io.IOException;
import java.io.Serializable;
import java.io.Writer;
import java.util.Map;

public class CaptureData implements Serializable {

    static final long serialVersionUID = 719145489623562378L;

    private CaptureConfig captureConfig;
    private Map<SensorType, SensorInfo> smartphoneSensorsInfo;
    private Map<SensorType, SensorInfo> smartwatchSensorsInfo;

    public CaptureData(){
        this.captureConfig = null;
        this.smartphoneSensorsInfo = null;
        this.smartwatchSensorsInfo = null;
    }

    public CaptureConfig getCaptureConfig() {
        return captureConfig;
    }

    public void setCaptureConfig(CaptureConfig captureConfig) {
        this.captureConfig = captureConfig;
    }

    public Map<SensorType, SensorInfo> getSmartphoneSensorsInfo() {
        return smartphoneSensorsInfo;
    }

    public void setSmartphoneSensorsInfo(Map<SensorType, SensorInfo> smartphoneSensorsInfo) {
        this.smartphoneSensorsInfo = smartphoneSensorsInfo;
    }

    public Map<SensorType, SensorInfo> getSmartwatchSensorsInfo() {
        return smartwatchSensorsInfo;
    }

    public void setSmartwatchSensorsInfo(Map<SensorType, SensorInfo> smartwatchSensorsInfo) {
        this.smartwatchSensorsInfo = smartwatchSensorsInfo;
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
