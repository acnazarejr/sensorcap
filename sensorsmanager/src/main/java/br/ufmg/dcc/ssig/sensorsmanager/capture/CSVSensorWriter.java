package br.ufmg.dcc.ssig.sensorsmanager.capture;

import android.annotation.SuppressLint;
import android.hardware.SensorEvent;

import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVPrinter;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;


class CSVSensorWriter extends SensorWriter {

    private CSVPrinter csvPrinter;

    @SuppressLint("DefaultLocale")
    CSVSensorWriter(File sensorDataFile, int valuesLength) throws IOException {
        super();
        BufferedWriter bufferedWriter = new BufferedWriter(new FileWriter(sensorDataFile), 8 * 1024);
        List<String> header = new ArrayList<>();
        header.add("eventTimeNanos");
        header.add("deviceTimeMillis");
        header.add("ntpTimeMillis");
        header.add("sensorAccuracy");
        for(int i=1; i<=valuesLength; i++){
            header.add(String.format("value%02d", i));
        }
        String[] headerArray = new String[header.size()];
        this.csvPrinter = new CSVPrinter(bufferedWriter, CSVFormat.RFC4180.withHeader(header.toArray(headerArray)));
    }


    @Override
    public void close() throws IOException {
        this.csvPrinter.flush();
        this.csvPrinter.close();
    }

    @Override
    protected void writeEvent(SensorEvent event) {
        try {
            this.csvPrinter.print(event.timestamp);
            this.csvPrinter.print(this.timeUtil.eventToDeviceTime(event.timestamp));
            this.csvPrinter.print(this.timeUtil.eventToNTPTime(event.timestamp));
            this.csvPrinter.print(event.accuracy);
            for(float value : event.values)
                this.csvPrinter.print(value);
            this.csvPrinter.println();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

}
