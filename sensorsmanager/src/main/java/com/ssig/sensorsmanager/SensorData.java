package com.ssig.sensorsmanager;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Serializable;
import java.util.ArrayList;

/**
 * Created by flabe on 01/06/2018.
 */

public class SensorData implements Serializable {
    private SensorType sensorType;
    private SensorInfo sensorInfo;
    private SensorConfig sensorConfig;
    private Float startTimestamp;
    private Float endTimestamp;
    private String sensorFilePath;
    private ArrayList<ArrayList<String>> values = new ArrayList<ArrayList<String>>();

    public SensorData(SensorConfig sensorConfig, SensorInfo sensorInfo, String file){
        this.sensorConfig = sensorConfig;
        this.sensorType = sensorConfig.getSensortype();
        this.sensorInfo = sensorInfo;
        this.sensorFilePath = file;
        getValuesFromFile();
    }

    private void getValuesFromFile() {
        String s = "";
        String fileContent = "";
        try {
            File file = new File(this.sensorFilePath);
            FileInputStream stream = new FileInputStream(file);
            BufferedReader myReader = new BufferedReader(new InputStreamReader(stream));
            while ((s = myReader.readLine()) != null) {
                ArrayList<String> inner = new ArrayList<>();
                String parts[] = s.split(";");
                for(int i = 0; i < parts.length; i++){
                    inner.add(parts[i]);
                }
                values.add(inner);
            }
            myReader.close();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public ArrayList<ArrayList<String>> getValues() {
        return values;
    }

    public String getSensorFilePath() {
        return sensorFilePath;
    }

    public Float getEndTimestamp() {
        return endTimestamp;
    }

    public Float getStartTimestamp() {
        return startTimestamp;
    }

    public SensorConfig getSensorConfig() {
        return sensorConfig;
    }

    public SensorInfo getSensorInfo() {
        return sensorInfo;
    }

    public SensorType getSensorType() {
        return sensorType;
    }
}
