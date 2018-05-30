package com.ssig.smartcap.mobile.fragment;

import com.ssig.smartcap.mobile.R;

public class CaptureFragment extends AbstractMainFragment {

    public CaptureFragment(){
        super("Capture Mode", R.drawable.ic_running, R.color.capture, R.layout.fragment_capture);
    }

    @Override
    public boolean makeContent() {
        return true;
    }

}
