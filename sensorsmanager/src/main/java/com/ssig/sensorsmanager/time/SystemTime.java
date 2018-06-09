package com.ssig.sensorsmanager.time;


public class SystemTime implements Time {

    @Override
    public Long now() {
        return System.currentTimeMillis();
    }

    @Override
    public Long now(Long valueIfInvalid) {
        return System.currentTimeMillis();
    }

}
