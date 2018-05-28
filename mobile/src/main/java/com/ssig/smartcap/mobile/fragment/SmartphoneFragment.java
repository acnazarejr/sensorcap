package com.ssig.smartcap.mobile.fragment;

import android.graphics.PorterDuff;
import android.graphics.PorterDuffColorFilter;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.support.v4.content.ContextCompat;
import android.view.View;
import android.widget.TextView;

import com.ssig.smartcap.mobile.R;

public class SmartphoneFragment extends AbstractMainFragment {

    public SmartphoneFragment(){
        super("Smartphone Settings", R.layout.fragment_smartphone);
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        this.setTextViewDrawableColor((TextView) getActivity().findViewById(R.id.text_sensor_title));
    }

    private void setTextViewDrawableColor(TextView textView) {
        for (Drawable drawable : textView.getCompoundDrawables()) {
            if (drawable != null) {
                drawable.setColorFilter(new PorterDuffColorFilter(ContextCompat.getColor(this.getContext(), R.color.teal_800), PorterDuff.Mode.SRC_IN));
            }
        }
    }

}
