package com.ssig.smartcap.fragment;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.ssig.smartcap.R;
import com.ssig.smartcap.activity.MainActivity;
import com.ssig.smartcap.utils.DeviceTools;

import java.util.Objects;

public class SmartwatchFragment extends AbstractMainFragment {

    private MainActivity mainActivity;
    private View layoutSmartwatchError;

    public SmartwatchFragment(){
        super(R.layout.fragment_smartwatch);
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        this.initUI();
    }

    @Override
    public void onStart() {
        super.onStart();
    }

    @Override
    public void onResume() {
        super.onResume();
    }

    @Override
    public void refresh() {
        if (this.checkWearAppInstalled() && checkHasWearClientNodes() && checkHasWearSmartcapClientNodes()) {
            this.layoutSmartwatchError.setVisibility(View.GONE);
        }
    }

    private void initUI(){
        this.mainActivity = (MainActivity) getActivity();
        this.layoutSmartwatchError = this.mainActivity.findViewById(R.id.layout_smartwatch_fragment_error);
    }

    private boolean checkWearAppInstalled(){
        if (DeviceTools.hasApp(Objects.requireNonNull(this.getContext()), getString(R.string.util_wear_package))){
            return true;
        }

        this.layoutSmartwatchError.setVisibility(View.VISIBLE);
        ((TextView)this.layoutSmartwatchError.findViewById(R.id.layout_smartwatch_error_title)).setText(R.string.smartwatch_wearos_error_title);
        ((TextView)this.layoutSmartwatchError.findViewById(R.id.layout_smartwatch_error_content)).setText(R.string.smartwatch_wearos_error_content);
        Button layoutSmartwatchErrorButton = this.layoutSmartwatchError.findViewById(R.id.layout_smartwatch_error_button);
        layoutSmartwatchErrorButton.setText(R.string.smartwatch_wearos_error_button);
        layoutSmartwatchErrorButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent goToMarket = new Intent(Intent.ACTION_VIEW).setData(Uri.parse(String.format("market://details?id=%s", getString(R.string.util_wear_package))));
                getActivity().startActivity(goToMarket);
            }
        });
        return false;
    }

    private boolean checkHasWearClientNodes(){

        if (this.mainActivity.hasWearClientNodes()){
            return true;
        }
        layoutSmartwatchError.setVisibility(View.VISIBLE);
        ((TextView)this.layoutSmartwatchError.findViewById(R.id.layout_smartwatch_error_title)).setText(R.string.smartwatch_devices_error_title);
        ((TextView)this.layoutSmartwatchError.findViewById(R.id.layout_smartwatch_error_content)).setText(R.string.smartwatch_devices_error_content);
        Button layoutSmartwatchErrorButton = this.layoutSmartwatchError.findViewById(R.id.layout_smartwatch_error_button);
        layoutSmartwatchErrorButton.setText(R.string.button_try_again);
        layoutSmartwatchErrorButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ((MainActivity) Objects.requireNonNull(getActivity())).startWearSynchronization();
            }
        });
        return false;
    }


    private boolean checkHasWearSmartcapClientNodes(){

        if (this.mainActivity.hasWearSmartClientNodes()){
            return true;
        }
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
        return false;
    }

}