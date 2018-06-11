package com.ssig.sensorsmanager.capture;

import android.annotation.SuppressLint;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.support.annotation.NonNull;

import com.ssig.sensorsmanager.SensorType;
import com.ssig.sensorsmanager.time.SystemTime;
import com.ssig.sensorsmanager.time.Time;

import java.io.BufferedOutputStream;
import java.io.BufferedWriter;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.math.BigDecimal;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.util.Arrays;
import java.util.Locale;

public class SensorListener implements SensorEventListener {

    private File sensorDataFilePath;
    private BufferedWriter bufferedWriter;
    private Time primaryTime;
    private Time secondaryTime;
    private DecimalFormat decimalFormat;

    public SensorListener(SensorType sensorType, File sensorDataFolderPath, @NonNull Time secondaryTime) throws IOException {
        this.sensorDataFilePath = new File(String.format("%s/%s.txt", sensorDataFolderPath, sensorType.toString().replace(" ", "_").toLowerCase()));
        this.bufferedWriter = new BufferedWriter(new FileWriter(this.sensorDataFilePath));
        this.primaryTime = new SystemTime();
        this.secondaryTime = secondaryTime;
        DecimalFormatSymbols otherSymbols = new DecimalFormatSymbols(Locale.US);
        otherSymbols.setDecimalSeparator('.');
        otherSymbols.setGroupingSeparator(',');
        this.decimalFormat = new DecimalFormat("#.#", otherSymbols);
        this.decimalFormat.setMaximumFractionDigits(12);
    }

    public File close() throws IOException {
        this.bufferedWriter.flush();
        this.bufferedWriter.close();
        return this.getSensorDataFilePath();
    }

    @SuppressLint("DefaultLocale")
    @Override
    public void onSensorChanged(SensorEvent event) {
        try {
            Long primaryTimestamp = this.primaryTime.now(-1L);
            Long secondaryTimestamp = this.secondaryTime.now(-1L);
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
        String valuesString = "";
        int i;
        for(i = 0; i < values.length - 1; i++){
            valuesString += String.format("%s;", this.decimalFormat.format(values[i]));
        }
        valuesString += this.decimalFormat.format(values[i]);
        return valuesString;
    }

    public File getSensorDataFilePath() {
        return sensorDataFilePath;
    }
}
