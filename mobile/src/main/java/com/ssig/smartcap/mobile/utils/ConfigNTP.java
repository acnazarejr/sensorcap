package com.ssig.smartcap.mobile.utils;

import android.content.Context;
import android.os.AsyncTask;

import com.instacart.library.truetime.TrueTime;

import java.io.IOException;

/**
 * Created by flabe on 29/5/2018.
 */

public class ConfigNTP {

    public static class InitTrueTimeAsyncTask extends AsyncTask<Void, Void, Void> {
        private Context ctx;
        private String NTP_Server = null;

        public InitTrueTimeAsyncTask (Context context, String NTP_server){
            ctx = context;
            this.NTP_Server = NTP_server;
        }
        public InitTrueTimeAsyncTask(Context context){
            ctx = context;
        }

        protected Void doInBackground(Void... params) {
            try {
                if(NTP_Server == null) {
                    TrueTime.build()
                            .withSharedPreferences(ctx)
                            .withNtpHost("a.ntp.br")
                            .withLoggingEnabled(false)
                            .withConnectionTimeout(31_428)
                            .initialize();
                }else{
                    TrueTime.build()
                            .withSharedPreferences(ctx)
                            .withNtpHost(NTP_Server)
                            .withLoggingEnabled(false)
                            .withConnectionTimeout(31_428)
                            .initialize();
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
            return null;
        }
    }
}
