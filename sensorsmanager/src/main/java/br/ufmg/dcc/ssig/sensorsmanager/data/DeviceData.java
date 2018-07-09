package br.ufmg.dcc.ssig.sensorsmanager.data;

import br.ufmg.dcc.ssig.sensorsmanager.SensorType;
import br.ufmg.dcc.ssig.sensorsmanager.SensorWriterType;
import br.ufmg.dcc.ssig.sensorsmanager.config.DeviceConfig;
import br.ufmg.dcc.ssig.sensorsmanager.info.DeviceInfo;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

@SuppressWarnings("ALL")
public final class DeviceData implements Serializable {

    static final long serialVersionUID = 5331128914562345678L;

    private final String deviceDataUUID;
    private final String captureDataUUID;

    private String deviceKey;
    private String deviceName;
    private String androidVersion;
    private int androidSDK;
    private String manufacturer;
    private String model;
    private String marketName;

    private SensorType.DeviceType deviceType;
    private SensorType.DeviceLocation deviceLocation;
    private SensorType.DeviceSide deviceSide;
    private Boolean enable;
    private SensorWriterType sensorWriterType;
    private final Map<SensorType, SensorData> sensorsData;

    private DeviceData(String deviceDataUUID, String captureDataUUID) {
        this.deviceDataUUID = deviceDataUUID;
        this.captureDataUUID = captureDataUUID;
        this.sensorsData = new HashMap<>();
    }

    public DeviceData(String deviceDataUUID, String captureDataUUID, DeviceInfo deviceInfo, DeviceConfig deviceConfig) {
        this(deviceDataUUID, captureDataUUID);

        this.deviceKey = deviceInfo.getDeviceKey();
        this.deviceName = deviceInfo.getDeviceName();
        this.androidVersion = deviceInfo.getAndroidVersion();
        this.androidSDK = deviceInfo.getAndroidSDK();
        this.manufacturer = deviceInfo.getManufacturer();
        this.model = deviceInfo.getModel();
        this.marketName = deviceInfo.getMarketName();

        this.deviceType = deviceConfig.getDeviceType();
        this.deviceLocation = deviceConfig.getDeviceLocation();
        this.deviceSide = deviceConfig.getDeviceSide();
        this.enable = deviceConfig.isEnable();
        this.sensorWriterType = deviceConfig.getSensorWriterType();

    }

    public void addSensorData(SensorData sensorData){
        this.sensorsData.put(sensorData.getSensorType(), sensorData);
    }

    public String getDeviceDataUUID() {
        return deviceDataUUID;
    }

    public String getCaptureDataUUID() {
        return captureDataUUID;
    }

    public Map<SensorType, SensorData> getSensorsData() {
        return sensorsData;
    }

    public String getDeviceKey() {
        return deviceKey;
    }

    public String getDeviceName() {
        return deviceName;
    }

    public SensorWriterType getSensorWriterType() {
        return sensorWriterType;
    }
}
