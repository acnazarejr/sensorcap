package com.ssig.sensorsmanager;

import java.io.Serializable;

/**
 * Created by flabe on 02/06/2018.
 */

public class CaptureFactory implements Serializable {

    private SensorData sensorData;
    //private DeviceData deviceData;
    //private CaptureData captureData;

    public CaptureFactory(){
    }

    public SensorData makeSensorData(SensorConfig config, SensorInfo sensorInfo, String file){
        this.sensorData = new SensorData(config, sensorInfo, file);
        return this.sensorData;
    }


}
