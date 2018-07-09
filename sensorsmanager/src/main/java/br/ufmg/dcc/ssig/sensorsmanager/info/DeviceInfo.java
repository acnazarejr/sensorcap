package br.ufmg.dcc.ssig.sensorsmanager.info;


import android.annotation.SuppressLint;
import android.bluetooth.BluetoothAdapter;
import android.content.Context;
import android.os.Build;
import android.provider.Settings;
import com.jaredrummler.android.device.DeviceName;
import br.ufmg.dcc.ssig.sensorsmanager.R;
import br.ufmg.dcc.ssig.sensorsmanager.SensorType;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

public class DeviceInfo implements Serializable {

    static final long serialVersionUID = 9111128914562345678L;

    private String deviceKey;
    private String deviceName;
    private String androidVersion;
    private int androidSDK;
    private String manufacturer;
    private String model;
    private String marketName;
    private Map<SensorType, SensorInfo> sensorsInfo;

    @SuppressLint("HardwareIds")
    public static DeviceInfo get(Context context){
        DeviceInfo deviceInfo = new DeviceInfo();

        DeviceName.DeviceInfo info = DeviceName.getDeviceInfo(context);
        deviceInfo.deviceKey = Settings.Secure.getString(context.getContentResolver(), Settings.Secure.ANDROID_ID);
        BluetoothAdapter bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        deviceInfo.deviceName =  bluetoothAdapter != null ? bluetoothAdapter.getName() : context.getString(R.string.util_unknown_device);
        deviceInfo.androidVersion = Build.VERSION.RELEASE;
        deviceInfo.androidSDK = Build.VERSION.SDK_INT;
        deviceInfo.manufacturer = info.manufacturer;
        deviceInfo.model = info.model;
        deviceInfo.marketName = info.marketName;
        deviceInfo.sensorsInfo = SensorInfo.getAll(context);
        return deviceInfo;
    }

    private DeviceInfo(){
        this.deviceKey = null;
        this.deviceName = null;
        this.androidVersion = null;
        this.androidSDK = -1;
        this.manufacturer = null;
        this.model = null;
        this.marketName = null;
        this.sensorsInfo = null;
    }

    public String getDeviceKey() {
        return deviceKey;
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

    public Map<SensorType, SensorInfo> getSensorsInfo(SensorType.SensorGroup sensorGroup) {
        Map<SensorType, SensorInfo> sensorsInfoGroup = new HashMap<>();
        for (Map.Entry<SensorType, SensorInfo> entry : this.sensorsInfo.entrySet()){
            if (entry.getKey().group() == sensorGroup)
                sensorsInfoGroup.put(entry.getKey(), entry.getValue());
        }
        return sensorsInfoGroup;
    }

    public Map<SensorType, SensorInfo> getSensorsInfo() {
        return this.sensorsInfo;
    }

}
