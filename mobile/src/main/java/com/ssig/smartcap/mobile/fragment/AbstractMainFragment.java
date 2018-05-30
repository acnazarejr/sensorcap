package com.ssig.smartcap.mobile.fragment;

import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.support.annotation.ColorRes;
import android.support.annotation.DrawableRes;
import android.support.annotation.LayoutRes;
import android.support.annotation.NonNull;
import android.support.design.widget.TabLayout;
import android.support.v4.app.Fragment;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;

import com.ssig.smartcap.mobile.R;
import com.ssig.smartcap.mobile.utils.ViewAnimation;

public abstract class AbstractMainFragment extends Fragment {

    public String title;
    public int icon;
    public int color;
    View view;

    private int resource;


    public AbstractMainFragment(String title, int icon, int color, @LayoutRes int resource) {
        this.title = title;
        this.resource = resource;
        this.icon = icon;
        this.color = color;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        this.view = inflater.inflate(this.resource, container, false);
        return this.view;
    }

    public void reload(@NonNull View progress, @NonNull View content, View error){

        progress.setVisibility(View.GONE);
        content.setVisibility(View.GONE);
        content.setVisibility(View.GONE);

        progress.setVisibility(View.VISIBLE);
        progress.setAlpha(1.0f);
        boolean loaded = this.makeContent();
        if(!loaded && error != null) {
            this.swapViews(progress, error);
        }else{
            this.swapViews(progress, content);
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

    public abstract boolean makeContent();


}
