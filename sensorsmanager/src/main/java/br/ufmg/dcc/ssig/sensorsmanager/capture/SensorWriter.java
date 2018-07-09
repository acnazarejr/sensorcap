package br.ufmg.dcc.ssig.sensorsmanager.capture;

import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.os.SystemClock;

import br.ufmg.dcc.ssig.sensorsmanager.util.NTPTime;

import java.io.*;
import java.util.concurrent.TimeUnit;

abstract class SensorWriter implements SensorEventListener {

    class TimeUtil{
        Long offset;

        TimeUtil(){
            long currentSystemTimeMillis = System.currentTimeMillis();
            long currentElapsedNanos = SystemClock.elapsedRealtimeNanos();
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

    protected TimeUtil timeUtil;

    SensorWriter(){
        this.timeUtil = new TimeUtil();
    }

    public abstract void close() throws IOException;
    protected abstract void writeEvent(SensorEvent event);

    @Override
    public void onSensorChanged(SensorEvent event) {
        this.writeEvent(event);
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {

    }

}
