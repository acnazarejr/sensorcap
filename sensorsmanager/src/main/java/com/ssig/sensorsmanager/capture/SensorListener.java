package com.ssig.sensorsmanager.capture;

import android.annotation.SuppressLint;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.os.SystemClock;

import com.ssig.sensorsmanager.util.NTPTime;

import java.io.*;
import java.util.concurrent.TimeUnit;

class SensorListener implements SensorEventListener {


    class TimeUtil{
        Long offset = null;

        TimeUtil(){
            long currentSystemTimeMillis = System.currentTimeMillis();
            long teste1 = System.currentTimeMillis();
            long currentElapsedNanos = SystemClock.elapsedRealtimeNanos();
            long teste2 = SystemClock.elapsedRealtimeNanos();
            this.offset = currentSystemTimeMillis - TimeUnit.NANOSECONDS.toMillis(currentElapsedNanos);
        }

        long eventToDeviceTime(long eventTime){
            return TimeUnit.NANOSECONDS.toMillis(eventTime) + this.offset;
        }

        long eventToNTPTime(long eventTime){
            Long ntpTime = NTPTime.toNTPTime(this.eventToDeviceTime(eventTime));
            return ntpTime != null ? ntpTime : -1L;
        }

    }

    private DataOutputStream dataOutputStream;
    private TimeUtil timeUtil;
//    private Time systemTime;
//    private Time networkTime;

    SensorListener(File sensorDataFile) throws IOException {
        this.dataOutputStream = new DataOutputStream(new BufferedOutputStream(new FileOutputStream(sensorDataFile), 512 * 1024));
        this.timeUtil = new TimeUtil();
//        this.systemTime = new SystemTime();
//        this.networkTime = TrueTimeNTPTime.isSynchronized() ? new TrueTimeNTPTime() : new DummyTime();
//        this.networkTime = new DummyTime();
    }

    public void close() throws IOException {
        this.dataOutputStream.flush();
        this.dataOutputStream.close();
    }

    @SuppressLint("DefaultLocale")
    @Override
    public void onSensorChanged(SensorEvent event) {

        try {
            this.dataOutputStream.writeLong(event.timestamp);
            this.dataOutputStream.writeLong(this.timeUtil.eventToDeviceTime(event.timestamp));
            this.dataOutputStream.writeLong(this.timeUtil.eventToNTPTime(event.timestamp));
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
