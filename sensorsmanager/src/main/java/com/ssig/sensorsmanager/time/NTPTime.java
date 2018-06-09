package com.ssig.sensorsmanager.time;


import android.content.Context;

import com.instacart.library.truetime.TrueTime;

import java.io.IOException;

public class NTPTime implements Time {

    public static boolean initialized = false;

    public NTPTime() {}

    @Override
    public Long now() {
        return NTPTime.isInitialized() ? TrueTime.now().getTime() : null;
    }

    @Override
    public Long now(Long valueIfInvalid) {
        return NTPTime.isInitialized() ? TrueTime.now().getTime() : valueIfInvalid;
    }

    public static boolean isInitialized(){
        return initialized && TrueTime.isInitialized();
    }

    public static boolean initialize(Context context, String ntpPoolAddress) throws IOException {
        TrueTime.clearCachedInfo(context);
        TrueTime.build()
                .withSharedPreferences(context)
                .withNtpHost(ntpPoolAddress)
                .withLoggingEnabled(false)
                .withConnectionTimeout(10_000)
                .initialize();
        NTPTime.initialized = TrueTime.isInitialized();
        return NTPTime.isInitialized();
    }

    public static void clear(Context context){
        TrueTime.clearCachedInfo(context);
        NTPTime.initialized = false;
    }


}
