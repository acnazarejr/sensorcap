package com.ssig.smartcap.fragment;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.drawable.Drawable;
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

import com.afollestad.materialdialogs.DialogAction;
import com.afollestad.materialdialogs.MaterialDialog;
import com.ncorti.slidetoact.SlideToActView;
import com.polyak.iconswitch.IconSwitch;
import com.shawnlin.numberpicker.NumberPicker;
import com.ssig.sensorsmanager.capture.CaptureRunner;
import com.ssig.sensorsmanager.config.CaptureConfig;
import com.ssig.sensorsmanager.config.SensorConfig;
import com.ssig.sensorsmanager.info.PersonInfo;
import com.ssig.smartcap.R;
import com.ssig.smartcap.activity.MainActivity;
import com.ssig.smartcap.adapter.AdapterListSensor;
import com.ssig.smartcap.common.CountDownAnimation;
import com.ssig.smartcap.model.SensorListItem;
import com.ssig.sensorsmanager.time.NTPTime;
import com.ssig.smartcap.utils.Tools;
import com.ssig.smartcap.utils.WearUtil;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.UUID;

import co.ceryle.radiorealbutton.RadioRealButtonGroup;

public class CaptureFragment extends AbstractMainFragment {

    private RadioRealButtonGroup switchSmartphoneLocation;
    private RadioRealButtonGroup switchSmartphoneSide;
    private RadioRealButtonGroup switchSmartwatchSide;

    private AppCompatEditText inputSubjectName;
    private IconSwitch switchSubjectGender;
    private TextView textGender;
    private NumberPicker numberPickerAge;
    private NumberPicker numberPickerHeight;
    private NumberPicker numberPickerWeight;
    private AppCompatButton buttonCaptureStart;
    private RadioGroup radioGroupDevices;

    public CaptureFragment(){
        super(R.layout.fragment_capture);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        this.initUi();
        this.registerListeners();
        this.checkRadioGroupLogic();
    }

    @Override
    public void refresh() {
        this.checkRadioGroupLogic();
    }

    private void initUi(){
        if (this.getView()==null)
            return;
        this.switchSmartphoneLocation = this.getView().findViewById(R.id.switch_smartphone_location);
        this.switchSmartphoneSide = this.getView().findViewById(R.id.switch_smartphone_side);
        this.switchSmartwatchSide = this.getView().findViewById(R.id.switch_smartwatch_side);
        this.inputSubjectName = this.getView().findViewById(R.id.subject_name_input_text);
        this.switchSubjectGender = this.getView().findViewById(R.id.switch_gender);
        this.numberPickerAge = this.getView().findViewById(R.id.number_picker_age);
        this.numberPickerHeight = this.getView().findViewById(R.id.number_picker_height);
        this.numberPickerWeight = this.getView().findViewById(R.id.number_picker_weight);
        this.buttonCaptureStart = this.getView().findViewById(R.id.button_capture_start);
        this.textGender = this.getView().findViewById(R.id.gender_text);
        this.radioGroupDevices = this.getView().findViewById(R.id.radio_group_devices);
    }

    private void checkRadioGroupLogic(){
        boolean wearConnected = WearUtil.isConnected();
        this.radioGroupDevices.findViewById(R.id.radio_option_smartwatch).setEnabled(wearConnected);
        this.radioGroupDevices.findViewById(R.id.radio_option_both).setEnabled(wearConnected);
        ((RadioButton)this.radioGroupDevices.findViewById(wearConnected ? R.id.radio_option_both : R.id.radio_option_smartphone)).setChecked(true);
    }

    private void registerListeners(){
        this.switchSubjectGender.setCheckedChangeListener(new IconSwitch.CheckedChangeListener() {
            @Override
            public void onCheckChanged(IconSwitch.Checked current) {
                if (current == IconSwitch.Checked.LEFT)
                    textGender.setText(R.string.capture_gender_male);
                else
                    textGender.setText(R.string.capture_gender_female);
            }
        });

        this.buttonCaptureStart.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                new NTPCheckTask().execute();
            }
        });

        for (int i=0; i<this.radioGroupDevices.getChildCount(); i++) {
            View view = radioGroupDevices.getChildAt(i);
            if (view instanceof RadioButton) {
                ((RadioButton)view).setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                    @Override
                    public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                        int color = ContextCompat.getColor(getContext(), isChecked ? R.color.colorAccent : R.color.colorGreyMediumDark);
                        for (Drawable drawable : buttonView.getCompoundDrawables()){
                            if (drawable != null)
                                Tools.changeDrawableColor(drawable, color);
                        }
                    }
                });
            }
        }
    }

    @SuppressLint("StaticFieldLeak")
    private class NTPCheckTask extends AsyncTask<Void, Void, Boolean> {

        @Override
        protected Boolean doInBackground(Void... voids) {
            if (NTPTime.isInitialized()) {
                return true;
            }else{
                getActivity().runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        new MaterialDialog.Builder(getActivity())
                                .title(R.string.capture_dialog_ntp_alert_title)
                                .content(R.string.capture_dialog_ntp_alert_content)
                                .icon(Tools.changeDrawableColor(getActivity().getDrawable(R.drawable.ic_earth_off), ContextCompat.getColor(getActivity(), R.color.colorPrimary)))
                                .cancelable(true)
                                .positiveText(R.string.button_yes)
                                .onPositive(new MaterialDialog.SingleButtonCallback() {
                                    @Override
                                    public void onClick(@NonNull MaterialDialog dialog, @NonNull DialogAction which) {
                                        new WearCheckTask().execute();
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
                new WearCheckTask().execute();
        }
    }

    @SuppressLint("StaticFieldLeak")
    private class WearCheckTask extends AsyncTask<Void, Void, Boolean> {

        @Override
        protected Boolean doInBackground(Void... voids) {
            if (WearUtil.isConnected()) {
                return true;
            }else{
                getActivity().runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        new MaterialDialog.Builder(getActivity())
                                .title(R.string.capture_dialog_wear_alert_title)
                                .content(R.string.capture_dialog_wear_alert_content)
                                .icon(Tools.changeDrawableColor(getActivity().getDrawable(R.drawable.ic_smartwatch_off), ContextCompat.getColor(getActivity(), R.color.colorPrimary)))
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
        final CaptureRunner captureRunner;
        try {
            captureRunner = new CaptureRunner(this.getContext(), captureConfig.getSmartphoneSensors(), new NTPTime(), "capturetemp");


        final MaterialDialog dialog =  new MaterialDialog.Builder(this.getContext())
                .customView(R.layout.layout_dialog_oncapture, true)
                .cancelable(false)
                .canceledOnTouchOutside(false)
                .build();

        View view = dialog.getCustomView();

        final View layoutChronometer = view.findViewById(R.id.layout_chronometer);
        layoutChronometer.setVisibility(View.GONE);

        final Chronometer chronometer = view.findViewById(R.id.chronometer);
        chronometer.setBase(SystemClock.elapsedRealtime());

        final TextView textCountdown =  view.findViewById(R.id.text_countdown);
        CountDownAnimation countDownAnimation = new CountDownAnimation(getContext(), textCountdown, captureConfig.getCountdownStart(), captureConfig.hasSound(), captureConfig.hasVibration());
        countDownAnimation.setCountDownListener(new CountDownAnimation.CountDownListener() {
            @Override
            public void onCountDownEnd(CountDownAnimation animation) {
                chronometer.start();
                layoutChronometer.setVisibility(View.VISIBLE);
                captureRunner.start();
            }
        });

        final SlideToActView buttonCaptureStop = view.findViewById(R.id.button_capture_stop);
        buttonCaptureStop.setOnSlideCompleteListener(new SlideToActView.OnSlideCompleteListener() {
            @Override
            public void onSlideComplete(SlideToActView slideToActView) {
                chronometer.stop();
                dialog.dismiss();
                try {
                    captureRunner.finish();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        });

        dialog.show();
        countDownAnimation.start();

        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }

    }

    private CaptureConfig makeCaptureConfig(){

        CaptureConfig.SmartphoneLocation[] smartphoneLocations = {
                CaptureConfig.SmartphoneLocation.FRONT_POCKET,
                CaptureConfig.SmartphoneLocation.BACK_POCKET,
                CaptureConfig.SmartphoneLocation.SHIRT_POCKET,
                CaptureConfig.SmartphoneLocation.BAG,
                CaptureConfig.SmartphoneLocation.OTHER
        };

        PersonInfo personInfo = new PersonInfo(this.inputSubjectName.getText().toString());
        personInfo.setGender(this.switchSubjectGender.getChecked() == IconSwitch.Checked.LEFT ? PersonInfo.Gender.MALE : PersonInfo.Gender.FEMALE);
        personInfo.setAge(this.numberPickerAge.getValue());
        personInfo.setHeight(this.numberPickerHeight.getValue());
        personInfo.setWeight(this.numberPickerWeight.getValue());

        CaptureConfig captureConfig = new CaptureConfig(UUID.randomUUID(), null);

        captureConfig.setPersonInfo(personInfo);
        captureConfig.setActivityName(null);

        int radioGroupDevicesCheckedId = this.radioGroupDevices.getCheckedRadioButtonId();
        captureConfig.setSmartphoneEnabled(radioGroupDevicesCheckedId == R.id.radio_option_smartphone || radioGroupDevicesCheckedId == R.id.radio_option_both);
        captureConfig.setSmartwatchEnabled(radioGroupDevicesCheckedId == R.id.radio_option_smartwatch || radioGroupDevicesCheckedId == R.id.radio_option_both);


        if (captureConfig.isSmartphoneEnabled()) {
            captureConfig.setSmartphoneLocation(smartphoneLocations[this.switchSmartphoneLocation.getPosition()]);
            captureConfig.setSmartphoneSide(this.switchSmartphoneSide.getPosition() == 0 ? CaptureConfig.SmartphoneSide.LEFT : CaptureConfig.SmartphoneSide.RIGHT);
            AdapterListSensor adapterListSensorSmartphone = ((SmartphoneFragment)((MainActivity)getActivity()).smartphoneFragment).getAdapterListSensor();
            for (SensorListItem item : adapterListSensorSmartphone.getSensorListItems()){
                captureConfig.addSmartphoneSensor(item.getSensorType(), new SensorConfig(item.getSensorType(), item.enabled, item.frequency));
            }
        }

        if (captureConfig.isSmartwatchEnabled()) {
            captureConfig.setSmartwatchSide(this.switchSmartwatchSide.getPosition() == 0 ? CaptureConfig.SmartwatchSide.LEFT : CaptureConfig.SmartwatchSide.RIGHT);
            AdapterListSensor adapterListSensorSmartwatch = ((SmartwatchFragment)((MainActivity)getActivity()).smartwatchFragment).getAdapterListSensor();
            for (SensorListItem item : adapterListSensorSmartwatch.getSensorListItems()){
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

}