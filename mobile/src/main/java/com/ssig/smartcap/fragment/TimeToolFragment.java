package com.ssig.smartcap.fragment;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.res.ColorStateList;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.content.ContextCompat;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;


import com.google.zxing.BarcodeFormat;
import com.google.zxing.EncodeHintType;
import com.google.zxing.MultiFormatWriter;
import com.google.zxing.WriterException;
import com.google.zxing.common.BitMatrix;
import com.journeyapps.barcodescanner.BarcodeEncoder;
import com.polyak.iconswitch.IconSwitch;
import com.ssig.smartcap.R;
import com.ssig.smartcap.activity.MainActivity;
import com.ssig.smartcap.utils.TimeUtils;


import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.EnumMap;
import java.util.Map;
import java.util.Objects;
import java.util.Timer;
import java.util.TimerTask;


public class TimeToolFragment extends AbstractMainFragment {

    private enum ModeState{
        NTP, DEVICE
    }

    private enum ButtonState{
        PLAY, PLAY_DISABLED, STOP
    }

    private ImageView imgQRCode;
    private TextView textQRCodeNotAvailable;
    private IconSwitch switchTimestampMode;
    private TextView textTimestampMode;
    private FloatingActionButton buttonPlay;
    private TextView textDateNtp;
    private TextView textTimestampNtp;
    private TextView textDateDevice;
    private TextView textTimestampDevice;
    private View layoutNtpNotAvailable;

    private ButtonState buttonState;
    private ModeState modeState;
    private Timer updateTimer;
    private TimerTask updateTimerTask;
    private SimpleDateFormat simpleDateFormat;

    private Integer delayUpdateMillis;

    public TimeToolFragment(){
        super(R.layout.fragment_time_tool);
        this.buttonState = ButtonState.PLAY;
        this.modeState = null;
        this.simpleDateFormat = null;
        this.updateTimer = null;
        this.delayUpdateMillis = null;
    }

    @SuppressLint("SimpleDateFormat")
    @Override
    public void onViewCreated(@NonNull View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        this.simpleDateFormat = new SimpleDateFormat(getString(R.string.util_time_format));
        int fps = this.getActivity().getPreferences(Context.MODE_PRIVATE).getInt(getString(R.string.preference_main_key_qrcode_fps), getResources().getInteger(R.integer.preference_main_default_qrcode_fps));
        this.delayUpdateMillis = (1000/fps);

        this.initUI();
        this.registerListeners();
        this.setNTPMode();
    }

    @Override
    public void onStop() {
        super.onStop();
        if (this.updateTimer != null){
            this.updateTimer.cancel();
            this.updateTimer.purge();
            this.updateTimerTask = null;
            this.updateTimer = null;
        }

    }

    @Override
    public void refresh() {
        if (this.modeState == ModeState.NTP) {
            if (this.buttonState == ButtonState.STOP)
                this.stopDisplay();
            this.setNTPMode();
        }
    }

    public void initUI(){
        this.imgQRCode = Objects.requireNonNull(getActivity()).findViewById(R.id.img_qrcode);
        this.textQRCodeNotAvailable = getActivity().findViewById(R.id.text_qrcode_not_available);
        this.switchTimestampMode = getActivity().findViewById(R.id.switch_timestamp_mode);
        this.textTimestampMode = getActivity().findViewById(R.id.text_timestamp_mode);
        this.buttonPlay = getActivity().findViewById(R.id.button_play);
        this.textDateNtp = getActivity().findViewById(R.id.text_date_ntp);
        this.textTimestampNtp = getActivity().findViewById(R.id.text_timestamp_ntp);
        this.textDateDevice = getActivity().findViewById(R.id.text_date_device);
        this.textTimestampDevice = getActivity().findViewById(R.id.text_timestamp_device);
        this.layoutNtpNotAvailable = getActivity().findViewById(R.id.layout_ntp_not_available);
    }

    private void setNTPMode() {
        this.resetViews();
        this.modeState = ModeState.NTP;
        this.switchTimestampMode.setChecked(IconSwitch.Checked.LEFT);
        boolean ntpIsInitialized = TimeUtils.ntpIsInitialized();
        this.changeButtonState(ntpIsInitialized ? ButtonState.PLAY : ButtonState.PLAY_DISABLED);
        this.layoutNtpNotAvailable.setVisibility(ntpIsInitialized ? View.GONE : View.VISIBLE);
        this.textTimestampMode.setText(R.string.time_tool_timestamp_ntp_mode);
    }

    private void setDeviceMode() {
        this.resetViews();
        this.modeState = ModeState.DEVICE;
        this.switchTimestampMode.setChecked(IconSwitch.Checked.RIGHT);
        this.changeButtonState(ButtonState.PLAY);
        this.layoutNtpNotAvailable.setVisibility(View.GONE);
        this.textTimestampMode.setText(R.string.time_tool_timestamp_device_mode);
    }

    private void resetViews() {
        this.textQRCodeNotAvailable.setVisibility(View.VISIBLE);
        this.imgQRCode.setVisibility(View.GONE);
        this.layoutNtpNotAvailable.setVisibility(View.GONE);
        this.textDateNtp.setText(R.string.time_tool_dummy_hour);
        this.textDateDevice.setText(R.string.time_tool_dummy_hour);
        this.textTimestampNtp.setText(R.string.time_tool_dummy_value);
        this.textTimestampDevice.setText(R.string.time_tool_dummy_value);
    }

    private void changeButtonState(ButtonState state) {
        this.buttonState = state;
        switch (this.buttonState){
            case PLAY:
                this.buttonPlay.setEnabled(true);
                this.buttonPlay.setImageResource(R.drawable.ic_play);
                this.buttonPlay.setBackgroundTintList(ColorStateList.valueOf(ContextCompat.getColor(Objects.requireNonNull(this.getContext()), R.color.colorAccent)));
                break;
            case PLAY_DISABLED:
                this.buttonPlay.setEnabled(false);
                this.buttonPlay.setImageResource(R.drawable.ic_play);
                this.buttonPlay.setBackgroundTintList(ColorStateList.valueOf(ContextCompat.getColor(Objects.requireNonNull(this.getContext()), R.color.colorDisabled)));
                break;
            case STOP:
                this.buttonPlay.setEnabled(true);
                this.buttonPlay.setImageResource(R.drawable.ic_stop);
                this.buttonPlay.setBackgroundTintList(ColorStateList.valueOf(ContextCompat.getColor(Objects.requireNonNull(this.getContext()), R.color.colorAccent)));
                break;
        }

    }

    private TimerTask createUpdateTimerTask(){

        return new TimerTask() {
            @Override
            public void run() {

                Objects.requireNonNull(getActivity()).runOnUiThread(new Runnable() {
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

                        textDateDevice.setText(simpleDateFormat.format(dateTimestampDevice));
                        textTimestampDevice.setText(stringUnixTimestampDevice);
                        if (dateTimestampNTP != null){
                            textDateNtp.setText(simpleDateFormat.format(dateTimestampNTP));
                            textTimestampNtp.setText(stringUnixTimestampNTP);
                        }

                        String stringToEncode = modeState == ModeState.NTP ? "ntp:" + stringUnixTimestampNTP : "device:" + stringUnixTimestampDevice;

                        MultiFormatWriter multiFormatWriter = new MultiFormatWriter();
                        Map<EncodeHintType, Object> hints = new EnumMap<>(EncodeHintType.class);
                        hints.put(EncodeHintType.MARGIN, 0);
                        try {
                            BitMatrix bitMatrix = multiFormatWriter.encode(stringToEncode, BarcodeFormat.QR_CODE, 500, 500, hints);

                            BarcodeEncoder barcodeEncoder = new BarcodeEncoder();
                            Bitmap bitmap = barcodeEncoder.createBitmap(bitMatrix);
                            imgQRCode.setImageBitmap(bitmap);
                        } catch (WriterException e) {
                            e.printStackTrace();
                        }
                    }
                });


            }
        };
    }

    private void stopDisplay(){
        changeButtonState(ButtonState.PLAY);
        switchTimestampMode.setVisibility(View.VISIBLE);
        updateTimer.cancel();
        updateTimer.purge();
        updateTimerTask = null;
        updateTimer = null;
    }

    private void startDisplay() {
        changeButtonState(ButtonState.STOP);
        switchTimestampMode.setVisibility(View.GONE);
        textQRCodeNotAvailable.setVisibility(View.GONE);
        imgQRCode.setVisibility(View.VISIBLE);
        updateTimer = new Timer();
        updateTimerTask = createUpdateTimerTask();
        updateTimer.scheduleAtFixedRate(updateTimerTask, 0, delayUpdateMillis);
    }


    private void registerListeners(){

        this.switchTimestampMode.setCheckedChangeListener(new IconSwitch.CheckedChangeListener() {
            @Override
            public void onCheckChanged(IconSwitch.Checked current) {
                if(current == IconSwitch.Checked.LEFT)
                    setNTPMode();
                else
                    setDeviceMode();
            }
        });

        buttonPlay.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                if(buttonState == ButtonState.PLAY) {
                    startDisplay();
                }else{
                    stopDisplay();
                }
            }
        });

        layoutNtpNotAvailable.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ((MainActivity) Objects.requireNonNull(getActivity())).startNTPSynchronization();
                setNTPMode();
            }
        });

    }



}
