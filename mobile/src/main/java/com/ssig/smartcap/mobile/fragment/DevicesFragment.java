package com.ssig.smartcap.mobile.fragment;

import android.content.Context;
import android.net.Uri;
import android.os.Bundle;
import android.support.design.widget.TabLayout;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.ssig.smartcap.mobile.R;

import java.util.ArrayList;
import java.util.List;


public class DevicesFragment extends Fragment {

    private ViewPager view_pager;
    private TabLayout tab_layout;


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setRetainInstance(true);
    }



    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.fragment_devices,container, false);
        // Setting ViewPager for each Tabs
        ViewPager viewPager = (ViewPager) view.findViewById(R.id.view_pager);
        setupViewPager(viewPager);
        // Set Tabs inside Toolbar
        TabLayout tabs;// = (TabLayout) view.findViewById(R.id.tab_layout);
        tabs = (TabLayout) getActivity().findViewById(R.id.tab_layout);
        tabs.setupWithViewPager(viewPager);
//        tabs.setVisibility(View.VISIBLE);

        return view;

    }


//    @Override
//    public View onCreateView(LayoutInflater inflater, ViewGroup container,
//                             Bundle savedInstanceState) {
//        // Inflate the layout for this fragment
//
//        return inflater.inflate(R.layout.fragment_devices, container, false);
//    }

//    @Override
//    public void onViewCreated(View view, Bundle savedInstanceState) {
////        Fragment childFragment = new ChildFragment();
////        FragmentTransaction transaction = getChildFragmentManager().beginTransaction();
////        transaction.replace(R.id.child_fragment_container, childFragment).commit();
//        initComponent();
//    }

//    private void initComponent() {
//        view_pager = (ViewPager) getView().findViewById(R.id.view_pager);
//        setupViewPager(view_pager);
//
//        tab_layout = (TabLayout) getActivity().findViewById(R.id.tab_layout);
//        tab_layout.setupWithViewPager(view_pager);
//        tab_layout.setVisibility(View.VISIBLE);
//
//    }



//    @Override
//    public void onResume() {
//        super.onResume();
//
//
////        tab_layout = (TabLayout) getActivity().findViewById(R.id.tab_layout);
////        tab_layout.setupWithViewPager(view_pager);
////        tab_layout.setVisibility(View.VISIBLE);
//    }


    private void setupViewPager(ViewPager viewPager) {
        SectionsPagerAdapter adapter = new SectionsPagerAdapter(getChildFragmentManager());
        adapter.addFragment(new SmartphoneDeviceFragment(), "Smartphone");
        adapter.addFragment(new SmartwatchDeviceFragment(), "Smartwatch");
        viewPager.setAdapter(adapter);
    }

    private class SectionsPagerAdapter extends FragmentPagerAdapter {

        private final List<Fragment> mFragmentList = new ArrayList<>();
        private final List<String> mFragmentTitleList = new ArrayList<>();

        public SectionsPagerAdapter(FragmentManager manager) {
            super(manager);
        }

        @Override
        public Fragment getItem(int position) {
            return mFragmentList.get(position);
        }

        @Override
        public int getCount() {
            return mFragmentList.size();
        }

        public void addFragment(Fragment fragment, String title) {
            mFragmentList.add(fragment);
            mFragmentTitleList.add(title);
        }

        @Override
        public CharSequence getPageTitle(int position) {
            return mFragmentTitleList.get(position);
        }
    }

}
