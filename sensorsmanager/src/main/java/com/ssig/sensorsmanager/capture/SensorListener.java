package com.ssig.sensorsmanager.capture;

import android.annotation.SuppressLint;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;

import com.ssig.sensorsmanager.time.DummyTime;
import com.ssig.sensorsmanager.time.NTPTime;
import com.ssig.sensorsmanager.time.SystemTime;
import com.ssig.sensorsmanager.time.Time;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.util.Locale;

public class SensorListener implements SensorEventListener {


    private File sensorDataFile;
    private BufferedWriter bufferedWriter;
    private SystemTime systemTime;
    private Time secondaryTime;
    private DecimalFormat decimalFormat;

    SensorListener(File sensorDataFile) throws IOException {

        this.sensorDataFile = sensorDataFile;

        this.bufferedWriter = new BufferedWriter(new FileWriter(this.sensorDataFile));
        this.systemTime = new SystemTime();
        this.secondaryTime = NTPTime.isSynchronized() ? new NTPTime() : new DummyTime();

        DecimalFormatSymbols otherSymbols = new DecimalFormatSymbols(Locale.US);
        otherSymbols.setDecimalSeparator('.');
        otherSymbols.setGroupingSeparator(',');
        this.decimalFormat = new DecimalFormat("#.#", otherSymbols);
        this.decimalFormat.setMaximumFractionDigits(12);
    }

    public File close() throws IOException {
        this.bufferedWriter.flush();
        this.bufferedWriter.close();
        return this.getSensorDataFile();
    }

    @SuppressLint("DefaultLocale")
    @Override
    public void onSensorChanged(SensorEvent event) {
        try {
            Long primaryTimestamp = this.systemTime.now();
            Long secondaryTimestamp = this.secondaryTime.now();
            String line = String.format("%d;%d;%d;%s", primaryTimestamp, secondaryTimestamp, event.accuracy, this.valuesToString(event.values));
            this.bufferedWriter.write(line);
            this.bufferedWriter.newLine();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {

    }

    private String valuesToString(float[] values){
        StringBuilder valuesString = new StringBuilder();
        int i;
        for(i = 0; i < values.length - 1; i++){
            valuesString.append(String.format("%s;", this.decimalFormat.format(values[i])));
        }
        valuesString.append(this.decimalFormat.format(values[i]));
        return valuesString.toString();
    }

    private File getSensorDataFile() {
        return sensorDataFile;
    }
}
