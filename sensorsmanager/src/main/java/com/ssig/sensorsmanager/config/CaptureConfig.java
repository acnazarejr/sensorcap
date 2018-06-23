package com.ssig.sensorsmanager.config;

import com.ssig.sensorsmanager.info.SubjectInfo;

import java.io.Serializable;


public class CaptureConfig implements Serializable {

    static final long serialVersionUID = 914562348956712378L;

    private String captureConfigUUID;
    private DeviceConfig hostDeviceConfig;
    private DeviceConfig clientDeviceConfig;
    private SubjectInfo subjectInfo;
    private String captureName;
    private String additionalInfo;

    public CaptureConfig(String captureConfigUUID, DeviceConfig hostDeviceConfig){
        this.captureConfigUUID = captureConfigUUID;
        this.hostDeviceConfig = hostDeviceConfig;
        this.subjectInfo = null;
        this.captureName = null;
        this.additionalInfo = null;
        this.clientDeviceConfig = null;
    }

    public String getCaptureConfigUUID() {
        return captureConfigUUID;
    }

    public DeviceConfig getHostDeviceConfig() {
        return hostDeviceConfig;
    }

    public DeviceConfig getClientDeviceConfig() {
        return clientDeviceConfig;
    }

    public void setClientDeviceConfig(DeviceConfig clientDeviceConfig) {
        this.clientDeviceConfig = clientDeviceConfig;
    }

    public SubjectInfo getSubjectInfo() {
        return subjectInfo;
    }

    public void setSubjectInfo(SubjectInfo subjectInfo) {
        this.subjectInfo = subjectInfo;
    }

    public String getCaptureName() {
        return captureName;
    }

    public void setCaptureName(String captureName) {
        this.captureName = captureName;
    }

    public String getAdditionalInfo() {
        return additionalInfo;
    }

    public void setAdditionalInfo(String additionalInfo) {
        this.additionalInfo = additionalInfo;
    }
}
