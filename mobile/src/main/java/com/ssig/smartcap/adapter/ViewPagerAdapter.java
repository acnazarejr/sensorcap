package com.ssig.smartcap.adapter;

import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.view.ViewGroup;
import com.ssig.smartcap.fragment.AbstractMainFragment;

import java.util.ArrayList;

public class ViewPagerAdapter extends FragmentPagerAdapter {

    private final ArrayList<AbstractMainFragment> abstractMainFragments = new ArrayList<>();
    private AbstractMainFragment mCurrentFragment;

    public ViewPagerAdapter(FragmentManager fragmentManager) {
        super(fragmentManager);
    }

    public void add(AbstractMainFragment fragment) {
        this.abstractMainFragments.add(fragment);
    }

    @Override
    public Fragment getItem(int position) {
        return abstractMainFragments.get(position);
    }

    @Override
    public int getCount() {
        return abstractMainFragments.size();
    }

    @Override
    public void setPrimaryItem(ViewGroup container, int position, Object object) {
        if (getCurrentFragment() != object)
            mCurrentFragment = ((AbstractMainFragment) object);
        super.setPrimaryItem(container, position, object);
    }

    public AbstractMainFragment getCurrentFragment() {
        return mCurrentFragment;
    }
}
