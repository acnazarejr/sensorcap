package com.ssig.smartcap.fragment;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.BitmapFactory;
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
import android.util.Log;
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
import com.ssig.sensorsmanager.info.PersonInfo;
import com.ssig.sensorsmanager.info.SensorInfo;
import com.ssig.sensorsmanager.util.Compression;
import com.ssig.smartcap.R;
import com.ssig.smartcap.activity.MainActivity;
import com.ssig.smartcap.adapter.AdapterSensorsList;
import com.ssig.smartcap.common.CountDownAnimation;
import com.ssig.smartcap.model.SensorsListItem;
import com.ssig.sensorsmanager.time.NTPTime;
import com.ssig.smartcap.service.WearService;
import com.ssig.smartcap.utils.Tools;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.channels.FileChannel;
import java.text.Normalizer;
import java.text.NumberFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;
import java.util.concurrent.ExecutionException;

import co.ceryle.radiorealbutton.RadioRealButtonGroup;

public class CaptureFragment extends AbstractMainFragment implements
        MessageClient.OnMessageReceivedListener,
        DataClient.OnDataChangedListener{

//    private enum CaptureState{
//        IDLE, CAPTURING
//    }

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

    private CaptureConfig mCaptureConfig;
    private CaptureRunner mCaptureRunner;


    private MaterialDialog mDialogWaitingSmartwatchFiles;

    public CaptureFragment(){
        super(R.layout.fragment_capture);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        this.mCaptureRunner = null;
        this.mCaptureConfig = null;
        this.initUi();
        this.registerListeners();
        this.checkRadioGroupLogic();
        final String uri = String.format("wear://*%s", getString(R.string.message_path_host_capture_fragment_prefix));
        Wearable.getMessageClient(this.getContext()).addListener(this, Uri.parse(uri), MessageClient.FILTER_PREFIX);
        Wearable.getDataClient(this.getContext()).addListener(this, Uri.parse(uri), DataClient.FILTER_PREFIX);
    }

    @Override
    public void onDestroy() {
        Wearable.getMessageClient(this.getContext()).removeListener(this);
        Wearable.getDataClient(this.getContext()).removeListener(this);
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
        if (wearService != null && (this.mCaptureRunner == null || this.mCaptureRunner.getStatus() == CaptureRunner.Status.IDLE))
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
    public void onDataChanged(@NonNull DataEventBuffer dataEventBuffer) {
        for (DataEvent event : dataEventBuffer) {
            if (event.getType() == DataEvent.TYPE_CHANGED) {
                String path = event.getDataItem().getUri().getPath();
                if (path.equals(getString(R.string.message_path_host_capture_fragment_sensor_files))) {
                    DataMapItem dataMapItem = DataMapItem.fromDataItem(event.getDataItem());
                    DataMap dataMap = dataMapItem.getDataMap();
                    Asset asset = dataMap.getAsset(getString(R.string.asset_sensors_smartwatch));
                    this.mDialogWaitingSmartwatchFiles.dismiss();
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

        this.mDialogWaitingSmartwatchFiles = null;
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

        this.mCaptureConfig = this.makeCaptureConfig();
        if (this.mCaptureConfig.isSmartphoneEnabled()) {
            try {
                String captureAlias = String.format("smartphone_%s", this.mCaptureConfig.getAlias());
                this.mCaptureRunner = new CaptureRunner(Objects.requireNonNull(this.getContext()), this.mCaptureConfig.getSmartphoneSensors(), this.mCaptureConfig.getAppFolderName(), captureAlias);
            } catch (IOException e) {
                Toast.makeText(this.getActivity(), e.getMessage(), Toast.LENGTH_LONG).show();
                return;
            }
        }

        this.resetDialogOnCapture();
        CountDownAnimation countDownAnimation = new CountDownAnimation(getContext(), this.mTextCountdown, this.mCaptureConfig.getCountdownStart(), this.mCaptureConfig.hasSound(), this.mCaptureConfig.hasVibration());
        countDownAnimation.setCountDownListener(new CountDownAnimation.CountDownListener() {
            @Override
            public void onCountDownEnd(CountDownAnimation animation) {
                mChronometerCapture.setBase(SystemClock.elapsedRealtime());
                mChronometerCapture.start();
                mLayoutDuringCapture.setVisibility(View.VISIBLE);
                if (mCaptureRunner != null)
                    mCaptureRunner.start();
            }
        });

        if (((MainActivity)getActivity()).getWearService().isConnected())
            ((MainActivity)getActivity()).getWearService().startCapture(this.mCaptureConfig);
        this.mDialogOnCapture.show();
        countDownAnimation.start();

    }

    public void stopCapture(){
        this.mChronometerCapture.stop();
        this.mDialogOnCapture.dismiss();
        if (this.mCaptureRunner != null){
            try {
                this.mCaptureRunner.stop();
            } catch (IOException e) {
                Toast.makeText(this.getActivity(), e.getMessage(), Toast.LENGTH_LONG).show();
                return;
            }
        }
        if (((MainActivity)getActivity()).getWearService().isConnected()) {
            this.mDialogWaitingSmartwatchFiles = new MaterialDialog.Builder(this.getContext())
                    .title(R.string.capture_dialog_wear_sensors_title)
                    .content(R.string.capture_dialog_wear_sensors_content)
                    .icon(Tools.changeDrawableColor(Objects.requireNonNull(this.getContext().getDrawable(R.drawable.ic_smartwatch)), ContextCompat.getColor(this.getContext(), R.color.colorPrimary)))
                    .cancelable(false)
                    .progress(true, 0)
                    .show();
            ((MainActivity) getActivity()).getWearService().stopCapture();
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

        String subjectName = this.mInputSubjectName.getText().toString();
        subjectName = !subjectName.equals("") ? subjectName : "Unknown";
        PersonInfo personInfo = new PersonInfo(subjectName);
        personInfo.setGender(this.mSwitchSubjectGender.getChecked() == IconSwitch.Checked.LEFT ? PersonInfo.Gender.MALE : PersonInfo.Gender.FEMALE);
        personInfo.setAge(this.mNumberPickerAge.getValue());
        personInfo.setHeight(this.mNumberPickerHeight.getValue());
        personInfo.setWeight(this.mNumberPickerWeight.getValue());

        String alias = Normalizer.normalize(personInfo.getName().toLowerCase().replace(" ", "-"), Normalizer.Form.NFD);
        alias = alias.replaceAll("[^\\p{ASCII}]", "");
        CaptureConfig captureConfig = new CaptureConfig(UUID.randomUUID(), alias);


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
        String appFolderName = this.getActivity().getPreferences(Context.MODE_PRIVATE).getString(getString(R.string.preference_main_key_app_folder_name), getResources().getString(R.string.preference_main_default_app_folder_name));
        captureConfig.setCountdownStart(countdownStart);
        captureConfig.setHasSound(hasSound);
        captureConfig.setHasVibration(hasVibration);
        captureConfig.setAppFolderName(appFolderName);

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

    private class ReceiveSmartwatchSensorFilesTask extends AsyncTask<Asset, Integer, File> {

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            mDialogWaitingSmartwatchFiles = new MaterialDialog.Builder(getContext())
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
                Task<DataClient.GetFdForAssetResponse> getFdForAssetResponseTask = Wearable.getDataClient(getContext()).getFdForAsset(asset);
                DataClient.GetFdForAssetResponse getFdForAssetResponse = Tasks.await(getFdForAssetResponseTask);

                InputStream assetInputStream = getFdForAssetResponse.getInputStream();
                int totalEstimatedBytes = assetInputStream.available();

                String captureAlias = String.format("smartwatch_%s", mCaptureConfig.getAlias()).toLowerCase();
                File smartwatchFile = new File(String.format("%s/%s/%s.zip", Environment.getExternalStorageDirectory().getAbsolutePath(), mCaptureConfig.getAppFolderName(), captureAlias));
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
            mDialogWaitingSmartwatchFiles.setProgress(values[0]);
        }

        @Override
        protected void onPostExecute(File file) {
            super.onPostExecute(file);
            mDialogWaitingSmartwatchFiles.dismiss();
            new SaveCaptureDataTask().execute(file);
        }
    }

    private class SaveCaptureDataTask extends AsyncTask<File, Void, Void>{

        private MaterialDialog dialog;

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            dialog = new MaterialDialog.Builder(getContext())
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

                File smartphoneSensorsFile = mCaptureRunner.finish();
                File smartwatchSensorsFile = (files.length > 0) ? files[0] : null;

                CaptureData captureData = new CaptureData();
                captureData.setCaptureConfig(mCaptureConfig);
                if (smartphoneSensorsFile != null) {
                    filesToCompress.put("smartphone.zip", smartphoneSensorsFile);
                    captureData.setSmartphoneSensorsInfo(SensorInfo.getAll(getContext()));
                }
                if (smartwatchSensorsFile != null) {
                    filesToCompress.put("smartwatch.zip", smartwatchSensorsFile);
                    captureData.setSmartwatchSensorsInfo(((MainActivity)getActivity()).getWearService().getClientSensorInfo());
                }


                File archiveFolder = new File(String.format("%s/%s/archive", Environment.getExternalStorageDirectory().getAbsolutePath(), mCaptureConfig.getAppFolderName()));
                if(!archiveFolder.exists()){
                    if (!archiveFolder.mkdirs()) {
                        throw new FileNotFoundException(String.format("Failed to create the capture folder: %s", archiveFolder));
                    }
                }

                SimpleDateFormat simpleDateFormat = new SimpleDateFormat(getString(R.string.util_time_format_file));
                String startDate = simpleDateFormat.format(new Date(mCaptureRunner.getStartTimestamp()));
                String endDate = simpleDateFormat.format(new Date(mCaptureRunner.getEndTimestamp()));
                String captureName = String.format("capture_%s_%s_%s", mCaptureConfig.getAlias().toLowerCase(), startDate, endDate);
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
            mCaptureRunner = null;
            mCaptureConfig = null;
        }
    }

}