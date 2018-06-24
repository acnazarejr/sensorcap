package com.ssig.sensorsmanager.capture;

import android.content.Context;
import android.hardware.Sensor;
import android.hardware.SensorManager;
import android.support.annotation.NonNull;

import com.ssig.sensorsmanager.SensorType;
import com.ssig.sensorsmanager.config.DeviceConfig;
import com.ssig.sensorsmanager.config.SensorConfig;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

public class DeviceCaptureRunner {

    public enum Status{
        IDLE, RUNNING, STOPPED, FINISHED
    }

    private DeviceConfig deviceConfig;
    private File systemCapturesFolder;
    private File deviceCaptureFolder;
    private Status status;

    private SensorManager sensorManager;
    private Map<SensorType, SensorListener> sensorListeners;


    public DeviceCaptureRunner(@NonNull Context context, @NonNull DeviceConfig deviceConfig, @NonNull File systemCapturesFolder) throws IOException {

        this.deviceConfig = deviceConfig;
        this.systemCapturesFolder = systemCapturesFolder;

        this.sensorManager = (SensorManager)context.getSystemService(Context.SENSOR_SERVICE);
        this.sensorListeners = new HashMap<>();

        this.configureFolders();
        this.configureListeners();
        this.status = Status.IDLE;
    }

    public void start(){
        if (this.status != Status.IDLE)
            return;
        Map<SensorType, SensorConfig> sensorsConfig = this.deviceConfig.getAllSensorsConfig();
        for(SensorType sensorType : this.sensorListeners.keySet()){
            SensorConfig sensorConfig = sensorsConfig.get(sensorType);
            SensorListener sensorListener = this.sensorListeners.get(sensorType);
            int samplingPeriodUs = 1_000_000/ sensorConfig.getFrequency();
            Sensor sensor = this.sensorManager.getDefaultSensor(sensorType.androidType());
            this.sensorManager.registerListener(sensorListener, sensor, samplingPeriodUs);
        }
        this.status = Status.RUNNING;
    }

    private void stop() throws IOException{
        for(SensorType sensorType : this.sensorListeners.keySet()){
            SensorListener sensorListener = this.sensorListeners.get(sensorType);
            Sensor sensor = this.sensorManager.getDefaultSensor(sensorType.androidType());
            this.sensorManager.unregisterListener(sensorListener, sensor);
            sensorListener.close();
        }
        this.status = Status.STOPPED;
    }

    public void finish() throws IOException {
        if (this.status == Status.RUNNING)
            this.stop();
        this.status = Status.FINISHED;
    }


    private void configureFolders() throws FileNotFoundException {

        if(!this.systemCapturesFolder.exists()){
            if (!this.systemCapturesFolder.mkdirs()) {
                throw new FileNotFoundException(String.format("Failed to create the system capture folder: %s", this.systemCapturesFolder));
            }
        }


        File currentCaptureFolder = new File(String.format("%s%s%s", this.systemCapturesFolder, File.separator, this.deviceConfig.getCaptureConfigUUID()));
        if(!currentCaptureFolder.exists()){
            if (!currentCaptureFolder.mkdirs()) {
                throw new FileNotFoundException(String.format("Failed to create the current capture folder: %s", currentCaptureFolder));
            }
        }

        this.deviceCaptureFolder = new File(String.format("%s%s%s", currentCaptureFolder, File.separator, this.deviceConfig.getDeviceConfigUUID()));
        if(!this.deviceCaptureFolder.exists()){
            if (!this.deviceCaptureFolder.mkdirs()) {
                throw new FileNotFoundException(String.format("Failed to create the current capture folder: %s", this.deviceCaptureFolder));
            }
        }

    }

    private void configureListeners() throws IOException {
        Map<SensorType, SensorConfig> sensorsConfig = this.deviceConfig.getAllSensorsConfig();
        for(SensorType sensorType : sensorsConfig.keySet()){
            SensorConfig sensorConfig = sensorsConfig.get(sensorType);
            if (sensorConfig.isEnabled()){
                File sensorDataFile = new File(String.format("%s%s%s.dat", this.deviceCaptureFolder, File.separator, sensorConfig.getSensorConfigUUID()));
                this.sensorListeners.put(sensorType, new SensorListener(sensorDataFile));
            }
        }
        this.status = Status.IDLE;
    }

}
