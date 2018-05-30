package com.ssig.smartcap.mobile.fragment;

import com.ssig.smartcap.mobile.R;


public class TimeToolFragment extends AbstractMainFragment {

    public TimeToolFragment(){
        super("TimeTool", R.drawable.ic_qrcode_scan, R.color.teal_500, R.layout.fragment_time_tool);
    }

    @Override
    public boolean makeContent() {
        return true;
    }

}
