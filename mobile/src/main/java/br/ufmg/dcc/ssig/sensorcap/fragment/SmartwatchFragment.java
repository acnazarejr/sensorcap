package br.ufmg.dcc.ssig.sensorcap.fragment;

import android.view.View;
import android.widget.Button;
import br.ufmg.dcc.ssig.sensorsmanager.info.DeviceInfo;
import br.ufmg.dcc.ssig.sensorcap.R;
import br.ufmg.dcc.ssig.sensorcap.activity.MainActivity;

import java.util.Objects;

public class SmartwatchFragment extends AbstractDeviceFragment {

    private View mLayoutSmartwatchError;
    private View mLayoutSmartwatchContent;

    public SmartwatchFragment(){
        super(R.layout.fragment_smartwatch, R.string.preference_smartwatch_file_id);
    }

    @Override
    protected void initUI(){

        this.mLayoutSmartwatchError = Objects.requireNonNull(getActivity()).findViewById(R.id.layout_smartwatch_fragment_error);
        this.mLayoutSmartwatchContent = getActivity().findViewById(R.id.layout_smartwatch_fragment_content);
        this.initDeviceSensors(Objects.requireNonNull(this.getView()).findViewById(R.id.available_sensors_layout));

        Button layoutSmartwatchErrorButton = this.mLayoutSmartwatchError.findViewById(R.id.layout_smartwatch_error_button);
        layoutSmartwatchErrorButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ((MainActivity) Objects.requireNonNull(getActivity())).doWearConnection();
            }
        });
        this.refresh();
    }

    @Override
    public void refresh() {
        this.mLayoutSmartwatchError.setVisibility(View.GONE);
        this.mLayoutSmartwatchContent.setVisibility(View.GONE);

        if (!this.isWearClientConnected()) {
            this.clearAdapterSensorsGridMap();
            this.mLayoutSmartwatchError.setVisibility(View.VISIBLE);
        } else {
            DeviceInfo deviceInfo = this.getWearService().getClientDeviceInfo();
            this.initDeviceInfo(Objects.requireNonNull(this.getView()).findViewById(R.id.device_info_layout), deviceInfo);
            this.configureAvailableSensors(deviceInfo);
            this.mLayoutSmartwatchContent.setVisibility(View.VISIBLE);
        }
    }

}