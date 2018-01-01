package com.example.seongjun.biocube;

import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Eunmi on 2017-11-01.
 */

public class UserPagerAdapter extends FragmentPagerAdapter {

    public UserPagerAdapter(FragmentManager fm) {
        super(fm);
    }

//    @Override
//    public Fragment getItem(int position) {
//        switch (position) {
//            case 0:
//                return UserManualFragment.newInstance();
//            case 1:
//                return UserNewspeedFragment.newInstance();
//            case 2:
//                return WriteDiaryFragment.newInstance();
//            case 3:
//                return CubeFragment.newInstance();
//            case 4:
//                return UserPageFragment.newInstance();
//            default:
//                return null;
//        }
//    }

    private static int PAGE_NUMBER = 5;

    @Override
    public int getCount() {
        return PAGE_NUMBER;
    }

//    @Override
//    public CharSequence getPageTitle(int position) {
//        switch (position) {
//            case 0:
//                return "매뉴얼";
//            case 1:
//                return "뉴스피드";
//            case 2:
//                return "일지 작성";
//            case 3:
//                return "큐브 제어/등록";
//            case 4:
//                return "마이페이지";
//            default:
//                return null;
//        }
//    }

//    public int getIcon(int position) {
//        switch (position) {
//            case 0:
//                return R.drawable.menu_home;
//            case 1:
//                return R.drawable.menu_newsfeed;
//            case 2:
//                return R.drawable.menu_note;
//            case 3:
//                return R.drawable.menu_control;
//            case 4:
//                return R.drawable.menu_my;
//            default:
//                return R.drawable.menu_home;
//        }
//    }

    private final List<FragmentInfo> mFragmentInfoList = new ArrayList<FragmentInfo>();

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
