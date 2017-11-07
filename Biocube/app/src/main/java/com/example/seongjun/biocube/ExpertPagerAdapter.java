package com.example.seongjun.biocube;

import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;

/**
 * Created by Eunmi on 2017-11-07.
 */

public class ExpertPagerAdapter extends FragmentPagerAdapter {

    public ExpertPagerAdapter(FragmentManager fm) {
        super(fm);
    }

    @Override
    public Fragment getItem(int position) {
        switch (position) {
            case 0:
                return ExpertManualFragment.newInstance();
            case 1:
                return ExpertNewspeedFragment.newInstance();
            case 2:
                return ExpertPageFragment.newInstance();
            default:
                return null;
        }
    }

    private static int PAGE_NUMBER = 3;

    @Override
    public int getCount() {
        return PAGE_NUMBER;
    }

    @Override
    public CharSequence getPageTitle(int position) {
        switch (position) {
            case 0:
                return "매뉴얼";
            case 1:
                return "뉴스피드";
            case 2:
                return "마이페이지";
            default:
                return null;
        }
    }
}
