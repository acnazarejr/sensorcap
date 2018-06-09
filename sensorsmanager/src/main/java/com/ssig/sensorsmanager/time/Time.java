package com.ssig.sensorsmanager.time;

public interface Time {

    Long now();
    Long now(Long valueIfInvalid);

}
