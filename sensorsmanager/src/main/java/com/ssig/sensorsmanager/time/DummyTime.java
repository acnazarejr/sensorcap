package com.ssig.sensorsmanager.time;

public class DummyTime implements Time {

    @Override
    public Long now() {
        return -1L;
    }


}
