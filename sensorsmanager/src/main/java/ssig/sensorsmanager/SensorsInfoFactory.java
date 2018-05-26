package ssig.sensorsmanager;

import android.content.Context;
import android.content.pm.PackageManager;
import android.hardware.SensorManager;

public class SensorsInfoFactory {

    private Context context;

    public SensorsInfoFactory(Context context){
        this.context = context;
    }

    public SensorInfo makeSensorInfo(SensorType sensorType){

        PackageManager packageManager = this.context.getPackageManager();
        SensorManager sensorManager = (SensorManager)this.context.getSystemService(Context.SENSOR_SERVICE);

        if (packageManager.hasSystemFeature(sensorType.pmFeature())){
            int maxValue = sensorManager.getDefaultSensor(sensorType.androidType()).getMaxDelay();
            int minValue = sensorManager.getDefaultSensor(sensorType.androidType()).getMinDelay();
            String vendor = sensorManager.getDefaultSensor(sensorType.androidType()).getVendor();
            float resolution = sensorManager.getDefaultSensor(sensorType.androidType()).getResolution();
            SensorInfo sensorInfo = new SensorInfo((sensorType));
            sensorInfo.setMaxValue(maxValue);
            sensorInfo.setMinValue(minValue);
            sensorInfo.setVendor(vendor);
            sensorInfo.setResolution(resolution);
            return sensorInfo;
        }
        return null;
    }

}
