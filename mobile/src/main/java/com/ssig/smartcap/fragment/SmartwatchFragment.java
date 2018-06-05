package com.ssig.smartcap.fragment;

import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.content.ContextCompat;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.afollestad.materialdialogs.MaterialDialog;
import com.ssig.sensorsmanager.SensorInfo;
import com.ssig.sensorsmanager.SensorType;
import com.ssig.smartcap.R;
import com.ssig.smartcap.activity.MainActivity;
import com.ssig.smartcap.utils.Tools;
import com.ssig.smartcap.utils.WearUtil;

import java.lang.ref.WeakReference;
import java.util.Map;
import java.util.Objects;

public class SmartwatchFragment extends AbstractMainFragment {

    private MainActivity mainActivity;
    private View layoutSmartwatchError;

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
        this.checkWearNodes();
    }

    private void initUI(){
        this.mainActivity = (MainActivity) getActivity();
        this.layoutSmartwatchError = this.mainActivity.findViewById(R.id.layout_smartwatch_fragment_error);
    }

    private void checkWearNodes() {
        this.layoutSmartwatchError.setVisibility(View.GONE);
        if (!WearUtil.get().hasWearClientNodes()) {
            layoutSmartwatchError.setVisibility(View.VISIBLE);
            ((TextView)this.layoutSmartwatchError.findViewById(R.id.layout_smartwatch_error_title)).setText(R.string.smartwatch_smartcap_error_title);
            ((TextView)this.layoutSmartwatchError.findViewById(R.id.layout_smartwatch_error_content)).setText(R.string.smartwatch_smartcap_error_content);
            Button layoutSmartwatchErrorButton = this.layoutSmartwatchError.findViewById(R.id.layout_smartwatch_error_button);
            layoutSmartwatchErrorButton.setText(R.string.button_try_again);
            layoutSmartwatchErrorButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    ((MainActivity) Objects.requireNonNull(getActivity())).startWearSynchronization();
                }
            });
        } else {
            new WearRequestSensorInfoTask((MainActivity) this.getActivity()).execute();
        }
    }

    private class WearRequestSensorInfoTask extends AsyncTask< Void, Void, Map<SensorType, SensorInfo>> {

        private MaterialDialog dialog;
        private final WeakReference<MainActivity> mainActivity;
        Map<SensorType, SensorInfo> smartwatchSensorInfo;

        public WearRequestSensorInfoTask(MainActivity mainActivity){
            this.mainActivity = new WeakReference<>(mainActivity);
            this.smartwatchSensorInfo = null;
        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            this.dialog = new MaterialDialog.Builder(this.mainActivity.get())
                    .title(R.string.dialog_wear_sensorinfo_title)
                    .content(R.string.dialog_wear_sensorinfo_content)
                    .icon(Tools.changeDrawableColor(this.mainActivity.get().getDrawable(R.drawable.ic_smartphone), ContextCompat.getColor(this.mainActivity.get(), R.color.colorPrimary)))
                    .cancelable(false)
                    .progress(true, 0)
                    .show();
        }

        @Override
        protected Map<SensorType, SensorInfo> doInBackground(Void... voids) {
            WearUtil.get().requestClientSensorInfo();
            Thread thread = new Thread(new Runnable() {
                @Override
                public void run() {
                    while(true){
                        smartwatchSensorInfo = WearUtil.get().getClientSensorInfo();
                        if (smartwatchSensorInfo == null) {
                            try {
                                Thread.sleep(100);
                            } catch (InterruptedException e) {}
                        }
                        else {
                            break;
                        }
                    }
                }
            });
            thread.start();
            return smartwatchSensorInfo;
        }

        @Override
        protected void onPostExecute(Map<SensorType, SensorInfo> sensorTypeSensorInfoMap) {
            super.onPostExecute(sensorTypeSensorInfoMap);
            this.dialog.dismiss();
            Toast.makeText(this.mainActivity.get(), getString(R.string.toast_wear_sensorinfo_success), Toast.LENGTH_LONG).show();
        }

        //        @Override
//        protected void onPostExecute(WearUtil.SynchronizationResponse synchronizationResponse) {
//            super.onPostExecute(synchronizationResponse);
//            switch (synchronizationResponse){
//                case UNKNOWN_ERROR:
//                    Toast.makeText(this.mainActivity.get(), getString(R.string.toast_wear_synchronization_unknown_error), Toast.LENGTH_LONG).show();
//                    break;
//                case NO_WEAR_APP:
//                    Toast.makeText(this.mainActivity.get(), getString(R.string.toast_wear_synchronization_unknown_error), Toast.LENGTH_LONG).show();
//                    break;
//                case BLUETOOTH_DISABLED:
//                    Toast.makeText(this.mainActivity.get(), getString(R.string.toast_wear_synchronization_bluetooth_error), Toast.LENGTH_LONG).show();
//                    break;
//                case NO_PAIRED_DEVICES:
//                    Toast.makeText(this.mainActivity.get(), getString(R.string.toast_wear_synchronization_no_paired_error), Toast.LENGTH_LONG).show();
//                    break;
//                case NO_CAPABLE_DEVICES:
//                    Toast.makeText(this.mainActivity.get(), getString(R.string.toast_wear_synchronization_no_capable_error), Toast.LENGTH_LONG).show();
//                    break;
//                case SUCCESS:
//                    Toast.makeText(this.mainActivity.get(), getString(R.string.toast_wear_synchronization_success), Toast.LENGTH_LONG).show();
//                    WearUtil.get().openClientActivity();
//                    break;
//            }
//            this.dialog.dismiss();
//            this.mainActivity.get().updateWearMenuItem();
//            this.mainActivity.get().refreshCurrentFragment();
//        }

    }

}