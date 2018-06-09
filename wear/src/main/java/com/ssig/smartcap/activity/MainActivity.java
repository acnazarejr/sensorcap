package com.ssig.smartcap.activity;

import android.annotation.SuppressLint;
import android.graphics.Bitmap;
import android.graphics.PorterDuff;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.support.annotation.ColorInt;
import android.support.v4.content.ContextCompat;
import android.support.wearable.activity.WearableActivity;
import android.view.View;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import com.ssig.sensorsmanager.time.NTPTime;
import com.ssig.sensorsmanager.time.SystemTime;
import com.ssig.smartcap.R;
import com.ssig.smartcap.service.SmartcapListenerService;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.EnumMap;
import java.util.Map;
import java.util.Objects;
import java.util.Timer;
import java.util.TimerTask;

public class MainActivity extends WearableActivity {

    private ImageView imageHostConnectedIcon;
    private ImageView imageNTPSynchronizedIcon;
    private View layoutContent;
    private View layoutLogo;
    private ImageView imageDeviceTimeIcon;
    private TextView textDeviceTime;
    private ImageView imageNTPTimeIcon;
    private TextView textNTPTime;
    private ImageButton buttonControlCapture;

    private SimpleDateFormat simpleDateFormat;
    private Timer updateTimer;
    private TimerTask updateTimerTask;
    private SystemTime systemTime;
    private NTPTime ntpTime;


    @SuppressLint("SimpleDateFormat")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        setAmbientEnabled();
        this.simpleDateFormat = new SimpleDateFormat(getString(R.string.util_time_format));
        this.updateTimer = null;
        this.updateTimerTask = null;
        this.systemTime = new SystemTime();
        this.ntpTime = new NTPTime();
        this.initUI();
    }

    @Override
    protected void onStart() {
        super.onStart();
    }

    @Override
    protected void onResume() {
        super.onResume();
        this.updateStatusIcons();
        this.startTimer();
    }

    @Override
    protected void onPause() {
        super.onPause();
        this.stopTimer();
    }

    private void initUI(){
        this.imageHostConnectedIcon = findViewById(R.id.image_host_connected);
        this.imageNTPSynchronizedIcon = findViewById(R.id.image_ntp_synchronized);
        this.layoutContent = findViewById(R.id.layout_content);
        this.layoutLogo = findViewById(R.id.layout_logo);
        this.imageDeviceTimeIcon = findViewById(R.id.image_device_time);
        this.textDeviceTime = findViewById(R.id.text_device_time);
        this.imageNTPTimeIcon = findViewById(R.id.image_ntp_time);
        this.textNTPTime = findViewById(R.id.text_ntp_time);
        this.buttonControlCapture = findViewById(R.id.button_control_capture);
    }

    private TimerTask createUpdateTimerTask(){

        return new TimerTask() {
            @Override
            public void run() {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        Long unixTimestampDevice = systemTime.now();
                        Long unixTimestampNTP = ntpTime.now();
                        Date dateTimestampDevice = new Date(unixTimestampDevice);
                        textDeviceTime.setText(simpleDateFormat.format(dateTimestampDevice));
                        textNTPTime.setText(unixTimestampNTP != null ? simpleDateFormat.format(new Date(unixTimestampNTP)) : getString(R.string.time_tool_dummy_hour));
                    }
                });
            }
        };
    }

    private void updateStatusIcons(){
        this.imageHostConnectedIcon.setImageResource(SmartcapListenerService.connectedOnHost ? R.drawable.ic_smartphone_on : R.drawable.ic_smartphone_off);
        this.imageHostConnectedIcon.setColorFilter(ContextCompat.getColor(this, SmartcapListenerService.connectedOnHost ? R.color.colorAccent : R.color.colorAlert));
        this.imageNTPSynchronizedIcon.setImageResource(NTPTime.isInitialized() ? R.drawable.ic_earth : R.drawable.ic_earth_off);
        this.imageNTPSynchronizedIcon.setColorFilter(ContextCompat.getColor(this, NTPTime.isInitialized() ? R.color.colorAccent : R.color.colorAlert));
    }

    private void stopTimer(){
        if (this.updateTimer != null){
            this.updateTimer.cancel();
            this.updateTimer.purge();
            this.updateTimerTask = null;
            this.updateTimer = null;
        }
    }

    private void startTimer() {
        updateTimer = new Timer();
        updateTimerTask = createUpdateTimerTask();
        updateTimer.scheduleAtFixedRate(updateTimerTask, 0, 20);
    }


//    public static Drawable changeDrawableColor(Drawable drawable, @ColorInt int color){
//        drawable.mutate();
//        drawable.setColorFilter(color, PorterDuff.Mode.SRC_ATOP);
//        return drawable;
//    }

}
