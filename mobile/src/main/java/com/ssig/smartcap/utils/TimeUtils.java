package com.ssig.smartcap.utils;

import android.content.Context;

import com.instacart.library.truetime.TrueTime;

import java.io.IOException;
import java.util.Date;

public class TimeUtils {

    public static boolean ntpIsInitialized(){
        return TrueTime.isInitialized();
    }

    public static Date getNtpTime(){
        return TrueTime.isInitialized()?TrueTime.now():null;
    }

    public static boolean initializeNTP(Context context, String ntpPoolAddress) throws IOException {
        TrueTime.build()
                .withSharedPreferences(context)
                .withNtpHost(ntpPoolAddress)
                .withLoggingEnabled(false)
                .withConnectionTimeout(10_000)
                .initialize();
        return TrueTime.isInitialized();
    }

    public static void clearNTPCache(Context context){
        TrueTime.clearCachedInfo(context);
    }


}
