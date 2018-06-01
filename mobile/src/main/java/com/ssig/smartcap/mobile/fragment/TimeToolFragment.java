package com.ssig.smartcap.mobile.fragment;

import android.content.Context;
import android.content.res.ColorStateList;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.content.ContextCompat;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.zxing.BarcodeFormat;
import com.google.zxing.EncodeHintType;
import com.google.zxing.MultiFormatWriter;
import com.google.zxing.WriterException;
import com.google.zxing.common.BitMatrix;
import com.journeyapps.barcodescanner.BarcodeEncoder;
import com.polyak.iconswitch.IconSwitch;
import com.ssig.smartcap.mobile.R;
import com.ssig.smartcap.mobile.activity.MainActivity;
import com.ssig.smartcap.mobile.utils.TimeUtils;
import com.ssig.smartcap.mobile.utils.Tools;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.EnumMap;
import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;


public class TimeToolFragment extends AbstractMainFragment {

//    private static final String TAG = "TAG";
//    private Handler handler = new Handler();
//    private int delay_ms = 30;
//    private Runnable runnable;
//    private boolean btn_state = false;

    private enum ButtonState{
        PLAY, PLAY_DISABLED, STOP
    }

    private ImageView img_qrcode;
    private TextView text_qrcode_not_available;
    private IconSwitch switch_timestamp_mode;
    private TextView text_timestamp_mode;
    private FloatingActionButton button_play;
    private TextView text_date_ntp;
    private TextView text_timestamp_ntp;
    private TextView text_date_device;
    private TextView text_timestamp_device;
    private View layout_ntp_not_available;

    private ButtonState buttonState;
    private Timer updateTimer;
    private TimerTask updateTimerTask;
    private SimpleDateFormat simpleDateFormat;

    private final int delayUpdateMillisec = 30;

    public TimeToolFragment(){
        super(R.layout.fragment_time_tool);
        buttonState = ButtonState.PLAY;
        simpleDateFormat = new SimpleDateFormat("HH:mm:ss.SSS");
    }

    @Override
    public String getTitle() {
        return "TimeTool";
    }

    @Override
    public int getIcon() {
        return R.drawable.ic_qrcode_scan;
    }

    @Override
    public int getPrimaryColor() {
        return R.color.timetool_primary;
    }

    @Override
    public int getSecondaryColor() {
        return R.color.timetool_secondary;
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        img_qrcode = getActivity().findViewById(R.id.img_qrcode);
        text_qrcode_not_available = getActivity().findViewById(R.id.text_qrcode_not_available);
        switch_timestamp_mode = getActivity().findViewById(R.id.switch_timestamp_mode);
        text_timestamp_mode = getActivity().findViewById(R.id.text_timestamp_mode);
        button_play = getActivity().findViewById(R.id.button_play);
        text_date_ntp = getActivity().findViewById(R.id.text_date_ntp);
        text_timestamp_ntp = getActivity().findViewById(R.id.text_timestamp_ntp);
        text_date_device = getActivity().findViewById(R.id.text_date_device);
        text_timestamp_device = getActivity().findViewById(R.id.text_timestamp_device);
        layout_ntp_not_available = getActivity().findViewById(R.id.layout_ntp_not_available);


        updateTimer = null;

    }

    @Override
    public void setViews() {
        this.progressView = getActivity().findViewById(R.id.layout_timetool_progress);
        this.contentView = getActivity().findViewById(R.id.layout_timetool_content);
        this.errorView = null;
    }


    @Override
    public void onStop() {
        super.onStop();
        if (updateTimer != null){
            updateTimer.cancel();
            updateTimer.purge();
            updateTimerTask = null;
            updateTimer = null;
        }

    }

    @Override
    public boolean makeContent() {
        this.registerListeners();

        this.setNTPMode();
        return true;
    }

    private void setNTPMode() {
        this.resetDisplays();
        this.switch_timestamp_mode.setChecked(IconSwitch.Checked.LEFT);
        boolean ntpIsInitialized = TimeUtils.ntpIsInitialized();
        this.changeButtonState(ntpIsInitialized ? ButtonState.PLAY : ButtonState.PLAY_DISABLED);
        this.layout_ntp_not_available.setVisibility(ntpIsInitialized ? View.GONE : View.VISIBLE);
        this.text_timestamp_mode.setText(R.string.time_tool_timestamp_ntp_mode);
    }

    private void setDeviceMode() {
        this.resetDisplays();
        this.switch_timestamp_mode.setChecked(IconSwitch.Checked.RIGHT);
        this.changeButtonState(ButtonState.PLAY);
        this.layout_ntp_not_available.setVisibility(View.GONE);
        this.text_timestamp_mode.setText(R.string.time_tool_timestamp_device_mode);
    }

    private void resetDisplays() {
        this.text_qrcode_not_available.setVisibility(View.VISIBLE);
        this.img_qrcode.setVisibility(View.GONE);
        this.layout_ntp_not_available.setVisibility(View.GONE);
        this.text_date_ntp.setText("--:--:--.---");
        this.text_date_device.setText("--:--:--.---");
        this.text_timestamp_ntp.setText("-------------");
        this.text_timestamp_device.setText("-------------");
    }

    private void changeButtonState(ButtonState state) {
        buttonState = state;
        switch (buttonState){
            case PLAY:
                this.button_play.setEnabled(true);
                this.button_play.setImageResource(R.drawable.ic_play);
                this.button_play.setBackgroundTintList(ColorStateList.valueOf(ContextCompat.getColor(this.getContext(), R.color.timetool_primary)));
                break;
            case PLAY_DISABLED:
                this.button_play.setEnabled(false);
                this.button_play.setImageResource(R.drawable.ic_play);
                this.button_play.setBackgroundTintList(ColorStateList.valueOf(ContextCompat.getColor(this.getContext(), R.color.grey_400)));
                break;
            case STOP:
                this.button_play.setEnabled(true);
                this.button_play.setImageResource(R.drawable.ic_stop);
                this.button_play.setBackgroundTintList(ColorStateList.valueOf(ContextCompat.getColor(this.getContext(), R.color.timetool_primary)));
                break;
        }

    }

    private TimerTask createUpdateTimerTask(){

        return new TimerTask() {
            @Override
            public void run() {

                getActivity().runOnUiThread(new Runnable() {
                    @Override
                    public void run() {

                        Long unixTimestampDevice = System.currentTimeMillis();
                        String stringUnixTimestampDevice = String.valueOf(unixTimestampDevice);
                        Date dateTimestampDevice = new Date(unixTimestampDevice);

                        Date dateTimestampNTP = TimeUtils.getNtpTime();
                        String stringUnixTimestampNTP = null;
                        if (dateTimestampNTP != null){
                            Long unixTimestampNTP = dateTimestampNTP.getTime();
                            stringUnixTimestampNTP = String.valueOf(unixTimestampNTP);
                        }

                        text_date_device.setText(simpleDateFormat.format(dateTimestampDevice));
                        text_timestamp_device.setText(stringUnixTimestampDevice);
                        if (dateTimestampNTP != null){
                            text_date_ntp.setText(simpleDateFormat.format(dateTimestampNTP));
                            text_timestamp_ntp.setText(stringUnixTimestampNTP);
                        }

                        String stringToEncode = switch_timestamp_mode.getChecked() == IconSwitch.Checked.LEFT ? "ntp:" + stringUnixTimestampNTP : "device:" + stringUnixTimestampDevice;

                        MultiFormatWriter multiFormatWriter = new MultiFormatWriter();
                        Map<EncodeHintType, Object> hints = new EnumMap<>(EncodeHintType.class);
                        hints.put(EncodeHintType.MARGIN, 0);
                        try {
                            BitMatrix bitMatrix = multiFormatWriter.encode(stringToEncode, BarcodeFormat.QR_CODE, 500, 500, hints);

                            BarcodeEncoder barcodeEncoder = new BarcodeEncoder();
                            Bitmap bitmap = barcodeEncoder.createBitmap(bitMatrix);
                            img_qrcode.setImageBitmap(bitmap);
                        } catch (WriterException e) {
                            e.printStackTrace();
                        }
                    }
                });


            }
        };
    }


    private void registerListeners(){
        this.switch_timestamp_mode.setCheckedChangeListener(new IconSwitch.CheckedChangeListener() {
            @Override
            public void onCheckChanged(IconSwitch.Checked current) {
                if(current == IconSwitch.Checked.LEFT)
                    setNTPMode();
                else
                    setDeviceMode();
            }
        });

        button_play.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                if(buttonState == ButtonState.PLAY) {
                    changeButtonState(ButtonState.STOP);
                    switch_timestamp_mode.setVisibility(View.GONE);
                    text_qrcode_not_available.setVisibility(View.GONE);
                    img_qrcode.setVisibility(View.VISIBLE);
                    updateTimer = new Timer();
                    updateTimerTask = createUpdateTimerTask();
                    updateTimer.scheduleAtFixedRate(updateTimerTask, 0, delayUpdateMillisec);
                }else{
                    changeButtonState(ButtonState.PLAY);
                    switch_timestamp_mode.setVisibility(View.VISIBLE);
                    updateTimer.cancel();
                    updateTimer.purge();
                    updateTimerTask = null;
                    updateTimer = null;
                }
            }
        });

        layout_ntp_not_available.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ((MainActivity)getActivity()).startNTPSynchronization();
                setNTPMode();
            }
        });

    }



}
