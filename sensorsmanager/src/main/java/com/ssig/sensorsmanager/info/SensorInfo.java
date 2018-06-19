package com.ssig.sensorsmanager.info;

import android.content.Context;
import android.hardware.Sensor;
import android.hardware.SensorManager;

import com.ssig.sensorsmanager.SensorType;

import java.io.Serializable;
import java.util.Map;
import java.util.TreeMap;

public class SensorInfo implements Serializable{

    static final long serialVersionUID = 123456789123456789L;

    protected SensorType sensorType;
    protected String model;
    protected String vendor;
    protected int version;
    protected float power;
    protected float maximumRange;
    protected int maxDelay;
    protected int minDelay;
    protected float resolution;
    protected int reportingMode;

    public SensorInfo(SensorType sensorType){
        this.sensorType = sensorType;
        this.model = null;
        this.vendor = null;
        this.version = -1;
        this.power = -1;
        this.maximumRange = -1;
        this.maxDelay = -1;
        this.minDelay = -1;
        this.resolution = -1;
        this.reportingMode = -1;
    }

    public SensorType getSensorType() {
        return sensorType;
    }

    public float getMaximumRange() {
        return maximumRange;
    }

    private void setMaximumRange(float maximumRange) {
        this.maximumRange = maximumRange;
    }

    public String getVendor() {
        return vendor;
    }

    private void setVendor(String vendor) {
        this.vendor = vendor;
    }

    public float getResolution() {
        return resolution;
    }

    private void setResolution(float resolution) {
        this.resolution = resolution;
    }

    public String getModel() {
        return this.model;
    }

    private void setModel(String model) {
        this.model = model;
    }

    public int getVersion() {
        return version;
    }

    public void setVersion(int version) {
        this.version = version;
    }

    public float getPower() {
        return power;
    }

    private void setPower(float power) {
        this.power = power;
    }

    public int getMaxDelay() {
        return maxDelay;
    }

    private void setMaxDelay(int maxDelay) {
        this.maxDelay = maxDelay;
    }

    public int getMinDelay() {
        return minDelay;
    }

    private void setMinDelay(int minDelay) {
        this.minDelay = minDelay;
    }

    public int getMinFrequency() {
        if (reportingMode == Sensor.REPORTING_MODE_CONTINUOUS || reportingMode == Sensor.REPORTING_MODE_ON_CHANGE)
            return Math.max(maxDelay>0?1_000_000/maxDelay:1, 1);
        return -1;
    }

    public int getMaxFrequency() {
        if (reportingMode == Sensor.REPORTING_MODE_CONTINUOUS || reportingMode == Sensor.REPORTING_MODE_ON_CHANGE)
            return Math.max(minDelay>0?1_000_000/minDelay:1, 1);
        return -1;
    }

    public int getDefaultFrequency() {
        if (reportingMode == Sensor.REPORTING_MODE_CONTINUOUS || reportingMode == Sensor.REPORTING_MODE_ON_CHANGE)
            return  Math.max(getMaxFrequency(), 1);
        return -1;
    }

    public int getReportingMode() {
        return reportingMode;
    }

    private void setReportingMode(int reportingMode) {
        this.reportingMode = reportingMode;
    }


    ////////////////////////////////////////////////////////////////////////////////////////////////
    // STATIC FACTORY METHODS
    ////////////////////////////////////////////////////////////////////////////////////////////////

    public static SensorInfo get(Context context, SensorType sensorType){

        SensorManager sensorManager = (SensorManager)context.getSystemService(Context.SENSOR_SERVICE);
        Sensor defaultSensor = sensorManager != null ? sensorManager.getDefaultSensor(sensorType.androidType()) : null;
        if (defaultSensor != null){
            SensorInfo sensorInfo = new SensorInfo((sensorType));
            sensorInfo.setModel(defaultSensor.getName());
            sensorInfo.setVendor(defaultSensor.getVendor());
            sensorInfo.setVersion(defaultSensor.getVersion());
            sensorInfo.setPower(defaultSensor.getPower());
            sensorInfo.setMaximumRange(defaultSensor.getMaximumRange());
            sensorInfo.setMaxDelay(defaultSensor.getMaxDelay());
            sensorInfo.setMinDelay(defaultSensor.getMinDelay());
            sensorInfo.setResolution(defaultSensor.getResolution());
            sensorInfo.setReportingMode(defaultSensor.getReportingMode());
            return sensorInfo;
        }
        return null;
    }

    public static Map<SensorType, SensorInfo> getAll(Context context) {
        Map<SensorType, SensorInfo> allSensors = new TreeMap<>();
        for(SensorType sensorType : SensorType.values()){
            allSensors.put(sensorType, SensorInfo.get(context, sensorType));
        }
        return  allSensors;
    }


}
