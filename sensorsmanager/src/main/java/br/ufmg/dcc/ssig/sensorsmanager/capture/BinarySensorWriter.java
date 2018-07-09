package br.ufmg.dcc.ssig.sensorsmanager.capture;

import android.hardware.SensorEvent;

import java.io.BufferedOutputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;

class BinarySensorWriter extends SensorWriter {


    private DataOutputStream dataOutputStream;

    BinarySensorWriter(File sensorDataFile) throws IOException {
        super();
        this.dataOutputStream = new DataOutputStream(new BufferedOutputStream(new FileOutputStream(sensorDataFile), 8 * 1024));
    }


    @Override
    public void close() throws IOException {
        this.dataOutputStream.flush();
        this.dataOutputStream.close();
    }

    @Override
    protected void writeEvent(SensorEvent event) {
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

}
