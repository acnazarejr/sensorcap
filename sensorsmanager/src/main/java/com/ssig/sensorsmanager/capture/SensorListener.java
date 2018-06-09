package com.ssig.sensorsmanager.capture;

import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.support.annotation.NonNull;

import com.ssig.sensorsmanager.SensorType;
import com.ssig.sensorsmanager.time.SystemTime;
import com.ssig.sensorsmanager.time.Time;

import java.io.DataOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;

public class SensorListener implements SensorEventListener {

    private SensorType sensorType;
    private File sensorDataFilePath;
    private DataOutputStream dataOutputStream;
    private Time secondaryTime;

    public SensorListener(SensorType sensorType, File sensorDataFolderPath, @NonNull Time secondaryTime) throws FileNotFoundException {
        this.sensorType = sensorType;
        this.sensorDataFilePath = new File(String.format("%s/%s.dat", sensorDataFolderPath, sensorType));
        this.dataOutputStream = new DataOutputStream(new FileOutputStream(this.sensorDataFilePath));
        this.secondaryTime = secondaryTime;
    }


    public void close() throws IOException {
        this.dataOutputStream.flush();
        this.dataOutputStream.close();
    }


    @Override
    public void onSensorChanged(SensorEvent event) {
        try {
            Long secondaryTimestamp = this.secondaryTime.now(-1L);
            this.dataOutputStream.writeLong(event.timestamp);
            this.dataOutputStream.writeLong(secondaryTimestamp);
            this.dataOutputStream.writeInt(event.accuracy);
            for (float value : event.values)
                this.dataOutputStream.writeFloat(value);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {

    }
}
