package com.ssig.sensorsmanager.capture;

import android.annotation.SuppressLint;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import com.ssig.sensorsmanager.time.DummyTime;
import com.ssig.sensorsmanager.time.NTPTime;
import com.ssig.sensorsmanager.time.SystemTime;
import com.ssig.sensorsmanager.time.Time;

import java.io.*;

class SensorListener implements SensorEventListener {


    private DataOutputStream dataOutputStream;
    private SystemTime systemTime;
    private Time secondaryTime;

    SensorListener(File sensorDataFile) throws IOException {
        this.dataOutputStream = new DataOutputStream(new BufferedOutputStream(new FileOutputStream(sensorDataFile), 512 * 1024));
        this.systemTime = new SystemTime();
        this.secondaryTime = NTPTime.isSynchronized() ? new NTPTime() : new DummyTime();
    }

    public void close() throws IOException {
        this.dataOutputStream.flush();
        this.dataOutputStream.close();
    }

    @SuppressLint("DefaultLocale")
    @Override
    public void onSensorChanged(SensorEvent event) {
        long primaryTimestamp = this.systemTime.now();
        long secondaryTimestamp = this.secondaryTime.now();

        try {
            this.dataOutputStream.writeLong(event.timestamp);
            this.dataOutputStream.writeLong(primaryTimestamp);
            this.dataOutputStream.writeLong(secondaryTimestamp);
            this.dataOutputStream.writeInt(event.accuracy);
            for(float value : event.values)
                this.dataOutputStream.writeFloat(value);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {

    }

}
