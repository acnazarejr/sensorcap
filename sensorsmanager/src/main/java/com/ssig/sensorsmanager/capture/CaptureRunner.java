package com.ssig.sensorsmanager.capture;

import android.annotation.SuppressLint;
import android.content.Context;
import android.hardware.Sensor;
import android.hardware.SensorManager;
import android.os.Environment;

import com.ssig.sensorsmanager.SensorType;
import com.ssig.sensorsmanager.config.SensorConfig;
import com.ssig.sensorsmanager.time.NTPTime;
import com.ssig.sensorsmanager.time.SystemTime;
import com.ssig.sensorsmanager.time.Time;
import com.ssig.sensorsmanager.util.Compression;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

public class CaptureRunner {

    public enum Status{
        IDLE, RUNNING, STOPPED, FINISHED
    }

    private Map<SensorType, SensorConfig> sensorConfigs;
    private Time secondaryTime;

    private String captureAlias;
    private File appFolder;
    private File captureFolder;
    private Map<String, File> sensorFilesToCompress;
    private File captureCompressedFile;
    private SensorManager sensorManager;
    private Map<SensorType, SensorListener> sensorListeners;
    private Status status;

    private Long startTimestamp;
    private Long endTimestamp;


    public CaptureRunner(Context context, Map<SensorType, SensorConfig> sensorConfigs, String appFolderName, String captureAlias) throws IOException {

        this.captureAlias = captureAlias;
        this.sensorConfigs = sensorConfigs;

        this.secondaryTime = new NTPTime();
        this.sensorManager = (SensorManager)context.getSystemService(Context.SENSOR_SERVICE);
        this.sensorListeners = new HashMap<>();
        this.sensorFilesToCompress = null;
        this.startTimestamp = null;
        this.endTimestamp = null;

        this.configureFolders(appFolderName);
        this.configureListeners();
        this.status = Status.IDLE;
    }

    public void start(){
        if (this.status != Status.IDLE)
            return;
        this.startTimestamp = new SystemTime().now();
        for(SensorType sensorType : this.sensorListeners.keySet()){
            SensorConfig sensorConfig = this.sensorConfigs.get(sensorType);
            SensorListener sensorListener = this.sensorListeners.get(sensorType);
            int samplingPeriodUs = 1_000_000/sensorConfig.getFrequency();
            Sensor sensor = this.sensorManager.getDefaultSensor(sensorType.androidType());
            this.sensorManager.registerListener(sensorListener, sensor, samplingPeriodUs);
        }
        this.status = Status.RUNNING;
    }

    public void stop() throws IOException{
        this.sensorFilesToCompress = new HashMap<>();
        for(SensorType sensorType : this.sensorListeners.keySet()){
            SensorListener sensorListener = this.sensorListeners.get(sensorType);
            Sensor sensor = this.sensorManager.getDefaultSensor(sensorType.androidType());
            this.sensorManager.unregisterListener(sensorListener, sensor);
            File sensorFile = sensorListener.close();
            this.sensorFilesToCompress.put(sensorFile.getName(), sensorFile);
        }
        this.endTimestamp = new SystemTime().now();
        this.status = Status.STOPPED;
    }

    @SuppressLint("SimpleDateFormat")
    public File finish() throws IOException {
        if (this.status == Status.RUNNING)
            this.stop();
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyyMMdd_HHmmss");
        String startDate = simpleDateFormat.format(new Date(this.getStartTimestamp()));
        String endDate = simpleDateFormat.format(new Date(this.getEndTimestamp()));
        this.captureCompressedFile = new File(String.format("%s/%s_%s_%s.zip", this.appFolder, captureAlias.toLowerCase(), startDate, endDate));
        Compression.compressFiles(this.sensorFilesToCompress, this.captureCompressedFile);
        this.deleteCaptureFolder();
        this.status = Status.FINISHED;
        return this.captureCompressedFile;
    }

    public Status getStatus() {
        return status;
    }

    public Long getStartTimestamp() {
        return startTimestamp;
    }

    public Long getEndTimestamp() {
        return endTimestamp;
    }

    private void deleteCaptureFolder(){
        for (File child : this.captureFolder.listFiles())
            child.delete();
        this.captureFolder.delete();
    }

    @SuppressLint("DefaultLocale")
    private void configureFolders(String appFolderName) throws FileNotFoundException {

        this.appFolder = new File(String.format("%s/%s", Environment.getExternalStorageDirectory().getAbsolutePath(), appFolderName));
        if(!this.appFolder.exists()){
            if (!this.appFolder.mkdirs()) {
                throw new FileNotFoundException(String.format("Failed to create the capture folder: %s", this.appFolder));
            }
        }

        this.captureFolder = new File(String.format("%s/%s_%d", this.appFolder, captureAlias.toLowerCase(), System.currentTimeMillis()));
        if(!this.captureFolder.exists()){
            if (!this.captureFolder.mkdirs()) {
                throw new FileNotFoundException(String.format("Failed to create the capture folder: %s", this.captureFolder));
            }
        }

    }

    private void configureListeners() throws IOException {
        for(SensorType sensorType : this.sensorConfigs.keySet()){
            SensorConfig sensorConfig = this.sensorConfigs.get(sensorType);
            if (sensorConfig.isEnabled()){
                this.sensorListeners.put(sensorType, new SensorListener(sensorType, this.captureFolder, this.secondaryTime));
            }
        }
        this.status = Status.IDLE;
    }

}
