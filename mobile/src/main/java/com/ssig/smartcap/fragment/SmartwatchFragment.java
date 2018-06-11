package com.ssig.smartcap.fragment;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;

import com.ssig.sensorsmanager.SensorType;
import com.ssig.sensorsmanager.info.SensorInfo;
import com.ssig.smartcap.R;
import com.ssig.smartcap.activity.MainActivity;
import com.ssig.smartcap.adapter.AdapterSensorsList;
import com.ssig.smartcap.service.WearService;
import com.ssig.smartcap.utils.Tools;
import com.ssig.smartcap.widget.LineItemDecoration;

import java.util.Map;
import java.util.Objects;

public class SmartwatchFragment extends AbstractMainFragment {

    private RecyclerView mRecyclerView;
    private View mLayoutSmartwatchError;
    private View mLayoutSmartwatchContent;

    private AdapterSensorsList mAdapterSensorsList;

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
        this.checkWearConnection();
    }


    @Override
    public void onHide() {
        super.onHide();
        WearService wearService = ((MainActivity) Objects.requireNonNull(getActivity())).getWearService();
        if(wearService.isConnected()) {
            String preferencesName = String.format("%s%s", getString(R.string.preference_smartwatch_file_id), wearService.getClientID());
            Tools.saveSensorsPreferences(getContext(), this.mAdapterSensorsList, preferencesName);
        }
    }

    public AdapterSensorsList getAdapterSensorsList() {
        return mAdapterSensorsList;
    }

    private void initUI(){
        this.mLayoutSmartwatchError = Objects.requireNonNull(getActivity()).findViewById(R.id.layout_smartwatch_fragment_error);
        this.mLayoutSmartwatchContent = getActivity().findViewById(R.id.layout_smartwatch_fragment_content);

        this.mRecyclerView = Objects.requireNonNull(this.getView()).findViewById(R.id.sensors_recycler_view);
        this.mRecyclerView.setLayoutManager(new LinearLayoutManager(this.getContext()));
        this.mRecyclerView.addItemDecoration(new LineItemDecoration(Objects.requireNonNull(this.getContext()), LinearLayout.VERTICAL));
        this.mRecyclerView.setHasFixedSize(true);

        Button layoutSmartwatchErrorButton = this.mLayoutSmartwatchError.findViewById(R.id.layout_smartwatch_error_button);
        layoutSmartwatchErrorButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ((MainActivity) Objects.requireNonNull(getActivity())).doWearConnection();
            }
        });

        this.mLayoutSmartwatchContent.findViewById(R.id.button_reset_defaults).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Tools.resetSensorsPreferences(getActivity(), mAdapterSensorsList);
            }
        });
        this.checkWearConnection();
    }

    private void checkWearConnection() {

        WearService wearService = ((MainActivity) Objects.requireNonNull(getActivity())).getWearService();
        boolean wearConnected = wearService != null && wearService.isConnected();

        this.mLayoutSmartwatchError.setVisibility(View.GONE);
        this.mLayoutSmartwatchContent.setVisibility(View.GONE);

        if (!wearConnected) {
            if (this.mAdapterSensorsList != null)
                this.mAdapterSensorsList.clear();
                this.mAdapterSensorsList = null;
            this.mLayoutSmartwatchError.setVisibility(View.VISIBLE);
        } else {
            this.configureSensorList(((MainActivity) Objects.requireNonNull(getActivity())).getWearService().getClientSensorInfo());
            this.mLayoutSmartwatchContent.setVisibility(View.VISIBLE);
        }
    }

    private void configureSensorList(Map<SensorType, SensorInfo> smartwatchSensors){
        String preferencesName = getString(R.string.preference_smartwatch_file_id) + ((MainActivity) Objects.requireNonNull(getActivity())).getWearService().getClientID();
        this.mAdapterSensorsList = Tools.populateSensorsList(getContext(), this.mRecyclerView, preferencesName, smartwatchSensors);
    }

//    @SuppressLint("StaticFieldLeak")
//    private class WearRequestSensorInfoTask extends AsyncTask< Void, Void, Map<SensorType, SensorInfo>> {
//
//        private MaterialDialog dialog;
//        private final WeakReference<MainActivity> mainActivity;
//
//        WearRequestSensorInfoTask(MainActivity mainActivity){
//            this.mainActivity = new WeakReference<>(mainActivity);
//        }
//
//        @Override
//        protected void onPreExecute() {
//            super.onPreExecute();
//            this.dialog = new MaterialDialog.Builder(this.mainActivity.get())
//                    .title(R.string.dialog_wear_sensorinfo_title)
//                    .content(R.string.dialog_wear_sensorinfo_content)
//                    .icon(Tools.changeDrawableColor(Objects.requireNonNull(this.mainActivity.get().getDrawable(R.drawable.ic_smartphone)), ContextCompat.getColor(this.mainActivity.get(), R.color.colorPrimary)))
//                    .cancelable(false)
//                    .progress(true, 0)
//                    .show();
//        }
//
//        @Override
//        protected Map<SensorType, SensorInfo> doInBackground(Void... voids) {
//            return WearUtil.requestClientSensorInfo(this.mainActivity.get());
//        }
//
//        @Override
//        protected void onPostExecute(Map<SensorType, SensorInfo> sensorTypeSensorInfoMap) {
//            super.onPostExecute(sensorTypeSensorInfoMap);
//            configureSensorList(sensorTypeSensorInfoMap);
//            Toast.makeText(this.mainActivity.get(), getString(R.string.toast_wear_sensorinfo_success), Toast.LENGTH_LONG).show();
//            this.dialog.dismiss();
//        }
//
//    }

}