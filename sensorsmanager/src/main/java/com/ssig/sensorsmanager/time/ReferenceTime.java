package com.ssig.sensorsmanager.time;




public class ReferenceTime implements Time {

    private static Long offsetMs = null;

    public ReferenceTime(){
        ReferenceTime.offsetMs = null;
    }


    @Override
    public Long now() {
        return ReferenceTime.isInitialized() ? System.currentTimeMillis() + ReferenceTime.offsetMs : null;
    }

    @Override
    public Long now(Long valueIfInvalid) {
        return ReferenceTime.isInitialized() ? System.currentTimeMillis() + ReferenceTime.offsetMs : valueIfInvalid;
    }

    public static boolean isInitialized() {
        return ReferenceTime.offsetMs != null;
    }

    public static void initialize(Long referenceTimestamp){
        ReferenceTime.offsetMs = referenceTimestamp - System.currentTimeMillis();
    }


}
