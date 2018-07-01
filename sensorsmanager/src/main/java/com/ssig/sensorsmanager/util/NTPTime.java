package com.ssig.sensorsmanager.util;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;

import org.apache.commons.net.ntp.NTPUDPClient;
import org.apache.commons.net.ntp.TimeInfo;

import java.net.InetAddress;
import java.util.Objects;

public class NTPTime {

    public enum NTPTimeSyncResponseType{
        ALREADY_SYNCHRONIZED,
        NETWORK_DISABLED,
        NTP_ERROR,
        UNKNOWN_ERROR,
        SUCCESS,
    }

    private static NTPTimeSyncResponseType ALREADY_SYNCHRONIZED = NTPTimeSyncResponseType.ALREADY_SYNCHRONIZED;
    private static NTPTimeSyncResponseType NETWORK_DISABLED = NTPTimeSyncResponseType.NETWORK_DISABLED;
    private static NTPTimeSyncResponseType NTP_ERROR = NTPTimeSyncResponseType.NTP_ERROR;
    public static NTPTimeSyncResponseType UNKNOWN_ERROR = NTPTimeSyncResponseType.UNKNOWN_ERROR;
    public static NTPTimeSyncResponseType SUCCESS = NTPTimeSyncResponseType.SUCCESS;

    private static boolean isynchronized = false;
    private static Long offset = null;

    public static class NTPTimeSyncResponse{

        public NTPTimeSyncResponseType responseType;
        public String errorMessage;

        public NTPTimeSyncResponse(NTPTimeSyncResponseType responseType){
            this(responseType, null);
        }

        NTPTimeSyncResponse(NTPTimeSyncResponseType responseType, String errorMessage){
            this.responseType = responseType;
            this.errorMessage = errorMessage;
        }

    }

    public static NTPTimeSyncResponse synchronize(Context context, String ntpPoolAddress){

        if (NTPTime.isSynchronized())
            return new NTPTimeSyncResponse(NTPTime.ALREADY_SYNCHRONIZED);

        if (!NTPTime.isNetworkConnected(context))
            return new NTPTimeSyncResponse(NTPTime.NETWORK_DISABLED);

        NTPUDPClient ntpClient = new NTPUDPClient();
        ntpClient.setDefaultTimeout(10_000);

        try {
            ntpClient.open();
            ntpClient.setSoTimeout(10_000);
            TimeInfo timeInfo = ntpClient.getTime(InetAddress.getByName(ntpPoolAddress));
            timeInfo.computeDetails();
            NTPTime.offset = timeInfo.getOffset();
            NTPTime.isynchronized = (NTPTime.offset != null);
            if (!NTPTime.isynchronized)
                return new NTPTimeSyncResponse(NTPTime.NTP_ERROR, "Error during NTP offset calculation.");
            return new NTPTimeSyncResponse(NTPTime.SUCCESS);
        } catch (Exception e) {
            e.printStackTrace();
            return new NTPTimeSyncResponse(NTPTime.NTP_ERROR, e.getMessage());
        }

    }

    public static void close(){
        NTPTime.isynchronized = false;
        NTPTime.offset = null;
    }

    public static boolean isSynchronized(){
        return NTPTime.isynchronized && (NTPTime.offset != null);
    }

    public static Long now(){
        return NTPTime.toNTPTime(System.currentTimeMillis());
    }

    public static Long toNTPTime(Long localTime){
        if (NTPTime.isynchronized)
            return localTime + NTPTime.offset;
        return null;
    }

    public static Long getOffset() {
        return offset;
    }

    private static boolean isNetworkConnected(Context context) {
        ConnectivityManager cm = (ConnectivityManager) context.getSystemService (Context.CONNECTIVITY_SERVICE);
        NetworkInfo networkInfo = Objects.requireNonNull(cm).getActiveNetworkInfo();
        return networkInfo != null && networkInfo.isConnectedOrConnecting();
    }




}
