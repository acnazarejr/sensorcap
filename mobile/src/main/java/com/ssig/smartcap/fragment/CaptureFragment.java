package com.ssig.smartcap.fragment;

import android.annotation.SuppressLint;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
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
import com.ssig.sensorsmanager.SensorType;
import com.ssig.sensorsmanager.capture.DeviceCaptureRunner;
import com.ssig.sensorsmanager.config.CaptureConfig;
import com.ssig.sensorsmanager.config.DeviceConfig;
import com.ssig.sensorsmanager.config.SensorConfig;
import com.ssig.sensorsmanager.data.CaptureData;
import com.ssig.sensorsmanager.data.DeviceData;
import com.ssig.sensorsmanager.data.SensorData;
import com.ssig.sensorsmanager.info.DeviceInfo;
import com.ssig.sensorsmanager.info.SensorInfo;
import com.ssig.sensorsmanager.info.SubjectInfo;
import com.ssig.sensorsmanager.util.JSONUtil;
import com.ssig.smartcap.R;
import com.ssig.smartcap.activity.MainActivity;
import com.ssig.smartcap.common.CountDownAnimation;
import com.ssig.smartcap.model.SensorsGridItem;
import com.ssig.sensorsmanager.time.NTPTime;
import com.ssig.smartcap.utils.Tools;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.text.Normalizer;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import co.ceryle.radiorealbutton.RadioRealButtonGroup;

public class CaptureFragment extends AbstractMainFragment implements MessageClient.OnMessageReceivedListener {

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

    private MaterialDialog dialogOnCapture;
    private TextView textCountdown;
    private View layoutDuringCapture;
    private Chronometer chronometerCapture;
    private SlideToActView buttonCaptureStop;


    private CaptureConfig currentCaptureConfig;
    private DeviceCaptureRunner deviceCaptureRunner;
    private long currentCaptureStart;
    private long currentCaptureEnd;

    public CaptureFragment(){
        super(R.layout.fragment_capture);
    }


    //----------------------------------------------------------------------------------------------
    // Override Functions
    //----------------------------------------------------------------------------------------------
    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        this.deviceCaptureRunner = null;
        this.currentCaptureConfig = null;
        this.currentCaptureStart = -1;
        this.currentCaptureEnd = -1;

        this.initUI();
        this.registerListeners();
        this.checkRadioGroupLogic();

        final String uri = String.format("wear://*%s", getString(R.string.message_path_host_capture_fragment_prefix));
        Wearable.getMessageClient(Objects.requireNonNull(this.getContext())).addListener(this, Uri.parse(uri), MessageClient.FILTER_PREFIX);

    }

    @Override
    public void onDestroy() {
        Wearable.getMessageClient(Objects.requireNonNull(this.getContext())).removeListener(this);
        super.onDestroy();
    }

    @Override
    public void refresh() {
        this.checkRadioGroupLogic();
    }

    //----------------------------------------------------------------------------------------------
    // Message Listener
    //----------------------------------------------------------------------------------------------
    @Override
    public void onMessageReceived(@NonNull MessageEvent messageEvent) {
        if (!this.isWearClientConnected())
            return;
        String path = messageEvent.getPath();
        if (path.equals(getString(R.string.message_path_host_capture_fragment_stop_capture))){
            this.stopCapture();
        }
    }

    //----------------------------------------------------------------------------------------------
    // UI Stuffs
    //----------------------------------------------------------------------------------------------
    private void initUI(){
        this.switchSmartphoneLocation = Objects.requireNonNull(this.getView()).findViewById(R.id.switch_smartphone_location);
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

        this.dialogOnCapture =  new MaterialDialog.Builder(Objects.requireNonNull(this.getContext()))
                .customView(R.layout.layout_dialog_oncapture, true)
                .cancelable(false)
                .canceledOnTouchOutside(false)
                .build();
        View mDialogOnCaptureView = Objects.requireNonNull(this.dialogOnCapture.getCustomView());
        this.textCountdown = mDialogOnCaptureView.findViewById(R.id.text_countdown);
        this.layoutDuringCapture = mDialogOnCaptureView.findViewById(R.id.layout_during_capture);
        this.chronometerCapture = mDialogOnCaptureView.findViewById(R.id.chronometer);
        this.buttonCaptureStop = mDialogOnCaptureView.findViewById(R.id.button_capture_stop);
        this.buttonCaptureStop.setOnSlideCompleteListener(new SlideToActView.OnSlideCompleteListener() {
            @Override
            public void onSlideComplete(SlideToActView slideToActView) {
                stopCapture();
            }
        });
        this.resetDialogOnCapture();
    }

    private void resetDialogOnCapture(){
        this.layoutDuringCapture.setVisibility(View.GONE);
        this.textCountdown.setVisibility(View.VISIBLE);
        this.buttonCaptureStop.resetSlider();
    }

    private void checkRadioGroupLogic(){
        this.radioGroupDevices.findViewById(R.id.radio_option_smartwatch).setEnabled(this.isWearClientConnected());
        this.radioGroupDevices.findViewById(R.id.radio_option_both).setEnabled(this.isWearClientConnected());
        ((RadioButton)this.radioGroupDevices.findViewById(this.isWearClientConnected() ? R.id.radio_option_both : R.id.radio_option_smartphone)).setChecked(true);
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
                new NTPOffConfirmTask().execute();
            }
        });

        for (int i = 0; i<this.radioGroupDevices.getChildCount(); i++) {
            View view = radioGroupDevices.getChildAt(i);
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

    //----------------------------------------------------------------------------------------------
    // Confirmation NTP OFF an WEAR OFF
    //----------------------------------------------------------------------------------------------
    @SuppressLint("StaticFieldLeak")
    private class NTPOffConfirmTask extends AsyncTask<Void, Void, Boolean> {

        @Override
        protected Boolean doInBackground(Void... voids) {
            if (NTPTime.isSynchronized()) {
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
            if (isWearClientConnected()) {
                return true;
            }else{
                Objects.requireNonNull(getActivity()).runOnUiThread(new Runnable() {
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

    //----------------------------------------------------------------------------------------------
    // Capture STUFFS
    //----------------------------------------------------------------------------------------------
    public void startCapture(){

        this.currentCaptureStart = System.currentTimeMillis();

        this.currentCaptureConfig = this.makeCaptureConfig();
        DeviceConfig hostDeviceConfig = this.currentCaptureConfig.getHostDeviceConfig();
        if (hostDeviceConfig.isEnable()) {
            try {
                this.deviceCaptureRunner = new DeviceCaptureRunner(Objects.requireNonNull(this.getContext()), hostDeviceConfig, this.getSystemCapturesFolder());
            } catch (IOException e) {
                Toast.makeText(this.getActivity(), e.getMessage(), Toast.LENGTH_LONG).show();
                return;
            }
        }

        this.resetDialogOnCapture();
        CountDownAnimation countDownAnimation = new CountDownAnimation(Objects.requireNonNull(getContext()), this.textCountdown, hostDeviceConfig.getCountdownStart(), hostDeviceConfig.isSound(), hostDeviceConfig.isVibration());
        countDownAnimation.setCountDownListener(new CountDownAnimation.CountDownListener() {
            @Override
            public void onCountDownEnd(CountDownAnimation animation) {
                chronometerCapture.setBase(SystemClock.elapsedRealtime());
                chronometerCapture.start();
                layoutDuringCapture.setVisibility(View.VISIBLE);
                if (deviceCaptureRunner != null)
                    deviceCaptureRunner.start();
            }
        });

        if (this.isWearClientConnected() && this.currentCaptureConfig.getClientDeviceConfig() != null)
            this.getWearService().startCapture(this.currentCaptureConfig.getClientDeviceConfig());
        this.dialogOnCapture.show();
        countDownAnimation.start();

    }

    public void stopCapture(){
        this.chronometerCapture.stop();
        try {
            if (this.deviceCaptureRunner != null)
                this.deviceCaptureRunner.finish();
            if (this.isWearClientConnected())
                this.getWearService().stopCapture();
            this.currentCaptureEnd = System.currentTimeMillis();
            this.closeCurrentCapture();
        } catch (IOException e) {
            Toast.makeText(this.getActivity(), e.getMessage(), Toast.LENGTH_LONG).show();
            return;
        }
        this.dialogOnCapture.dismiss();
    }

    private CaptureConfig makeCaptureConfig(){

        SensorType.DeviceLocation[] smartphoneLocations = {
                SensorType.DeviceLocation.FRONT_POCKET,
                SensorType.DeviceLocation.BACK_POCKET,
                SensorType.DeviceLocation.SHIRT_POCKET,
                SensorType.DeviceLocation.BAG,
                SensorType.DeviceLocation.OTHER
        };

        int radioGroupDevicesCheckedId = this.radioGroupDevices.getCheckedRadioButtonId();


        String subjectName = this.inputSubjectName.getText().toString();
        subjectName = !subjectName.equals("") ? subjectName : "Unknown";
        SubjectInfo subjectInfo = new SubjectInfo(subjectName);
        subjectInfo.setGender(this.switchSubjectGender.getChecked() == IconSwitch.Checked.LEFT ? SubjectInfo.Gender.MALE : SubjectInfo.Gender.FEMALE);
        subjectInfo.setAge(this.numberPickerAge.getValue());
        subjectInfo.setHeight(this.numberPickerHeight.getValue());
        subjectInfo.setWeight(this.numberPickerWeight.getValue());

        int countdownStart = this.getSharedPreferences().getInt(getString(R.string.preference_main_key_countdown_capture), getResources().getInteger(R.integer.preference_main_default_countdown_capture));
        boolean hasSound = this.getSharedPreferences().getBoolean(getString(R.string.preference_main_key_has_sound), getResources().getBoolean(R.bool.preference_main_default_has_sound));
        boolean hasVibration = this.getSharedPreferences().getBoolean(getString(R.string.preference_main_key_has_vibration), getResources().getBoolean(R.bool.preference_main_default_has_vibration));


        String subjectNameNormalized = Normalizer.normalize(subjectInfo.getName().toLowerCase().replace(" ", ""), Normalizer.Form.NFD).replaceAll("[^\\p{ASCII}]", "");
        @SuppressLint("SimpleDateFormat") SimpleDateFormat simpleDateFormat = new SimpleDateFormat(getString(R.string.util_time_format_file));
        String globalCaptureConfigUUID = String.format("%s_%s", subjectNameNormalized, simpleDateFormat.format(new Date()));

        //---------------------------------
        // SMARTPHONE CAPTURE CONFIG STUFFS
        //---------------------------------
        String smartphoneCaptureUUID = String.format("%s_%s", globalCaptureConfigUUID, SensorType.DeviceType.SMARTPHONE.name().toLowerCase());
        DeviceInfo smartphoneDeviceInfo = DeviceInfo.get(this.getContext());
        DeviceConfig hostDeviceConfig = new DeviceConfig(smartphoneCaptureUUID, globalCaptureConfigUUID, smartphoneDeviceInfo.getDeviceKey(), SensorType.DeviceType.SMARTPHONE);

        hostDeviceConfig.setEnable(radioGroupDevicesCheckedId == R.id.radio_option_smartphone || radioGroupDevicesCheckedId == R.id.radio_option_both);
        if (hostDeviceConfig.isEnable()) {
            hostDeviceConfig.setDeviceLocation(smartphoneLocations[this.switchSmartphoneLocation.getPosition()]);
            hostDeviceConfig.setDeviceSide(this.switchSmartphoneSide.getPosition() == 0 ? SensorType.DeviceSide.LEFT : SensorType.DeviceSide.RIGHT);
            List<SensorsGridItem> sensorsGridItemList = ((SmartphoneFragment)((MainActivity) Objects.requireNonNull(getActivity())).smartphoneFragment).getValidSensorGridItemList();
            for (SensorsGridItem item : sensorsGridItemList){
                String sensorConfigUUID = String.format("%s_%s", smartphoneCaptureUUID, item.getSensorType().code().toLowerCase());
                SensorConfig sensorConfig = new SensorConfig(sensorConfigUUID);
                sensorConfig.setEnabled(item.isEnabled());
                sensorConfig.setFrequency(item.getFrequency());
                hostDeviceConfig.putSensorConfig(item.getSensorType(), sensorConfig);
            }

            hostDeviceConfig.setCountdownStart(countdownStart);
            hostDeviceConfig.setSound(hasSound);
            hostDeviceConfig.setVibration(hasVibration);
        }
        //--------------------------------------------


        CaptureConfig captureConfig = new CaptureConfig(globalCaptureConfigUUID, hostDeviceConfig);
        captureConfig.setSubjectInfo(subjectInfo);
        captureConfig.setActivityName(null);


        //---------------------------------
        // SMARTWATCH CAPTURE CONFIG STUFFS
        //---------------------------------
        if (this.isWearClientConnected()){
            String smartwatchCaptureUUID = String.format("%s_%s", globalCaptureConfigUUID, SensorType.DeviceType.SMARTWATCH.name().toLowerCase());
            DeviceInfo smartwatchDeviceInfo = this.getWearService().getClientDeviceInfo();
            DeviceConfig clientDeviceConfig = new DeviceConfig(smartwatchCaptureUUID, globalCaptureConfigUUID, smartwatchDeviceInfo.getDeviceKey(), SensorType.DeviceType.SMARTWATCH);

            clientDeviceConfig.setEnable(radioGroupDevicesCheckedId == R.id.radio_option_smartwatch || radioGroupDevicesCheckedId == R.id.radio_option_both);
            if (clientDeviceConfig.isEnable()) {
                clientDeviceConfig.setDeviceLocation(SensorType.DeviceLocation.WRIST);
                clientDeviceConfig.setDeviceSide(this.switchSmartwatchSide.getPosition() == 0 ? SensorType.DeviceSide.LEFT : SensorType.DeviceSide.RIGHT);
                List<SensorsGridItem> sensorsGridItemList = ((SmartwatchFragment)((MainActivity) Objects.requireNonNull(getActivity())).smartwatchFragment).getValidSensorGridItemList();
                for (SensorsGridItem item : sensorsGridItemList){
                    String sensorConfigUUID = String.format("%s_%s", smartwatchCaptureUUID, item.getSensorType().code().toLowerCase());
                    SensorConfig sensorConfig = new SensorConfig(sensorConfigUUID);
                    sensorConfig.setEnabled(item.isEnabled());
                    sensorConfig.setFrequency(item.getFrequency());
                    clientDeviceConfig.putSensorConfig(item.getSensorType(), sensorConfig);
                }
            }

            clientDeviceConfig.setCountdownStart(countdownStart);
            clientDeviceConfig.setSound(hasSound);
            clientDeviceConfig.setVibration(hasVibration);

            captureConfig.setClientDeviceConfig(clientDeviceConfig);
        }

        return captureConfig;
    }

    private void closeCurrentCapture() throws IOException {

        if (this.currentCaptureConfig == null){
            throw new NullPointerException("The currentCaptureConfig is null.");
        }

        CaptureData captureData = new CaptureData(this.currentCaptureConfig.getCaptureConfigUUID());

        DeviceConfig hostDeviceConfig = this.currentCaptureConfig.getHostDeviceConfig();
        if (hostDeviceConfig != null && hostDeviceConfig.isEnable()) {

            DeviceInfo hostDeviceInfo = DeviceInfo.get(this.getContext());
            DeviceData hostDeviceData = new DeviceData(hostDeviceConfig.getDeviceConfigUUID(), hostDeviceConfig.getCaptureConfigUUID(), hostDeviceInfo, hostDeviceConfig);

            for(Map.Entry<SensorType, SensorConfig> entry: hostDeviceConfig.getAllSensorsConfig().entrySet()){
                SensorInfo sensorInfo = hostDeviceInfo.getSensorsInfo().get(entry.getKey());
                SensorConfig sensorConfig = entry.getValue();
                hostDeviceData.addSensorData(new SensorData(sensorConfig.getSensorConfigUUID(), sensorInfo, sensorConfig));
            }

            captureData.setHostDeviceData(hostDeviceData);
        }

        DeviceConfig clientDeviceConfig = this.currentCaptureConfig.getClientDeviceConfig();
        if (this.isWearClientConnected() && clientDeviceConfig!=null && clientDeviceConfig.isEnable()) {

            DeviceInfo clientDeviceInfo = this.getWearService().getClientDeviceInfo();
            DeviceData clientDeviceData = new DeviceData(clientDeviceConfig.getDeviceConfigUUID(), clientDeviceConfig.getCaptureConfigUUID(), clientDeviceInfo, clientDeviceConfig);

            for(Map.Entry<SensorType, SensorConfig> entry: clientDeviceConfig.getAllSensorsConfig().entrySet()){
                SensorInfo sensorInfo = clientDeviceInfo.getSensorsInfo().get(entry.getKey());
                SensorConfig sensorConfig = entry.getValue();
                clientDeviceData.addSensorData(new SensorData(sensorConfig.getSensorConfigUUID(), sensorInfo, sensorConfig));
            }

            captureData.setClientDeviceData(clientDeviceData);
        }

        captureData.setSubjectInfo(this.currentCaptureConfig.getSubjectInfo());
        captureData.setActivityName(this.currentCaptureConfig.getActivityName());
        captureData.setAdditionalInfo(this.currentCaptureConfig.getAdditionalInfo());
        captureData.setStartTimestamp(this.currentCaptureStart);
        captureData.setEndTimestamp(this.currentCaptureEnd);
        captureData.setClosed(false);


        if (!this.getSystemArchiveFolder().exists())
            if (!this.getSystemArchiveFolder().mkdirs())
                throw new FileNotFoundException(String.format("Failed to create the system archive folder: %s", this.getSystemArchiveFolder()));

        File captureDataFile = new File(String.format("%s%s%s.json", this.getSystemArchiveFolder(), File.separator, captureData.getCaptureDataUUID()));
        JSONUtil.save(captureData, captureDataFile);
        ((ArchiveFragment)((MainActivity) Objects.requireNonNull(this.getActivity())).archiveFragment).addCaptureData(captureData);
        this.currentCaptureConfig = null;
        this.currentCaptureStart = -1;
        this.currentCaptureEnd = -1;
    }



}