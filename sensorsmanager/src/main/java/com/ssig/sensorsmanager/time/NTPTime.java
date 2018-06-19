package com.ssig.sensorsmanager.time;


import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;

import com.instacart.library.truetime.TrueTime;

import java.io.IOException;
import java.net.SocketTimeoutException;
import java.util.Objects;

public class NTPTime implements Time {

    public enum NTPSynchronizationResponse{
        ALREADY_SYNCHRONIZED,
        NETWORK_DISABLED,
        NTP_ERROR,
        NTP_TIMEOUT,
        SUCCESS,
        UNKNOWN_ERROR
    }


    private static boolean isynchronized = false;
    private static String lastExceptionMessage = null;

    public NTPTime() {}

    @Override
    public Long now() {
        return NTPTime.isSynchronized() ? TrueTime.now().getTime() : null;
    }

    @Override
    public Long now(Long valueIfInvalid) {
        return NTPTime.isSynchronized() ? TrueTime.now().getTime() : valueIfInvalid;
    }

    public static boolean isSynchronized(){
        return isynchronized && TrueTime.isInitialized();
    }

    public static NTPSynchronizationResponse synchronize(Context context, String ntpPoolAddress) {

        if (NTPTime.isSynchronized())
            return NTPSynchronizationResponse.ALREADY_SYNCHRONIZED;

        if (!NTPTime.isNetworkConnected(context))
            return NTPSynchronizationResponse.NETWORK_DISABLED;


        TrueTime.clearCachedInfo(context);
        try {
            TrueTime.build()
                    .withNtpHost(ntpPoolAddress)
                    .withLoggingEnabled(false)
                    .withConnectionTimeout(30_000)
                    .withSharedPreferences(context)
                    .initialize();
        } catch (SocketTimeoutException e) {
            e.printStackTrace();
            NTPTime.lastExceptionMessage = e.getMessage();
            return NTPSynchronizationResponse.NTP_TIMEOUT;
        } catch (IOException e) {
            e.printStackTrace();
            NTPTime.lastExceptionMessage = e.getMessage();
            return NTPSynchronizationResponse.NTP_ERROR;
        }
        NTPTime.isynchronized = TrueTime.isInitialized();

        if (NTPTime.isSynchronized())
            return NTPSynchronizationResponse.SUCCESS;
        else
            return NTPSynchronizationResponse.UNKNOWN_ERROR;

    }

    public static void close(Context context){
        TrueTime.clearCachedInfo(context);
        NTPTime.isynchronized = false;
        NTPTime.lastExceptionMessage = null;
    }


    public static String getLastExceptionMessage() {
        return lastExceptionMessage;
    }

    private static boolean isNetworkConnected(Context ctx) {
        ConnectivityManager cm = (ConnectivityManager) ctx.getSystemService (Context.CONNECTIVITY_SERVICE);
        NetworkInfo networkInfo = Objects.requireNonNull(cm).getActiveNetworkInfo();
        return networkInfo != null && networkInfo.isConnectedOrConnecting();
    }


}
