package com.example.seongjun.biocube;

import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;

/**
 * Created by Eunmi on 2017-11-01.
 */

public class UserPagerAdapter extends FragmentPagerAdapter {

    public UserPagerAdapter(FragmentManager fm) {
        super(fm);
    }

    @Override
    public Fragment getItem(int position) {
        switch (position) {
            case 0:
                return UserManualFragment.newInstance();
            case 1:
                return UserNewspeedFragment.newInstance();
            case 2:
                return WriteDiaryFragment.newInstance();
            case 3:
                return UserCubeFragment.newInstance();
            case 4:
                return UserPageFragment.newInstance();
            default:
                return null;
        }
    }

    private static int PAGE_NUMBER = 5;

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
                return "일지 작성";
            case 3:
                return "큐브 제어/등록";
            case 4:
                return "마이페이지";
            default:
                return null;
        }
    }
}
