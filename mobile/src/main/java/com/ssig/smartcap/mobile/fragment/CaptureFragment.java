package com.ssig.smartcap.mobile.fragment;

import android.os.Bundle;
import android.support.design.widget.TextInputEditText;
import android.support.design.widget.TextInputLayout;
import android.support.v7.widget.AppCompatEditText;
import android.support.v7.widget.SwitchCompat;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;

import com.ssig.smartcap.mobile.R;

import org.w3c.dom.Text;

public class CaptureFragment extends AbstractMainFragment {

    private TextInputLayout input_name;
    private EditText input_weight;
    private TextInputLayout input_age;
    private TextInputLayout input_height;
    private SwitchCompat switch_wear;
    private SwitchCompat switch_gender;

    private ImageView img_gender;
    private String gender = "male";

    public CaptureFragment(){
        super("Capture Mode", R.drawable.ic_running, R.color.capture, R.layout.fragment_capture);
    }

    @Override
    public boolean makeContent() {
        return true;
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        findViewsById();
        implementListeners();
    }

    private void findViewsById() {
        input_name = getActivity().findViewById(R.id.input_name);
        input_weight = getActivity().findViewById(R.id.input_weight);
        input_age = getActivity().findViewById(R.id.input_age);
        input_height = getActivity().findViewById(R.id.input_height);

        switch_gender = getActivity().findViewById(R.id.switch_gender);
        img_gender = getActivity().findViewById(R.id.img_gender);

        switch_wear = getActivity().findViewById(R.id.switch_wear);
    }

    private void implementListeners() {
        switch_gender.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(gender.equals("Male")){
                    img_gender.setImageResource(R.drawable.ic_human_male);
                }else{
                    img_gender.setImageResource(R.drawable.ic_human_female);
                }
                gender = (gender.equals("Male")) ? "Female" : "Male";
            }
        });
        switch_wear.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(switch_wear.getText().equals("Enabled")){
                    switch_wear.setText("Disabled");
                }else{
                    switch_wear.setText("Enabled");
                }
            }
        });
        input_weight.setEnabled(true);
    }
}
