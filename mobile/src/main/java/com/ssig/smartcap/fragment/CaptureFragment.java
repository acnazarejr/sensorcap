package com.ssig.smartcap.fragment;

import android.annotation.SuppressLint;
import android.content.Context;
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
import com.google.android.gms.tasks.Task;
import com.google.android.gms.tasks.Tasks;
import com.google.android.gms.wearable.Asset;
import com.google.android.gms.wearable.DataClient;
import com.google.android.gms.wearable.DataEvent;
import com.google.android.gms.wearable.DataEventBuffer;
import com.google.android.gms.wearable.DataMap;
import com.google.android.gms.wearable.DataMapItem;
import com.google.android.gms.wearable.MessageClient;
import com.google.android.gms.wearable.MessageEvent;
import com.google.android.gms.wearable.Wearable;
import com.ncorti.slidetoact.SlideToActView;
import com.polyak.iconswitch.IconSwitch;
import com.shawnlin.numberpicker.NumberPicker;
import com.ssig.sensorsmanager.capture.CaptureData;
import com.ssig.sensorsmanager.capture.CaptureRunner;
import com.ssig.sensorsmanager.config.CaptureConfig;
import com.ssig.sensorsmanager.config.SensorConfig;
import com.ssig.sensorsmanager.info.DeviceInfo;
import com.ssig.sensorsmanager.info.PersonInfo;
import com.ssig.sensorsmanager.util.Compression;
import com.ssig.smartcap.R;
import com.ssig.smartcap.activity.MainActivity;
import com.ssig.smartcap.common.CountDownAnimation;
import com.ssig.smartcap.model.SensorsGridItem;
import com.ssig.sensorsmanager.time.NTPTime;
import com.ssig.smartcap.utils.Tools;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.text.Normalizer;
import java.text.NumberFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;

import co.ceryle.radiorealbutton.RadioRealButtonGroup;

public class CaptureFragment extends AbstractMainFragment implements
        MessageClient.OnMessageReceivedListener,
        DataClient.OnDataChangedListener{

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

    private CaptureConfig captureConfig;
    private CaptureRunner captureRunner;

    private MaterialDialog dialogWaitingSmartwatchFiles;

    public CaptureFragment(){
        super(R.layout.fragment_capture);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        this.captureRunner = null;
        this.captureConfig = null;
        this.initUi();
        this.registerListeners();
        this.checkRadioGroupLogic();
        final String uri = String.format("wear://*%s", getString(R.string.message_path_host_capture_fragment_prefix));
        Wearable.getMessageClient(Objects.requireNonNull(this.getContext())).addListener(this, Uri.parse(uri), MessageClient.FILTER_PREFIX);
        Wearable.getDataClient(Objects.requireNonNull(this.getContext())).addListener(this, Uri.parse(uri), DataClient.FILTER_PREFIX);
    }

    @Override
    public void onDestroy() {
        Wearable.getMessageClient(Objects.requireNonNull(this.getContext())).removeListener(this);
        Wearable.getDataClient(Objects.requireNonNull(this.getContext())).removeListener(this);
        super.onDestroy();
    }


//    @Override
//    public void onShow() {
//        super.onShow();
//        WearService wearService = ((MainActivity) Objects.requireNonNull(getActivity())).getWearService();
//        if (wearService != null)
//            wearService.setCaptureCapability(true);
//    }
//
//    @Override
//    public void onHide() {
//        super.onHide();
//        WearService wearService = ((MainActivity) Objects.requireNonNull(getActivity())).getWearService();
//        if (wearService != null && (this.mCaptureRunner == null || this.mCaptureRunner.getStatus() == CaptureRunner.Status.IDLE))
//            wearService.setCaptureCapability(false);
//    }

    @Override
    public void onMessageReceived(@NonNull MessageEvent messageEvent) {

        if (!this.isWearClientConnected())
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
    public void onDataChanged(@NonNull DataEventBuffer dataEventBuffer) {
        for (DataEvent event : dataEventBuffer) {
            if (event.getType() == DataEvent.TYPE_CHANGED) {
                String path = event.getDataItem().getUri().getPath();
                if (path.equals(getString(R.string.message_path_host_capture_fragment_sensor_files))) {
                    DataMapItem dataMapItem = DataMapItem.fromDataItem(event.getDataItem());
                    DataMap dataMap = dataMapItem.getDataMap();
                    Asset asset = dataMap.getAsset(getString(R.string.asset_sensors_smartwatch));
                    this.dialogWaitingSmartwatchFiles.dismiss();
                    new ReceiveSmartwatchSensorFilesTask().execute(asset);
                }
            }
        }
    }

    @Override
    public void refresh() {
        this.checkRadioGroupLogic();
    }

    private void initUi(){
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

        this.dialogWaitingSmartwatchFiles = null;
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

    public void startCapture(){

        this.captureConfig = this.makeCaptureConfig();
        if (this.captureConfig.isSmartphoneEnabled()) {
            try {
                String captureAlias = String.format("smartphone_%s", this.captureConfig.getAlias());
                this.captureRunner = new CaptureRunner(Objects.requireNonNull(this.getContext()), this.captureConfig.getSmartphoneSensors(), this.captureConfig.getAppFolderName(), captureAlias);
            } catch (IOException e) {
                Toast.makeText(this.getActivity(), e.getMessage(), Toast.LENGTH_LONG).show();
                return;
            }
        }

        this.resetDialogOnCapture();
        CountDownAnimation countDownAnimation = new CountDownAnimation(Objects.requireNonNull(getContext()), this.textCountdown, this.captureConfig.getCountdownStart(), this.captureConfig.hasSound(), this.captureConfig.hasVibration());
        countDownAnimation.setCountDownListener(new CountDownAnimation.CountDownListener() {
            @Override
            public void onCountDownEnd(CountDownAnimation animation) {
                chronometerCapture.setBase(SystemClock.elapsedRealtime());
                chronometerCapture.start();
                layoutDuringCapture.setVisibility(View.VISIBLE);
                if (captureRunner != null)
                    captureRunner.start();
            }
        });

        if (this.isWearClientConnected())
            this.getWearService().startCapture(this.captureConfig);
        this.dialogOnCapture.show();
        countDownAnimation.start();

    }

    public void stopCapture(){
        this.chronometerCapture.stop();
        this.dialogOnCapture.dismiss();
        if (this.captureRunner != null){
            try {
                this.captureRunner.stop();
            } catch (IOException e) {
                Toast.makeText(this.getActivity(), e.getMessage(), Toast.LENGTH_LONG).show();
                return;
            }
        }
        if (this.isWearClientConnected()) {
            this.dialogWaitingSmartwatchFiles = new MaterialDialog.Builder(Objects.requireNonNull(this.getContext()))
                    .title(R.string.capture_dialog_wear_sensors_title)
                    .content(R.string.capture_dialog_wear_sensors_content)
                    .icon(Tools.changeDrawableColor(Objects.requireNonNull(this.getContext().getDrawable(R.drawable.ic_smartwatch)), ContextCompat.getColor(this.getContext(), R.color.colorPrimary)))
                    .cancelable(false)
                    .progress(true, 0)
                    .show();
            this.getWearService().stopCapture();
        } else {
            new SaveCaptureDataTask().execute();
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

        String subjectName = this.inputSubjectName.getText().toString();
        subjectName = !subjectName.equals("") ? subjectName : "Unknown";
        PersonInfo personInfo = new PersonInfo(subjectName);
        personInfo.setGender(this.switchSubjectGender.getChecked() == IconSwitch.Checked.LEFT ? PersonInfo.Gender.MALE : PersonInfo.Gender.FEMALE);
        personInfo.setAge(this.numberPickerAge.getValue());
        personInfo.setHeight(this.numberPickerHeight.getValue());
        personInfo.setWeight(this.numberPickerWeight.getValue());

        String alias = Normalizer.normalize(personInfo.getName().toLowerCase().replace(" ", "-"), Normalizer.Form.NFD);
        alias = alias.replaceAll("[^\\p{ASCII}]", "");
        CaptureConfig captureConfig = new CaptureConfig(UUID.randomUUID(), alias);


        captureConfig.setPersonInfo(personInfo);
        captureConfig.setActivityName(null);

        int radioGroupDevicesCheckedId = this.radioGroupDevices.getCheckedRadioButtonId();
        captureConfig.setSmartphoneEnabled(radioGroupDevicesCheckedId == R.id.radio_option_smartphone || radioGroupDevicesCheckedId == R.id.radio_option_both);
        captureConfig.setSmartwatchEnabled(radioGroupDevicesCheckedId == R.id.radio_option_smartwatch || radioGroupDevicesCheckedId == R.id.radio_option_both);


        if (captureConfig.isSmartphoneEnabled()) {
            captureConfig.setSmartphoneLocation(smartphoneLocations[this.switchSmartphoneLocation.getPosition()]);
            captureConfig.setSmartphoneSide(this.switchSmartphoneSide.getPosition() == 0 ? CaptureConfig.SmartphoneSide.LEFT : CaptureConfig.SmartphoneSide.RIGHT);
            List<SensorsGridItem> sensorsGridItemList = ((SmartphoneFragment)((MainActivity) Objects.requireNonNull(getActivity())).smartphoneFragment).getValidSensorGridItemList();
            for (SensorsGridItem item : sensorsGridItemList){
                captureConfig.addSmartphoneSensor(item.getSensorType(), new SensorConfig(item.getSensorType(), item.isEnabled(), item.getFrequency()));
            }
        }

        if (captureConfig.isSmartwatchEnabled()) {
            captureConfig.setSmartwatchSide(this.switchSmartwatchSide.getPosition() == 0 ? CaptureConfig.SmartwatchSide.LEFT : CaptureConfig.SmartwatchSide.RIGHT);
            List<SensorsGridItem> sensorsGridItemList = ((SmartphoneFragment)((MainActivity) Objects.requireNonNull(getActivity())).smartphoneFragment).getValidSensorGridItemList();
            for (SensorsGridItem item : sensorsGridItemList){
                captureConfig.addSmartwatchSensor(item.getSensorType(), new SensorConfig(item.getSensorType(), item.isEnabled(), item.getFrequency()));
            }
        }


        int countdownStart = this.getActivity().getPreferences(Context.MODE_PRIVATE).getInt(getString(R.string.preference_main_key_countdown_capture), getResources().getInteger(R.integer.preference_main_default_countdown_capture));
        boolean hasSound = this.getActivity().getPreferences(Context.MODE_PRIVATE).getBoolean(getString(R.string.preference_main_key_has_sound), getResources().getBoolean(R.bool.preference_main_default_has_sound));
        boolean hasVibration = this.getActivity().getPreferences(Context.MODE_PRIVATE).getBoolean(getString(R.string.preference_main_key_has_vibration), getResources().getBoolean(R.bool.preference_main_default_has_vibration));
        String appFolderName = this.getActivity().getPreferences(Context.MODE_PRIVATE).getString(getString(R.string.preference_main_key_app_folder_name), getResources().getString(R.string.preference_main_default_app_folder_name));
        captureConfig.setCountdownStart(countdownStart);
        captureConfig.setHasSound(hasSound);
        captureConfig.setHasVibration(hasVibration);
        captureConfig.setAppFolderName(appFolderName);

        return captureConfig;
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

    @SuppressLint("StaticFieldLeak")
    private class ReceiveSmartwatchSensorFilesTask extends AsyncTask<Asset, Integer, File> {

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            dialogWaitingSmartwatchFiles = new MaterialDialog.Builder(Objects.requireNonNull(getContext()))
                    .title(R.string.capture_dialog_wear_sensors_title)
                    .content(R.string.capture_dialog_wear_sensors_content)
                    .progress(false, 100, true)
                    .progressPercentFormat(NumberFormat.getPercentInstance())
                    .cancelable(false)
                    .show();
        }

        @Override
        protected File doInBackground(Asset... assets) {

            try {
                Asset asset = assets[0];
                Task<DataClient.GetFdForAssetResponse> getFdForAssetResponseTask = Wearable.getDataClient(Objects.requireNonNull(getContext())).getFdForAsset(asset);
                DataClient.GetFdForAssetResponse getFdForAssetResponse = Tasks.await(getFdForAssetResponseTask);

                InputStream assetInputStream = getFdForAssetResponse.getInputStream();
                int totalEstimatedBytes = assetInputStream.available();

                String captureAlias = String.format("smartwatch_%s", captureConfig.getAlias()).toLowerCase();
                File smartwatchFile = new File(String.format("%s/%s/%s.zip", Environment.getExternalStorageDirectory().getAbsolutePath(), captureConfig.getAppFolderName(), captureAlias));
                FileOutputStream fileOutputStream = new FileOutputStream(smartwatchFile);

                byte[] bytes = new byte[1024];
                int lengthReadBytes;
                int currentReadBytes = 0;
                while((lengthReadBytes = assetInputStream.read(bytes)) >= 0) {
                    fileOutputStream.write(bytes, 0, lengthReadBytes);
                    currentReadBytes += lengthReadBytes;
                    publishProgress((int)((float)currentReadBytes/(float)totalEstimatedBytes * 100));
                }
                publishProgress(100);
                fileOutputStream.close();
                assetInputStream.close();

                return smartwatchFile;
            } catch (Exception e){
                e.printStackTrace();
                return null;
            }

        }

        @Override
        protected void onProgressUpdate(Integer... values) {
            super.onProgressUpdate(values);
            dialogWaitingSmartwatchFiles.setProgress(values[0]);
        }

        @Override
        protected void onPostExecute(File file) {
            super.onPostExecute(file);
            dialogWaitingSmartwatchFiles.dismiss();
            new SaveCaptureDataTask().execute(file);
        }
    }

    @SuppressLint("StaticFieldLeak")
    private class SaveCaptureDataTask extends AsyncTask<File, Void, Void>{

        private MaterialDialog dialog;

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            dialog = new MaterialDialog.Builder(Objects.requireNonNull(getContext()))
                    .title(R.string.capture_dialog_make_capture_data_title)
                    .content(R.string.capture_dialog_make_capture_data_content)
                    .icon(Tools.changeDrawableColor(Objects.requireNonNull(getContext().getDrawable(R.drawable.ic_zip_box)), ContextCompat.getColor(getContext(), R.color.colorPrimary)))
                    .cancelable(false)
                    .progress(true, 0)
                    .show();
        }

        @Override
        protected Void doInBackground(File... files) {

            Map<String, File> filesToCompress = new HashMap<>();
            try {

                File smartphoneSensorsFile = captureRunner.finish();
                File smartwatchSensorsFile = (files.length > 0) ? files[0] : null;

                CaptureData captureData = new CaptureData();
                captureData.setCaptureConfig(captureConfig);
                if (smartphoneSensorsFile != null) {
                    filesToCompress.put("smartphone.zip", smartphoneSensorsFile);
                    captureData.setSmartphoneDeviceInfo(DeviceInfo.get(getContext()));
                }
                if (smartwatchSensorsFile != null) {
                    filesToCompress.put("smartwatch.zip", smartwatchSensorsFile);
                    captureData.setSmartwatchDeviceInfo(getWearService().getClientDeviceInfo());
                }


                File archiveFolder = new File(String.format("%s/%s/archive", Environment.getExternalStorageDirectory().getAbsolutePath(), captureConfig.getAppFolderName()));
                if(!archiveFolder.exists()){
                    if (!archiveFolder.mkdirs()) {
                        throw new FileNotFoundException(String.format("Failed to create the capture folder: %s", archiveFolder));
                    }
                }

                @SuppressLint("SimpleDateFormat") SimpleDateFormat simpleDateFormat = new SimpleDateFormat(getString(R.string.util_time_format_file));
                String startDate = simpleDateFormat.format(new Date(captureRunner.getStartTimestamp()));
                String endDate = simpleDateFormat.format(new Date(captureRunner.getEndTimestamp()));
                String captureName = String.format("capture_%s_%s_%s", captureConfig.getAlias().toLowerCase(), startDate, endDate);
                File captureDataFile = new File(String.format("%s/%s.json", archiveFolder, captureName));
                File captureCompressedFile = new File(String.format("%s/%s.zip", archiveFolder, captureName));

                captureData.toJson(captureDataFile.toString());
                filesToCompress.put("capture_info.json", captureDataFile);

                Compression.compressFiles(filesToCompress, captureCompressedFile);
                for(File fileToRemove : filesToCompress.values()) {
                    if (fileToRemove == captureDataFile)
                        continue;
                    fileToRemove.delete();
                }

            } catch (IOException e) {
                e.printStackTrace();
            }

            return null;
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            super.onPostExecute(aVoid);
            this.dialog.dismiss();
            captureRunner = null;
            captureConfig = null;
        }
    }

}