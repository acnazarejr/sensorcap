package com.ssig.sensorsmanager.capture;

import android.content.Context;
import android.hardware.Sensor;
import android.hardware.SensorManager;
import android.os.Environment;

import com.ssig.sensorsmanager.SensorType;
import com.ssig.sensorsmanager.config.SensorConfig;
import com.ssig.sensorsmanager.time.Time;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

public class CaptureRunner {

    public enum Status{
        IDLE, RUNNING, FINISHED
    }

    private Map<SensorType, SensorConfig> sensorConfigs;
    private Time secondaryTime;

    private String sensorDataFolderName;
    private File sensorDataFolderPath;
    private SensorManager sensorManager;
    private Map<SensorType, SensorListener> sensorListeners;
    private Status status;

    public CaptureRunner(Context context, Map<SensorType, SensorConfig> sensorConfigs, Time secondaryTime, String sensorDataFolderName) throws FileNotFoundException {
        this.sensorConfigs = sensorConfigs;
        this.secondaryTime = secondaryTime;
        this.sensorManager = (SensorManager)context.getSystemService(Context.SENSOR_SERVICE);
        this.sensorListeners = new HashMap<>();
        this.sensorDataFolderName = sensorDataFolderName;
        this.configure();
    }

    public Status getStatus() {
        return status;
    }

    public void setStatus(Status status) {
        this.status = status;
    }

    public File getSensorDataFolderPath() {
        return sensorDataFolderPath;
    }

    private void configure() throws FileNotFoundException {

        this.sensorDataFolderPath = new File(String.format("%s/%s", Environment.getExternalStorageDirectory().getAbsolutePath(), this.sensorDataFolderName));
        if(!this.sensorDataFolderPath.exists()){
            if (!this.sensorDataFolderPath.mkdirs()) {
                throw new FileNotFoundException(String.format("Failed to create the capture folder: %s", this.sensorDataFolderPath));
            }
        }

        this.sensorDataFolderPath = new File(String.format("%s/%s", this.sensorDataFolderPath, System.currentTimeMillis()));
        if(!this.sensorDataFolderPath.exists()){
            if (!this.sensorDataFolderPath.mkdirs()) {
                throw new FileNotFoundException(String.format("Failed to create the capture folder: %s", this.sensorDataFolderPath));
            }
        }

        for(SensorType sensorType : this.sensorConfigs.keySet()){
            SensorConfig sensorConfig = this.sensorConfigs.get(sensorType);
            if (sensorConfig.isEnabled()){
                this.sensorListeners.put(sensorType, new SensorListener(sensorType, this.sensorDataFolderPath, this.secondaryTime));
            }
        }
        this.status = Status.IDLE;
    }

    public void start(){
        for(SensorType sensorType : this.sensorListeners.keySet()){
            SensorConfig sensorConfig = this.sensorConfigs.get(sensorType);
            SensorListener sensorListener = this.sensorListeners.get(sensorType);
            int samplingPeriodUs = 1_000_000/sensorConfig.getFrequency();
            Sensor sensor = this.sensorManager.getDefaultSensor(sensorType.androidType());
            this.sensorManager.registerListener(sensorListener, sensor, samplingPeriodUs);
        }
        this.status = Status.RUNNING;
    }

    public void finish() throws IOException {
        for(SensorType sensorType : this.sensorListeners.keySet()){
            SensorListener sensorListener = this.sensorListeners.get(sensorType);
            Sensor sensor = this.sensorManager.getDefaultSensor(sensorType.androidType());
            this.sensorManager.unregisterListener(sensorListener, sensor);
            sensorListener.close();
        }
    }


}
