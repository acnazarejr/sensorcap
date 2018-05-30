package com.ssig.smartcap.mobile.fragment;

import android.content.Context;
import android.graphics.Bitmap;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import com.google.zxing.BarcodeFormat;
import com.google.zxing.MultiFormatWriter;
import com.google.zxing.WriterException;
import com.google.zxing.common.BitMatrix;
import com.instacart.library.truetime.TrueTime;
import com.journeyapps.barcodescanner.BarcodeEncoder;
import com.ssig.smartcap.mobile.R;
import com.ssig.smartcap.mobile.utils.ConfigNTP.InitTrueTimeAsyncTask;

import org.w3c.dom.Text;

import java.io.IOException;
import java.sql.Timestamp;
import java.text.SimpleDateFormat;
import java.util.Date;


public class TimeToolFragment extends AbstractMainFragment {

    private static final String TAG = "TAG";
    private Handler handler = new Handler();
    private int delay_ms = 30;
    private Runnable runnable;
    private boolean btn_state = false;
    private Button btn_start;
    private ImageView img_qrcode;
    private TextView txt_qrcode;
    private TextView txt_android_time;
    private TextView txt_datetime;
    private TextView txt_timestamp;

    public TimeToolFragment(){
        super("TimeTool", R.drawable.ic_qrcode_scan, R.color.teal_500, R.layout.fragment_time_tool);
    }

    @Override
    public boolean makeContent() {
        return true;
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        //Initialize NTP Class
        initTrueTime(getContext());

        //Get ID and implement Listeners
        findViewsById();

        implementListeners();
    }

    private void findViewsById() {
        img_qrcode = getActivity().findViewById(R.id.img_qrcode);
        btn_start = getActivity().findViewById(R.id.btn_start);
        txt_qrcode = getActivity().findViewById(R.id.txt_qrcode_avaiable);
        txt_datetime = getActivity().findViewById(R.id.txt_datetime);
        txt_timestamp = getActivity().findViewById(R.id.txt_timestamp);
        txt_android_time = getActivity().findViewById(R.id.txt_android_time);
    }

    private void initTrueTime(Context ctx){
        if (isNetworkConnected(ctx)) {
            if (!TrueTime.isInitialized()) {
                InitTrueTimeAsyncTask trueTime = new InitTrueTimeAsyncTask(ctx);
                trueTime.execute();
            }
        }
    }

    public static boolean isNetworkConnected(Context ctx) {
        ConnectivityManager cm = (ConnectivityManager) ctx.getSystemService (Context.CONNECTIVITY_SERVICE);
        NetworkInfo ni = cm.getActiveNetworkInfo();
        return ni != null && ni.isConnectedOrConnecting();
    }

    public static Date getTrueTime() {
        Date date = TrueTime.isInitialized() ? TrueTime.now() : null;
        return date;
    }

    private void implementListeners() {
        btn_start.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

            if(btn_state == false) {
                btn_start.setText("STOP");
                txt_qrcode.setText("TIMESTAMP");
                btn_state = true;
                handler.postDelayed(new Runnable() {
                    public void run() {
                        Long timestamp = System.currentTimeMillis();
                        Timestamp t = new Timestamp(timestamp);
                        String s_timestamp = String.valueOf(timestamp);
                        printDateTime(t, timestamp);

                        MultiFormatWriter multiFormatWriter = new MultiFormatWriter();
                        try {
                            BitMatrix bitMatrix = multiFormatWriter.encode(s_timestamp, BarcodeFormat.QR_CODE, 320, 320);
                            BarcodeEncoder barcodeEncoder = new BarcodeEncoder();
                            Bitmap bitmap = barcodeEncoder.createBitmap(bitMatrix);
                            img_qrcode.setImageBitmap(bitmap);
                        } catch (WriterException e) {
                            e.printStackTrace();
                        }
                        runnable = this;
                        handler.postDelayed(runnable, delay_ms);
                    }
                }, delay_ms);
            }else{
                btn_start.setText("START");
                btn_state = false;
                handler.removeCallbacks(runnable);//Stop handler
            }
            }
        });
    }

    private void printDateTime(Timestamp t, Long UnixTime) {
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("HH:mm:ss.SSS");

        Date android_date = new Date(t.getTime());
        txt_android_time.setText(simpleDateFormat.format(android_date));

        Date date = getTrueTime();
        String text = date != null ? simpleDateFormat.format(date) : "NTP TIME FAILED";
        txt_datetime.setText(text);

        txt_timestamp.setText(String.valueOf(UnixTime));
    }
}
