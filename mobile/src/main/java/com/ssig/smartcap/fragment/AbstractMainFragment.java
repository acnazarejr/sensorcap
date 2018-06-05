package com.ssig.smartcap.fragment;


import android.annotation.SuppressLint;
import android.os.Bundle;
import android.support.annotation.LayoutRes;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.CoordinatorLayout;
import android.support.v4.app.Fragment;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.view.menu.MenuBuilder;
import android.support.v7.widget.Toolbar;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;

import com.ssig.smartcap.R;
import com.ssig.smartcap.utils.Tools;


public abstract class AbstractMainFragment extends Fragment {

    private int layout;

    private View fragmentContainer;
    private View view;

    public AbstractMainFragment(@LayoutRes int layout) {
        this.layout = layout;
        this.fragmentContainer = null;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        this.view = inflater.inflate(this.layout, container, false);
        this.fragmentContainer = this.view.findViewById(R.id.fragment_container);
        return this.view;
    }


    @Override
    public void onStart() {
        super.onStart();
        this.show();
    }

    @Override
    public void onStop() {
        super.onStop();
        this.hide();
    }

    public void show(){
        if (this.fragmentContainer != null) {
            Animation fadeIn = AnimationUtils.loadAnimation(getActivity(), R.anim.fade_in);
            this.fragmentContainer.startAnimation(fadeIn);
        }
        this.onShow();
    }


    public void hide() {
        if (this.fragmentContainer != null) {
            Animation fadeOut = AnimationUtils.loadAnimation(getActivity(), R.anim.fade_out);
            this.fragmentContainer.startAnimation(fadeOut);
        }
        this.onHide();
    }

    public void refresh(){}
    public void onShow(){}
    public void onHide(){}

}
