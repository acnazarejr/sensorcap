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
import com.afollestad.materialdialogs.DialogAction;
import com.afollestad.materialdialogs.MaterialDialog;
import com.google.zxing.BarcodeFormat;
import com.google.zxing.EncodeHintType;
import com.google.zxing.MultiFormatWriter;
import com.google.zxing.WriterException;
import com.google.zxing.common.BitMatrix;
import com.journeyapps.barcodescanner.BarcodeEncoder;
import com.polyak.iconswitch.IconSwitch;
import com.ssig.sensorsmanager.time.NTPTime;
import com.ssig.sensorsmanager.time.SystemTime;
import com.ssig.smartcap.R;
import com.ssig.smartcap.activity.MainActivity;
import com.ssig.smartcap.utils.Tools;

import java.text.SimpleDateFormat;
import java.util.*;

public class TimeToolFragment extends AbstractMainFragment {

    private enum ModeState{
        NTP, DEVICE
    }

    private enum ButtonState{
        PLAY, PLAY_DISABLED, STOP
    }

    private ImageView mImageQRCode;
    private TextView mTextQRCodeNotAvailable;
    private IconSwitch mSwitchTimestampMode;
    private TextView mTextTimestampMode;
    private FloatingActionButton mButtonPlay;
    private TextView mTextDateNtp;
    private TextView mTextTimestampNtp;
    private TextView mTextDateDevice;
    private TextView mTextTimestampDevice;
    private View mLayoutNtpNotAvailable;

    private final SystemTime mSystemTime;
    private final NTPTime mNTPTime;

    private ButtonState mButtonState;
    private ModeState mModeState;
    private Timer mUpdateTimer;
    private TimerTask mUpdateTimerTask;
    private SimpleDateFormat mSimpleDateFormat;
    private final MultiFormatWriter multiFormatWriter;
    private final Map<EncodeHintType, Object> encodeHintsType;
    private final BarcodeEncoder barcodeEncoder;

    private Integer mDelayUpdateMillis;

    public TimeToolFragment(){
        super(R.layout.fragment_time_tool);
        this.mButtonState = ButtonState.PLAY;
        this.mModeState = null;
        this.mSimpleDateFormat = null;
        this.mUpdateTimer = null;
        this.mDelayUpdateMillis = null;
        this.mSystemTime = new SystemTime();
        this.mNTPTime = new NTPTime();

        this.multiFormatWriter = new MultiFormatWriter();
        this.encodeHintsType = new EnumMap<>(EncodeHintType.class);
        this.encodeHintsType.put(EncodeHintType.MARGIN, 0);
        this.barcodeEncoder = new BarcodeEncoder();
    }

    @SuppressLint("SimpleDateFormat")
    @Override
    public void onViewCreated(@NonNull View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        this.mSimpleDateFormat = new SimpleDateFormat(getString(R.string.util_time_format));

        this.initUI();
        this.registerListeners();
    }

    @Override
    public void onStart() {
        super.onStart();
        this.setNTPMode();
    }

    @Override
    public void onStop() {
        super.onStop();
        this.stopQRCodeGeneration();
        this.resetViews();
    }

    @Override
    public void refresh() {
        if (this.mModeState == ModeState.NTP) {
            if (this.mButtonState == ButtonState.STOP)
                this.stopQRCodeGeneration();
            this.setNTPMode();
        }
    }

    private void initUI(){
        this.mImageQRCode = Objects.requireNonNull(getActivity()).findViewById(R.id.img_qrcode);
        this.mTextQRCodeNotAvailable = getActivity().findViewById(R.id.text_qrcode_not_available);
        this.mSwitchTimestampMode = getActivity().findViewById(R.id.switch_timestamp_mode);
        this.mTextTimestampMode = getActivity().findViewById(R.id.text_timestamp_mode);
        this.mButtonPlay = getActivity().findViewById(R.id.button_play);
        this.mTextDateNtp = getActivity().findViewById(R.id.text_date_ntp);
        this.mTextTimestampNtp = getActivity().findViewById(R.id.text_timestamp_ntp);
        this.mTextDateDevice = getActivity().findViewById(R.id.text_date_device);
        this.mTextTimestampDevice = getActivity().findViewById(R.id.text_timestamp_device);
        this.mLayoutNtpNotAvailable = getActivity().findViewById(R.id.layout_ntp_not_available);
    }

    private void setNTPMode() {
        this.resetViews();
        this.mModeState = ModeState.NTP;
        this.mSwitchTimestampMode.setChecked(IconSwitch.Checked.LEFT);
        boolean ntpIsInitialized = NTPTime.isSynchronized();
        this.changeButtonState(ntpIsInitialized ? ButtonState.PLAY : ButtonState.PLAY_DISABLED);
        this.mLayoutNtpNotAvailable.setVisibility(ntpIsInitialized ? View.GONE : View.VISIBLE);
        this.mTextTimestampMode.setText(R.string.timetool_timestamp_ntp_mode);
    }

    private void setDeviceMode() {
        this.resetViews();
        this.mModeState = ModeState.DEVICE;
        this.mSwitchTimestampMode.setChecked(IconSwitch.Checked.RIGHT);
        this.changeButtonState(ButtonState.PLAY);
        this.mLayoutNtpNotAvailable.setVisibility(View.GONE);
        this.mTextTimestampMode.setText(R.string.timetool_timestamp_device_mode);
    }

    private void resetViews() {
        this.mTextQRCodeNotAvailable.setVisibility(View.VISIBLE);
        this.mImageQRCode.setVisibility(View.GONE);
        this.mLayoutNtpNotAvailable.setVisibility(View.GONE);
        this.mTextDateNtp.setText(R.string.timetool_dummy_hour);
        this.mTextDateDevice.setText(R.string.timetool_dummy_hour);
        this.mTextTimestampNtp.setText(R.string.timetool_dummy_value);
        this.mTextTimestampDevice.setText(R.string.timetool_dummy_value);
    }

    private void changeButtonState(ButtonState state) {
        this.mButtonState = state;
        switch (this.mButtonState){
            case PLAY:
                this.mButtonPlay.setEnabled(true);
                this.mButtonPlay.setImageResource(R.drawable.ic_play);
                this.mButtonPlay.setBackgroundTintList(ColorStateList.valueOf(ContextCompat.getColor(Objects.requireNonNull(this.getContext()), R.color.colorAccent)));
                break;
            case PLAY_DISABLED:
                this.mButtonPlay.setEnabled(false);
                this.mButtonPlay.setImageResource(R.drawable.ic_play);
                this.mButtonPlay.setBackgroundTintList(ColorStateList.valueOf(ContextCompat.getColor(Objects.requireNonNull(this.getContext()), R.color.colorDisabled)));
                break;
            case STOP:
                this.mButtonPlay.setEnabled(true);
                this.mButtonPlay.setImageResource(R.drawable.ic_stop);
                this.mButtonPlay.setBackgroundTintList(ColorStateList.valueOf(ContextCompat.getColor(Objects.requireNonNull(this.getContext()), R.color.colorAccent)));
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

                        Long unixTimestampDevice = mSystemTime.now();
                        Long unixTimestampNTP = mNTPTime.now();

                        String stringUnixTimestampDevice = String.valueOf(unixTimestampDevice);
                        Date dateTimestampDevice = new Date(unixTimestampDevice);

                        String stringUnixTimestampNTP = null;
                        Date dateTimestampNTP = null;
                        if (unixTimestampNTP != null){
                            dateTimestampNTP = new Date(unixTimestampNTP);
                            stringUnixTimestampNTP = String.valueOf(unixTimestampNTP);
                        }

                        mTextDateDevice.setText(mSimpleDateFormat.format(dateTimestampDevice));
                        mTextTimestampDevice.setText(stringUnixTimestampDevice);
                        if (unixTimestampNTP != null){
                            mTextDateNtp.setText(mSimpleDateFormat.format(dateTimestampNTP));
                            mTextTimestampNtp.setText(stringUnixTimestampNTP);
                        }

                        String stringToEncode = mModeState == ModeState.NTP ? stringUnixTimestampNTP : stringUnixTimestampDevice;
                        try {
                            if (stringToEncode != null) {
                                BitMatrix bitMatrix = multiFormatWriter.encode(stringToEncode, BarcodeFormat.QR_CODE, 150, 150, encodeHintsType);
                                Bitmap bitmap = barcodeEncoder.createBitmap(bitMatrix);
                                mImageQRCode.setImageBitmap(bitmap);
                            }
                        } catch (WriterException e) {
                            e.printStackTrace();
                        }
                    }
                });


            }
        };
    }

    private void stopQRCodeGeneration(){
        changeButtonState(ButtonState.PLAY);
        mSwitchTimestampMode.setVisibility(View.VISIBLE);
        if (this.mUpdateTimer != null){
            this.mUpdateTimer.cancel();
            this.mUpdateTimer.purge();
            this.mUpdateTimerTask = null;
            this.mUpdateTimer = null;
        }
    }

    private void startQRCodeGeneration() {

        if (this.mModeState == ModeState.DEVICE){
            new MaterialDialog.Builder(Objects.requireNonNull(getActivity()))
                    .title(R.string.timetool_dialog_device_alert_title)
                    .titleColorRes(R.color.colorAlert)
                    .content(R.string.timetool_dialog_device_alert_content)
                    .icon(Tools.changeDrawableColor(Objects.requireNonNull(getActivity().getDrawable(R.drawable.ic_smartphone)), ContextCompat.getColor(getActivity(), R.color.colorAlert)))
                    .cancelable(true)
                    .positiveText(R.string.button_yes)
                    .onPositive(new MaterialDialog.SingleButtonCallback() {
                        @Override
                        public void onClick(@NonNull MaterialDialog dialog, @NonNull DialogAction which) {
                            runQRCodeGeneration();
                        }
                    })
                    .negativeText(R.string.button_no)
                    .show();
        }else{
            runQRCodeGeneration();
        }

    }

    private void runQRCodeGeneration() {

        final int fps = Objects.requireNonNull(this.getActivity()).getPreferences(Context.MODE_PRIVATE).getInt(getString(R.string.preference_main_key_qrcode_fps), getResources().getInteger(R.integer.preference_main_default_qrcode_fps));
        mDelayUpdateMillis = (1000/fps);

        changeButtonState(ButtonState.STOP);
        mSwitchTimestampMode.setVisibility(View.GONE);
        mTextQRCodeNotAvailable.setVisibility(View.GONE);
        mImageQRCode.setVisibility(View.VISIBLE);
        mUpdateTimer = new Timer();
        mUpdateTimerTask = createUpdateTimerTask();
        mUpdateTimer.scheduleAtFixedRate(mUpdateTimerTask, 0, mDelayUpdateMillis);
    }

    private void registerListeners(){

        this.mSwitchTimestampMode.setCheckedChangeListener(new IconSwitch.CheckedChangeListener() {
            @Override
            public void onCheckChanged(IconSwitch.Checked current) {
                if(current == IconSwitch.Checked.LEFT)
                    setNTPMode();
                else
                    setDeviceMode();
            }
        });

        mButtonPlay.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                if(mButtonState == ButtonState.PLAY) {
                    startQRCodeGeneration();
                }else{
                    stopQRCodeGeneration();
                }
            }
        });

        mLayoutNtpNotAvailable.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ((MainActivity) Objects.requireNonNull(getActivity())).doNTPSynchronization();
                setNTPMode();
            }
        });

    }

}
