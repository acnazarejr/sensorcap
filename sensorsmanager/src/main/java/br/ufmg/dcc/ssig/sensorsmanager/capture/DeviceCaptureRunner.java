package br.ufmg.dcc.ssig.sensorsmanager.capture;

import android.content.Context;
import android.hardware.Sensor;
import android.hardware.SensorManager;
import android.support.annotation.NonNull;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import br.ufmg.dcc.ssig.sensorsmanager.SensorType;
import br.ufmg.dcc.ssig.sensorsmanager.SensorWriterType;
import br.ufmg.dcc.ssig.sensorsmanager.config.DeviceConfig;
import br.ufmg.dcc.ssig.sensorsmanager.config.SensorConfig;
import br.ufmg.dcc.ssig.sensorsmanager.util.SensorsValuesLength;

public class DeviceCaptureRunner {

    public enum Status{
        IDLE, RUNNING, STOPPED, FINISHED
    }

    private Context context;

    private final DeviceConfig deviceConfig;
    private final File systemCapturesFolder;
    private File deviceCaptureFolder;
    private Status status;

    private final SensorManager sensorManager;
    private final Map<SensorType, SensorWriter> sensorListeners;


    public DeviceCaptureRunner(@NonNull Context context, @NonNull DeviceConfig deviceConfig, @NonNull File systemCapturesFolder) throws IOException {

        this.context = context;

        this.deviceConfig = deviceConfig;
        this.systemCapturesFolder = systemCapturesFolder;

        this.sensorManager = (SensorManager)this.context.getSystemService(Context.SENSOR_SERVICE);
        this.sensorListeners = new HashMap<>();

        this.configureFolders();
        this.configureListeners();
        this.status = Status.IDLE;
    }

    public void start(){
        if (this.status != Status.IDLE)
            return;
        Map<SensorType, SensorConfig> sensorsConfig = this.deviceConfig.getAllSensorsConfig();
        for(SensorType sensorType : this.sensorListeners.keySet()){
            SensorConfig sensorConfig = sensorsConfig.get(sensorType);
            SensorWriter sensorWriter = this.sensorListeners.get(sensorType);
            int samplingPeriodUs = 1_000_000/ sensorConfig.getFrequency();
            Sensor sensor = this.sensorManager.getDefaultSensor(sensorType.androidType());
            this.sensorManager.registerListener(sensorWriter, sensor, samplingPeriodUs);
        }
        this.status = Status.RUNNING;
    }

    private void stop() throws IOException{
        for(SensorType sensorType : this.sensorListeners.keySet()){
            SensorWriter sensorWriter = this.sensorListeners.get(sensorType);
            Sensor sensor = this.sensorManager.getDefaultSensor(sensorType.androidType());
            this.sensorManager.unregisterListener(sensorWriter, sensor);
            sensorWriter.close();
        }
        this.status = Status.STOPPED;
    }

    public void finish() throws IOException {
        if (this.status == Status.RUNNING)
            this.stop();
        this.status = Status.FINISHED;
    }


    private void configureFolders() throws FileNotFoundException {

        if(!this.systemCapturesFolder.exists()){
            if (!this.systemCapturesFolder.mkdirs()) {
                throw new FileNotFoundException(String.format("Failed to create the system capture folder: %s", this.systemCapturesFolder));
            }
        }


        File currentCaptureFolder = new File(String.format("%s%s%s", this.systemCapturesFolder, File.separator, this.deviceConfig.getCaptureConfigUUID()));
        if(!currentCaptureFolder.exists()){
            if (!currentCaptureFolder.mkdirs()) {
                throw new FileNotFoundException(String.format("Failed to create the current capture folder: %s", currentCaptureFolder));
            }
        }

        this.deviceCaptureFolder = new File(String.format("%s%s%s", currentCaptureFolder, File.separator, this.deviceConfig.getDeviceConfigUUID()));
        if(!this.deviceCaptureFolder.exists()){
            if (!this.deviceCaptureFolder.mkdirs()) {
                throw new FileNotFoundException(String.format("Failed to create the current capture folder: %s", this.deviceCaptureFolder));
            }
        }

    }

    private void configureListeners() throws IOException {
        SensorWriterType sensorWriterType = this.deviceConfig.getSensorWriterType();
        Map<SensorType, SensorConfig> sensorsConfig = this.deviceConfig.getAllSensorsConfig();
        for(SensorType sensorType : sensorsConfig.keySet()){
            SensorConfig sensorConfig = sensorsConfig.get(sensorType);
            if (sensorConfig.isEnabled()){
                SensorWriter sensorWriter;
                String fileNameWithoutExtension = String.format("%s%s%s", this.deviceCaptureFolder, File.separator, sensorConfig.getSensorConfigUUID());
                switch (sensorWriterType){
                    case BINARY:
                        sensorWriter = new BinarySensorWriter(new File(String.format("%s.%s", fileNameWithoutExtension, sensorWriterType.fileExtension())));
                        break;
                    case CSV:
                        int valuesLength = SensorsValuesLength.get(this.context, sensorType);
                        sensorWriter = new CSVSensorWriter(new File(String.format("%s.%s", fileNameWithoutExtension, sensorWriterType.fileExtension())), valuesLength);
                        break;
                    default:
                        sensorWriter = new BinarySensorWriter(new File(String.format("%s.%s", fileNameWithoutExtension, sensorWriterType.fileExtension())));
                        break;
                }
                this.sensorListeners.put(sensorType, sensorWriter);
            }
        }
        this.status = Status.IDLE;
    }

}
