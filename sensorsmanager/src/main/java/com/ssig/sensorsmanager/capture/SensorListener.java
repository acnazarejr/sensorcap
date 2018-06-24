package com.ssig.sensorsmanager.capture;

import android.annotation.SuppressLint;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;

import com.ssig.sensorsmanager.time.DummyTime;
import com.ssig.sensorsmanager.time.NTPTime;
import com.ssig.sensorsmanager.time.SystemTime;
import com.ssig.sensorsmanager.time.Time;

import java.io.BufferedOutputStream;
import java.io.BufferedWriter;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.util.Locale;

public class SensorListener implements SensorEventListener {


    private File sensorDataFile;
    private DataOutputStream dataOutputStream;
    private SystemTime systemTime;
    private Time secondaryTime;
//    private DecimalFormat decimalFormat;

    SensorListener(File sensorDataFile) throws IOException {

        this.sensorDataFile = sensorDataFile;

        this.dataOutputStream = new DataOutputStream(new BufferedOutputStream(new FileOutputStream(sensorDataFile), 512 * 1024));
        this.systemTime = new SystemTime();
        this.secondaryTime = NTPTime.isSynchronized() ? new NTPTime() : new DummyTime();

//        DecimalFormatSymbols otherSymbols = new DecimalFormatSymbols(Locale.US);
//        otherSymbols.setDecimalSeparator('.');
//        otherSymbols.setGroupingSeparator(',');
//        this.decimalFormat = new DecimalFormat("#.#", otherSymbols);
//        this.decimalFormat.setMaximumFractionDigits(12);
    }

    public File close() throws IOException {
        this.dataOutputStream.flush();
        this.dataOutputStream.close();
        return this.getSensorDataFile();
    }

    @SuppressLint("DefaultLocale")
    @Override
    public void onSensorChanged(SensorEvent event) {
        long primaryTimestamp = this.systemTime.now();
        long secondaryTimestamp = this.secondaryTime.now();

        try {
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

//    private String valuesToString(float[] values){
//        StringBuilder valuesString = new StringBuilder();
//        int i;
//        for(i = 0; i < values.length - 1; i++){
//            valuesString.append(String.format("%s;", this.decimalFormat.format(values[i])));
//        }
//        valuesString.append(this.decimalFormat.format(values[i]));
//        return valuesString.toString();
//    }

    private File getSensorDataFile() {
        return sensorDataFile;
    }
}
