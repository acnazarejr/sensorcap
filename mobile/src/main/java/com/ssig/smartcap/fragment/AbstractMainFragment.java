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

    //
//    @Override
//    public void onStart() {
//        super.onStart();
//        this.reload();
//    }
//
//    public void reload(){
//
//        if (contentView != null && progressView == null) {
//            contentView.setVisibility(View.VISIBLE);
//            return;
//        }
//
//        if (contentView != null && progressView != null){
//            progressView.setVisibility(View.GONE);
//            contentView.setVisibility(View.GONE);
//            if (errorView != null)
//                errorView.setVisibility(View.GONE);
//
//            progressView.setVisibility(View.VISIBLE);
//            boolean loaded = this.makeContent();
//            if(!loaded && errorView != null) {
//                this.swapViews(progressView, errorView);
//            }else{
//                this.swapViews(progressView, contentView);
//            }
//            return;
//        }
//
//    }
//
//
//    private void swapViews(@NonNull final View outView, @NonNull final View inView){
//        new Thread(new Runnable() {
//            @Override
//            public void run() {
//                ViewAnimation.fadeOut(outView);
//            }
//        }).run();
//        new Thread(new Runnable() {
//            @Override
//            public void run() {
//                ViewAnimation.fadeIn(inView);
//            }
//        }).run();
//    }
//
//    public abstract void setViews();
//    public abstract boolean makeContent();


    public void willBeDisplayed() {
        if (this.fragmentContainer != null) {
            Animation fadeIn = AnimationUtils.loadAnimation(getActivity(), R.anim.fade_in);
            this.fragmentContainer.startAnimation(fadeIn);
        }
    }

    public void willBeHidden() {
        if (this.fragmentContainer != null) {
            Animation fadeOut = AnimationUtils.loadAnimation(getActivity(), R.anim.fade_out);
            this.fragmentContainer.startAnimation(fadeOut);
        }
    }

    public abstract void refresh();

}
