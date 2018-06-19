package com.ssig.sensorsmanager.info;


import android.annotation.SuppressLint;
import android.bluetooth.BluetoothAdapter;
import android.content.Context;
import android.os.Build;
import android.provider.Settings;

import com.jaredrummler.android.device.DeviceName;
import com.ssig.sensorsmanager.R;
import com.ssig.sensorsmanager.SensorType;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

public class DeviceInfo implements Serializable {

    static final long serialVersionUID = 9111128914562345678L;

    private String uuid;
    private String deviceName;
    private String androidVersion;
    private int androidSDK;
    private String manufacturer;
    private String model;
    private String marketName;
    private Map<SensorType, SensorInfo> sensorsInfo;

    public static DeviceInfo get(Context context){
        return new DeviceInfo(context);
    }

    @SuppressLint("HardwareIds")
    private DeviceInfo(Context context){
        DeviceName.DeviceInfo info = DeviceName.getDeviceInfo(context);
        this.uuid = Settings.Secure.getString(context.getContentResolver(), Settings.Secure.ANDROID_ID);
        BluetoothAdapter bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        this.deviceName =  bluetoothAdapter != null ? bluetoothAdapter.getName() : context.getString(R.string.util_unknown_device);
        this.androidVersion = Build.VERSION.RELEASE;
        this.androidSDK = Build.VERSION.SDK_INT;
        this.manufacturer = info.manufacturer;
        this.model = info.model;
        this.marketName = info.marketName;
        this.sensorsInfo = SensorInfo.getAll(context);
    }


    public String getUUID() {
        return uuid;
    }

    public String getDeviceName() {
        return deviceName;
    }

    public String getAndroidVersion() {
        return androidVersion;
    }

    public int getAndroidSDK() {
        return androidSDK;
    }

    public String getManufacturer() {
        return manufacturer;
    }

    public String getModel() {
        return model;
    }

    public String getMarketName() {
        return marketName;
    }

    public Map<SensorType, SensorInfo> getSensorsInfo() {
        return sensorsInfo;
    }

    public Map<SensorType, SensorInfo> getSensorsInfo(SensorType.SensorGroup sensorGroup) {
        Map<SensorType, SensorInfo> sensorsInfoGroup = new HashMap<>();
        for (Map.Entry<SensorType, SensorInfo> entry : this.sensorsInfo.entrySet()){
            if (entry.getKey().group() == sensorGroup)
                sensorsInfoGroup.put(entry.getKey(), entry.getValue());
        }
        return sensorsInfoGroup;
    }
}
