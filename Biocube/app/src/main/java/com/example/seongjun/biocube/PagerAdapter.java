package com.example.seongjun.biocube;

import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by Seongjun on 2018. 1. 1..
 */
@SuppressWarnings("serial")
public class PagerAdapter extends FragmentPagerAdapter implements Serializable {

    public PagerAdapter(FragmentManager fm) {
        super(fm);
    }


    private final List<FragmentInfo> mFragmentInfoList = new ArrayList<FragmentInfo>();

    @Override
    public int getCount() {
        return mFragmentInfoList.size();
    }

    public void addFragment(int iconResId, Fragment fragment) {
        FragmentInfo info = new FragmentInfo(iconResId, fragment);
        mFragmentInfoList.add(info);
    }

    public FragmentInfo getFragmentInfo(int position) {
        return mFragmentInfoList.get(position);
    }

    @Override
    public Fragment getItem(int position) {
        return mFragmentInfoList.get(position).getFragment();
    }


    class FragmentInfo {
        private int iconResId;
        private Fragment fragment;

        public FragmentInfo(int iconResId, Fragment fragment) {
            this.iconResId = iconResId;
            this.fragment = fragment;
        }

        public int getIconResId() {
            return iconResId;
        }

        public Fragment getFragment() {
            return fragment;
        }
    }
}
