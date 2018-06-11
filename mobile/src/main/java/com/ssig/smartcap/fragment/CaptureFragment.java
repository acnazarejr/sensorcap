package com.ssig.smartcap.fragment;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.SystemClock;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.AppCompatButton;
import android.support.v7.widget.AppCompatEditText;
import android.view.View;
import android.widget.Chronometer;
import android.widget.CompoundButton;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;

import com.afollestad.materialdialogs.DialogAction;
import com.afollestad.materialdialogs.MaterialDialog;
import com.google.android.gms.wearable.MessageClient;
import com.google.android.gms.wearable.MessageEvent;
import com.google.android.gms.wearable.Wearable;
import com.ncorti.slidetoact.SlideToActView;
import com.polyak.iconswitch.IconSwitch;
import com.shawnlin.numberpicker.NumberPicker;
import com.ssig.sensorsmanager.capture.CaptureRunner;
import com.ssig.sensorsmanager.config.CaptureConfig;
import com.ssig.sensorsmanager.config.SensorConfig;
import com.ssig.sensorsmanager.info.PersonInfo;
import com.ssig.smartcap.R;
import com.ssig.smartcap.activity.MainActivity;
import com.ssig.smartcap.adapter.AdapterSensorsList;
import com.ssig.smartcap.common.CountDownAnimation;
import com.ssig.smartcap.model.SensorsListItem;
import com.ssig.sensorsmanager.time.NTPTime;
import com.ssig.smartcap.service.WearService;
import com.ssig.smartcap.utils.Tools;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.Objects;
import java.util.UUID;

import co.ceryle.radiorealbutton.RadioRealButtonGroup;

public class CaptureFragment extends AbstractMainFragment implements MessageClient.OnMessageReceivedListener{

    private enum CaptureState{
        IDLE, CAPTURING
    }

    private RadioRealButtonGroup mSwitchSmartphoneLocation;
    private RadioRealButtonGroup mSwitchSmartphoneSide;
    private RadioRealButtonGroup mSwitchSmartwatchSide;

    private AppCompatEditText mInputSubjectName;
    private IconSwitch mSwitchSubjectGender;
    private TextView mTextGender;
    private NumberPicker mNumberPickerAge;
    private NumberPicker mNumberPickerHeight;
    private NumberPicker mNumberPickerWeight;
    private AppCompatButton mButtonCaptureStart;
    private RadioGroup mRadioGroupDevices;

    private MaterialDialog mDialogOnCapture;
    private TextView mTextCountdown;
    private View mLayoutDuringCapture;
    private Chronometer mChronometerCapture;
    private SlideToActView mButtonCaptureStop;

    private CaptureRunner captureRunner;
    private CaptureState captureState;

    public CaptureFragment(){
        super(R.layout.fragment_capture);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        this.captureRunner = null;
        this.captureState = CaptureState.IDLE;
        this.initUi();
        this.registerListeners();
        this.checkRadioGroupLogic();
        final String uri = String.format("wear://*%s", getString(R.string.message_path_host_capture_fragment_prefix));
        Wearable.getMessageClient(this.getContext()).addListener(this, Uri.parse(uri), MessageClient.FILTER_PREFIX);
    }

    @Override
    public void onDestroy() {
        Wearable.getMessageClient(this.getContext()).removeListener(this);
        super.onDestroy();
    }


    @Override
    public void onShow() {
        super.onShow();
        WearService wearService = ((MainActivity) Objects.requireNonNull(getActivity())).getWearService();
        if (wearService != null)
            wearService.setCaptureCapability(true);
    }

    @Override
    public void onHide() {
        super.onHide();
        WearService wearService = ((MainActivity) Objects.requireNonNull(getActivity())).getWearService();
        if (wearService != null && this.captureState == CaptureState.IDLE)
            wearService.setCaptureCapability(false);
    }

    @Override
    public void onMessageReceived(@NonNull MessageEvent messageEvent) {
        WearService wearService = ((MainActivity) Objects.requireNonNull(getActivity())).getWearService();
        if (!(wearService != null && wearService.isConnected()))
            return;
        String path = messageEvent.getPath();
        if (path.equals(getString(R.string.message_path_host_capture_fragment_start_capture))){
            this.startCapture();
        }
        if (path.equals(getString(R.string.message_path_host_capture_fragment_stop_capture))){
            this.stopCapture();
        }
    }

    @Override
    public void refresh() {
        this.checkRadioGroupLogic();
    }

    private void initUi(){
        this.mSwitchSmartphoneLocation = Objects.requireNonNull(this.getView()).findViewById(R.id.switch_smartphone_location);
        this.mSwitchSmartphoneSide = this.getView().findViewById(R.id.switch_smartphone_side);
        this.mSwitchSmartwatchSide = this.getView().findViewById(R.id.switch_smartwatch_side);
        this.mInputSubjectName = this.getView().findViewById(R.id.subject_name_input_text);
        this.mSwitchSubjectGender = this.getView().findViewById(R.id.switch_gender);
        this.mNumberPickerAge = this.getView().findViewById(R.id.number_picker_age);
        this.mNumberPickerHeight = this.getView().findViewById(R.id.number_picker_height);
        this.mNumberPickerWeight = this.getView().findViewById(R.id.number_picker_weight);
        this.mButtonCaptureStart = this.getView().findViewById(R.id.button_capture_start);
        this.mTextGender = this.getView().findViewById(R.id.gender_text);
        this.mRadioGroupDevices = this.getView().findViewById(R.id.radio_group_devices);

        this.mDialogOnCapture =  new MaterialDialog.Builder(Objects.requireNonNull(this.getContext()))
                .customView(R.layout.layout_dialog_oncapture, true)
                .cancelable(false)
                .canceledOnTouchOutside(false)
                .build();
        View mDialogOnCaptureView = this.mDialogOnCapture.getCustomView();
        this.mTextCountdown = mDialogOnCaptureView.findViewById(R.id.text_countdown);
        this.mLayoutDuringCapture = mDialogOnCaptureView.findViewById(R.id.layout_during_capture);
        this.mChronometerCapture = mDialogOnCaptureView.findViewById(R.id.chronometer);
        this.mButtonCaptureStop = mDialogOnCaptureView.findViewById(R.id.button_capture_stop);
        this.mButtonCaptureStop.setOnSlideCompleteListener(new SlideToActView.OnSlideCompleteListener() {
            @Override
            public void onSlideComplete(SlideToActView slideToActView) {
                stopCapture();
            }
        });
        this.resetDialogOnCapture();
    }

    private void resetDialogOnCapture(){
        this.mLayoutDuringCapture.setVisibility(View.GONE);
        this.mTextCountdown.setVisibility(View.VISIBLE);
        this.mButtonCaptureStop.resetSlider();
    }

    private void checkRadioGroupLogic(){
        WearService wearService = ((MainActivity) Objects.requireNonNull(getActivity())).getWearService();
        boolean wearConnected = wearService != null && wearService.isConnected();
        this.mRadioGroupDevices.findViewById(R.id.radio_option_smartwatch).setEnabled(wearConnected);
        this.mRadioGroupDevices.findViewById(R.id.radio_option_both).setEnabled(wearConnected);
        ((RadioButton)this.mRadioGroupDevices.findViewById(wearConnected ? R.id.radio_option_both : R.id.radio_option_smartphone)).setChecked(true);
    }

    @SuppressLint("StaticFieldLeak")
    private class NTPOffConfirmTask extends AsyncTask<Void, Void, Boolean> {

        @Override
        protected Boolean doInBackground(Void... voids) {
            if (NTPTime.isInitialized()) {
                return true;
            }else{
                Objects.requireNonNull(getActivity()).runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        new MaterialDialog.Builder(getActivity())
                                .title(R.string.capture_dialog_ntp_alert_title)
                                .titleColorRes(R.color.colorAlert)
                                .content(R.string.capture_dialog_ntp_alert_content)
                                .icon(Tools.changeDrawableColor(Objects.requireNonNull(getActivity().getDrawable(R.drawable.ic_ntp_off)), ContextCompat.getColor(getActivity(), R.color.colorAlert)))
                                .cancelable(true)
                                .positiveText(R.string.button_yes)
                                .onPositive(new MaterialDialog.SingleButtonCallback() {
                                    @Override
                                    public void onClick(@NonNull MaterialDialog dialog, @NonNull DialogAction which) {
                                        new WearOffConfirmTask().execute();
                                    }
                                })
                                .negativeText(R.string.button_no)
                                .show();
                    }
                });
                return false;
            }
        }

        @Override
        protected void onPostExecute(Boolean checked) {
            if (checked)
                new WearOffConfirmTask().execute();
        }
    }

    @SuppressLint("StaticFieldLeak")
    private class WearOffConfirmTask extends AsyncTask<Void, Void, Boolean> {

        @Override
        protected Boolean doInBackground(Void... voids) {
            if (((MainActivity) Objects.requireNonNull(getActivity())).getWearService().isConnected()) {
                return true;
            }else{
                getActivity().runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        new MaterialDialog.Builder(getActivity())
                                .title(R.string.capture_dialog_wear_alert_title)
                                .titleColorRes(R.color.colorAlert)
                                .content(R.string.capture_dialog_wear_alert_content)
                                .icon(Tools.changeDrawableColor(Objects.requireNonNull(getActivity().getDrawable(R.drawable.ic_smartwatch_off)), ContextCompat.getColor(getActivity(), R.color.colorAlert)))
                                .cancelable(true)
                                .positiveText(R.string.button_yes)
                                .onPositive(new MaterialDialog.SingleButtonCallback() {
                                    @Override
                                    public void onClick(@NonNull MaterialDialog dialog, @NonNull DialogAction which) {
                                        startCapture();
                                    }
                                })
                                .negativeText(R.string.button_no)
                                .show();
                    }
                });
                return false;
            }
        }

        @Override
        protected void onPostExecute(Boolean checked) {
            if (checked)
                startCapture();
        }
    }

    public void startCapture(){

        CaptureConfig captureConfig = this.makeCaptureConfig();
        if (captureConfig.isSmartphoneEnabled()) {
            try {
                this.captureRunner = new CaptureRunner(Objects.requireNonNull(this.getContext()), captureConfig.getSmartphoneSensors(), new NTPTime(), captureConfig.getCaptureFolderName());
            } catch (FileNotFoundException e) {
                Toast.makeText(this.getActivity(), e.getMessage(), Toast.LENGTH_LONG).show();
                return;
            }
        }

        this.resetDialogOnCapture();
        CountDownAnimation countDownAnimation = new CountDownAnimation(getContext(), this.mTextCountdown, captureConfig.getCountdownStart(), captureConfig.hasSound(), captureConfig.hasVibration());
        countDownAnimation.setCountDownListener(new CountDownAnimation.CountDownListener() {
            @Override
            public void onCountDownEnd(CountDownAnimation animation) {
                mChronometerCapture.setBase(SystemClock.elapsedRealtime());
                mChronometerCapture.start();
                mLayoutDuringCapture.setVisibility(View.VISIBLE);
                if (captureRunner != null)
                    captureRunner.start();
            }
        });

        if (((MainActivity)getActivity()).getWearService().isConnected())
            ((MainActivity)getActivity()).getWearService().startCapture(captureConfig);
        this.mDialogOnCapture.show();
        countDownAnimation.start();
        this.captureState = CaptureState.CAPTURING;

    }

    public void stopCapture(){
        this.mChronometerCapture.stop();
        this.mDialogOnCapture.dismiss();
        if (this.captureRunner != null){
            try {
                captureRunner.finish();
            } catch (IOException e) {
                Toast.makeText(this.getActivity(), e.getMessage(), Toast.LENGTH_LONG).show();
                return;
            }
        }
        if (((MainActivity)getActivity()).getWearService().isConnected())
            ((MainActivity)getActivity()).getWearService().stopCapture();
        this.captureRunner = null;
        this.captureState = CaptureState.IDLE;
    }

    private CaptureConfig makeCaptureConfig(){

        CaptureConfig.SmartphoneLocation[] smartphoneLocations = {
                CaptureConfig.SmartphoneLocation.FRONT_POCKET,
                CaptureConfig.SmartphoneLocation.BACK_POCKET,
                CaptureConfig.SmartphoneLocation.SHIRT_POCKET,
                CaptureConfig.SmartphoneLocation.BAG,
                CaptureConfig.SmartphoneLocation.OTHER
        };

        PersonInfo personInfo = new PersonInfo(this.mInputSubjectName.getText().toString());
        personInfo.setGender(this.mSwitchSubjectGender.getChecked() == IconSwitch.Checked.LEFT ? PersonInfo.Gender.MALE : PersonInfo.Gender.FEMALE);
        personInfo.setAge(this.mNumberPickerAge.getValue());
        personInfo.setHeight(this.mNumberPickerHeight.getValue());
        personInfo.setWeight(this.mNumberPickerWeight.getValue());

        CaptureConfig captureConfig = new CaptureConfig(UUID.randomUUID(), null);

        captureConfig.setPersonInfo(personInfo);
        captureConfig.setActivityName(null);

        int radioGroupDevicesCheckedId = this.mRadioGroupDevices.getCheckedRadioButtonId();
        captureConfig.setSmartphoneEnabled(radioGroupDevicesCheckedId == R.id.radio_option_smartphone || radioGroupDevicesCheckedId == R.id.radio_option_both);
        captureConfig.setSmartwatchEnabled(radioGroupDevicesCheckedId == R.id.radio_option_smartwatch || radioGroupDevicesCheckedId == R.id.radio_option_both);


        if (captureConfig.isSmartphoneEnabled()) {
            captureConfig.setSmartphoneLocation(smartphoneLocations[this.mSwitchSmartphoneLocation.getPosition()]);
            captureConfig.setSmartphoneSide(this.mSwitchSmartphoneSide.getPosition() == 0 ? CaptureConfig.SmartphoneSide.LEFT : CaptureConfig.SmartphoneSide.RIGHT);
            AdapterSensorsList adapterSensorsListSmartphone = ((SmartphoneFragment)((MainActivity) Objects.requireNonNull(getActivity())).smartphoneFragment).getAdapterSensorsList();
            for (SensorsListItem item : adapterSensorsListSmartphone.getSensorsListItems()){
                captureConfig.addSmartphoneSensor(item.getSensorType(), new SensorConfig(item.getSensorType(), item.enabled, item.frequency));
            }
        }

        if (captureConfig.isSmartwatchEnabled()) {
            captureConfig.setSmartwatchSide(this.mSwitchSmartwatchSide.getPosition() == 0 ? CaptureConfig.SmartwatchSide.LEFT : CaptureConfig.SmartwatchSide.RIGHT);
            AdapterSensorsList adapterSensorsListSmartwatch = ((SmartwatchFragment)((MainActivity)getActivity()).smartwatchFragment).getAdapterSensorsList();
            for (SensorsListItem item : adapterSensorsListSmartwatch.getSensorsListItems()){
                captureConfig.addSmartwatchSensor(item.getSensorType(), new SensorConfig(item.getSensorType(), item.enabled, item.frequency));
            }
        }


        int countdownStart = this.getActivity().getPreferences(Context.MODE_PRIVATE).getInt(getString(R.string.preference_main_key_countdown_capture), getResources().getInteger(R.integer.preference_main_default_countdown_capture));
        boolean hasSound = this.getActivity().getPreferences(Context.MODE_PRIVATE).getBoolean(getString(R.string.preference_main_key_has_sound), getResources().getBoolean(R.bool.preference_main_default_has_sound));
        boolean hasVibration = this.getActivity().getPreferences(Context.MODE_PRIVATE).getBoolean(getString(R.string.preference_main_key_has_vibration), getResources().getBoolean(R.bool.preference_main_default_has_vibration));
        captureConfig.setCountdownStart(countdownStart);
        captureConfig.setHasSound(hasSound);
        captureConfig.setHasVibration(hasVibration);

        return captureConfig;
    }

    private void registerListeners(){

        this.mSwitchSubjectGender.setCheckedChangeListener(new IconSwitch.CheckedChangeListener() {
            @Override
            public void onCheckChanged(IconSwitch.Checked current) {
                if (current == IconSwitch.Checked.LEFT)
                    mTextGender.setText(R.string.capture_gender_male);
                else
                    mTextGender.setText(R.string.capture_gender_female);
            }
        });

        this.mButtonCaptureStart.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                new NTPOffConfirmTask().execute();
            }
        });

        for (int i = 0; i<this.mRadioGroupDevices.getChildCount(); i++) {
            View view = mRadioGroupDevices.getChildAt(i);
            if (view instanceof RadioButton) {
                ((RadioButton)view).setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                    @Override
                    public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                        int color = ContextCompat.getColor(Objects.requireNonNull(getContext()), isChecked ? R.color.colorAccent : R.color.colorGreyMediumDark);
                        for (Drawable drawable : buttonView.getCompoundDrawables()){
                            if (drawable != null)
                                Tools.changeDrawableColor(drawable, color);
                        }
                    }
                });
            }
        }
    }

}