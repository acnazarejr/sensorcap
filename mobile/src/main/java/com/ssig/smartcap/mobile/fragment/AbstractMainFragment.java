package com.ssig.smartcap.mobile.fragment;


import android.os.Bundle;
import android.support.annotation.LayoutRes;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.ssig.smartcap.mobile.utils.ViewAnimation;

public abstract class AbstractMainFragment extends Fragment {

    View view;
    protected View progressView;
    protected View contentView;
    protected View errorView;


    private int layout;


    public AbstractMainFragment(@LayoutRes int layout) {
        this.layout = layout;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        this.view = inflater.inflate(this.layout, container, false);

        this.progressView = null;
        this.contentView = null;
        this.errorView = null;

        return this.view;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        this.setViews();
    }

    @Override
    public void onStart() {
        super.onStart();
        this.reload();
    }

    public void reload(){

        if (contentView != null && progressView == null) {
            contentView.setVisibility(View.VISIBLE);
            return;
        }

        if (contentView != null && progressView != null){
            progressView.setVisibility(View.GONE);
            contentView.setVisibility(View.GONE);
            if (errorView != null)
                errorView.setVisibility(View.GONE);

            progressView.setVisibility(View.VISIBLE);
            boolean loaded = this.makeContent();
            if(!loaded && errorView != null) {
                this.swapViews(progressView, errorView);
            }else{
                this.swapViews(progressView, contentView);
            }
            return;
        }

    }


    private void swapViews(@NonNull final View outView, @NonNull final View inView){
        new Thread(new Runnable() {
            @Override
            public void run() {
                ViewAnimation.fadeOut(outView);
            }
        }).run();
        new Thread(new Runnable() {
            @Override
            public void run() {
                ViewAnimation.fadeIn(inView);
            }
        }).run();
    }

    public abstract void setViews();
    public abstract boolean makeContent();
    public abstract String getTitle();
    public abstract int getIcon();
    public abstract int getPrimaryColor();
    public abstract int getSecondaryColor();


}
