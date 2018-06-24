package com.ssig.sensorsmanager.data;


import com.ssig.sensorsmanager.info.SubjectInfo;


import java.io.Serializable;

public final class CaptureData implements Serializable {

    static final long serialVersionUID = 719145489623562378L;

    private String captureDataUUID;
    private DeviceData hostDeviceData;
    private DeviceData clientDeviceData;
    private SubjectInfo subjectInfo;
    private String captureName;
    private String additionalInfo;
    private long startTimestamp;
    private long endTimestamp;
    private boolean closed;

    public CaptureData(String captureDataUUID){
        this.captureDataUUID = captureDataUUID;
        this.hostDeviceData= null;
        this.clientDeviceData = null;
        this.subjectInfo = null;
        this.captureName = null;
        this.additionalInfo =  null;
    }

    public String getCaptureDataUUID() {
        return captureDataUUID;
    }

    public void setHostDeviceData(DeviceData hostDeviceData) {
        this.hostDeviceData = hostDeviceData;
    }

    public void setClientDeviceData(DeviceData clientDeviceData) {
        this.clientDeviceData = clientDeviceData;
    }

    public void setSubjectInfo(SubjectInfo subjectInfo) {
        this.subjectInfo = subjectInfo;
    }

    public void setCaptureName(String captureName) {
        this.captureName = captureName;
    }

    public void setAdditionalInfo(String additionalInfo) {
        this.additionalInfo = additionalInfo;
    }

    public SubjectInfo getSubjectInfo() {
        return subjectInfo;
    }

    public long getStartTimestamp() {
        return startTimestamp;
    }

    public long getEndTimestamp() {
        return endTimestamp;
    }

    public boolean isClosed() {
        return closed;
    }

    public DeviceData getHostDeviceData() {
        return hostDeviceData;
    }

    public DeviceData getClientDeviceData() {
        return clientDeviceData;
    }

    public void setStartTimestamp(long startTimestamp) {
        this.startTimestamp = startTimestamp;
    }

    public void setEndTimestamp(long endTimestamp) {
        this.endTimestamp = endTimestamp;
    }

    public void setClosed(boolean closed) {
        this.closed = closed;
    }
}
