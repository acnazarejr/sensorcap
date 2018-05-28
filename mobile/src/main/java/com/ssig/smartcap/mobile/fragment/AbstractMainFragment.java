package com.ssig.smartcap.mobile.fragment;

import android.os.Bundle;
import android.support.annotation.LayoutRes;
import android.support.design.widget.TabLayout;
import android.support.v4.app.Fragment;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.ssig.smartcap.mobile.R;

public abstract class AbstractMainFragment extends Fragment {

    private String title;
    private TabLayout tab_layout;
    private ActionBar actionBar;
    private int resource;


    public AbstractMainFragment(String title, @LayoutRes int resource) {
        this.title = title;
        this.resource = resource;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(this.resource, container, false);
    }


    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        this.actionBar = ((AppCompatActivity)getActivity()).getSupportActionBar();
    }

    @Override
    public void onResume() {
        super.onResume();
//        this.resetTabLayout();
        this.resetActionBar();
    }


    public void resetActionBar() {
        this.actionBar.setTitle(this.getTitle());
    }

    public void resetTabLayout(){
        this.tab_layout.removeAllTabs();
        this.tab_layout.setVisibility(View.GONE);
    }


    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }
}
