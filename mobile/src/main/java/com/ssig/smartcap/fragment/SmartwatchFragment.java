package com.ssig.smartcap.fragment;

import android.annotation.SuppressLint;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import com.afollestad.materialdialogs.MaterialDialog;
import com.ssig.sensorsmanager.info.SensorInfo;
import com.ssig.sensorsmanager.SensorType;
import com.ssig.smartcap.R;
import com.ssig.smartcap.activity.MainActivity;
import com.ssig.smartcap.adapter.AdapterListSensor;
import com.ssig.smartcap.utils.Tools;
import com.ssig.smartcap.utils.WearUtil;

import java.lang.ref.WeakReference;
import java.util.Map;
import java.util.Objects;

public class SmartwatchFragment extends AbstractMainFragment {

    private AdapterListSensor adapterListSensor;
    private View layoutSmartwatchError;
    private View layoutSmartwatchContent;



    public SmartwatchFragment(){
        super(R.layout.fragment_smartwatch);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        this.initUI();
    }

    @Override
    public void refresh() {
        this.checkWearNodes();
    }

    @Override
    public void onShow() {

    }

    private void initUI(){
        this.layoutSmartwatchError = Objects.requireNonNull(getActivity()).findViewById(R.id.layout_smartwatch_fragment_error);
        this.layoutSmartwatchContent = getActivity().findViewById(R.id.layout_smartwatch_fragment_content);
        Button layoutSmartwatchErrorButton = this.layoutSmartwatchError.findViewById(R.id.layout_smartwatch_error_button);

        layoutSmartwatchErrorButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ((MainActivity) Objects.requireNonNull(getActivity())).startWearSynchronization();
            }
        });

        this.layoutSmartwatchContent.findViewById(R.id.button_reset_defaults).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Tools.resetSensorsPreferences(getActivity(), adapterListSensor);
            }
        });
        this.checkWearNodes();
    }

    @Override
    public void onHide() {
        super.onHide();
        if(WearUtil.isConnected()) {
            String preferencesName = getString(R.string.preference_smartwatch_file_id) + WearUtil.getClientID();
            Tools.saveSensorsPreferences(getContext(), this.adapterListSensor, preferencesName);
        }
    }

    private void checkWearNodes() {
        this.layoutSmartwatchError.setVisibility(View.GONE);
        this.layoutSmartwatchContent.setVisibility(View.GONE);

        if (!WearUtil.isConnected()) {
            layoutSmartwatchError.setVisibility(View.VISIBLE);
        } else {
            new WearRequestSensorInfoTask((MainActivity) this.getActivity()).execute();
            this.layoutSmartwatchContent.setVisibility(View.VISIBLE);
        }
    }

    private void configureSensorList(Map<SensorType, SensorInfo> smartwatchSensors){
        RecyclerView recyclerView = Objects.requireNonNull(this.getView()).findViewById(R.id.sensors_recycler_view);
        String preferencesName = getString(R.string.preference_smartwatch_file_id) + WearUtil.getClientID();
        this.adapterListSensor = Tools.populateSensorsList(getContext(), recyclerView, preferencesName, smartwatchSensors);
    }

    @SuppressLint("StaticFieldLeak")
    private class WearRequestSensorInfoTask extends AsyncTask< Void, Void, Map<SensorType, SensorInfo>> {

        private MaterialDialog dialog;
        private final WeakReference<MainActivity> mainActivity;

        WearRequestSensorInfoTask(MainActivity mainActivity){
            this.mainActivity = new WeakReference<>(mainActivity);
        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            this.dialog = new MaterialDialog.Builder(this.mainActivity.get())
                    .title(R.string.dialog_wear_sensorinfo_title)
                    .content(R.string.dialog_wear_sensorinfo_content)
                    .icon(Tools.changeDrawableColor(Objects.requireNonNull(this.mainActivity.get().getDrawable(R.drawable.ic_smartphone)), ContextCompat.getColor(this.mainActivity.get(), R.color.colorPrimary)))
                    .cancelable(false)
                    .progress(true, 0)
                    .show();
        }

        @Override
        protected Map<SensorType, SensorInfo> doInBackground(Void... voids) {
            return WearUtil.requestClientSensorInfo(this.mainActivity.get());
        }

        @Override
        protected void onPostExecute(Map<SensorType, SensorInfo> sensorTypeSensorInfoMap) {
            super.onPostExecute(sensorTypeSensorInfoMap);
            configureSensorList(sensorTypeSensorInfoMap);
            Toast.makeText(this.mainActivity.get(), getString(R.string.toast_wear_sensorinfo_success), Toast.LENGTH_LONG).show();
            this.dialog.dismiss();
        }

    }

    public AdapterListSensor getAdapterListSensor() {
        return adapterListSensor;
    }

}