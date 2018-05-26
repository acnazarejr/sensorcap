package ssig.sensorsmanager;

import android.content.pm.PackageManager;
import android.hardware.Sensor;

import java.util.HashMap;
import java.util.Map;

public enum SensorType {
    ACC(0), GYR(1), MAG(2), LAC(3), BAR(4);


    private final int code;

    SensorType(int code) {
        this.code = code;
    }

    public int code() {
        return code;
    }

    public static SensorType byCode(int code) {
        for (SensorType sensorType : SensorType.values()) {
            if (code == sensorType.code())
                return sensorType;
        }
        throw new IllegalArgumentException("invalid code");
    }

    public String pmFeature() {

        Map<Integer, String> codeMap = new HashMap<>();

        codeMap.put(SensorType.ACC.code(), PackageManager.FEATURE_SENSOR_ACCELEROMETER);
        codeMap.put(SensorType.BAR.code(), PackageManager.FEATURE_SENSOR_BAROMETER);
        codeMap.put(SensorType.GYR.code(), PackageManager.FEATURE_SENSOR_GYROSCOPE);
        codeMap.put(SensorType.MAG.code(), PackageManager.FEATURE_SENSOR_COMPASS);
        codeMap.put(SensorType.LAC.code(), PackageManager.FEATURE_SENSOR_ACCELEROMETER);

        return codeMap.get(this.code);

    }

    public Integer androidType() {

        Map<Integer, Integer> codeMap = new HashMap<>();

        codeMap.put(SensorType.ACC.code(), Sensor.TYPE_ACCELEROMETER);
        codeMap.put(SensorType.BAR.code(), Sensor.TYPE_PRESSURE);
        codeMap.put(SensorType.GYR.code(), Sensor.TYPE_GYROSCOPE);
        codeMap.put(SensorType.MAG.code(), Sensor.TYPE_MAGNETIC_FIELD);
        codeMap.put(SensorType.LAC.code(), Sensor.TYPE_LINEAR_ACCELERATION);

        return codeMap.get(this.code);

    }

}
