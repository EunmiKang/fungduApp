package com.example.seongjun.biocube;

import android.os.Bundle;
import android.support.design.widget.TabLayout;
import android.support.v4.view.ViewPager;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;

import java.util.concurrent.ExecutionException;

public class UserMainActivity extends AppCompatActivity {
    String userID;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_user_main);

        /* Id 가져오기 */
        try {
            userID = new GetId().execute(this).get();
        } catch (InterruptedException e) {
            e.printStackTrace();
        } catch (ExecutionException e) {
            e.printStackTrace();
        }

        /* 뷰페이저 연결 */
        UserPagerAdapter mUserPagerAdapter = new UserPagerAdapter(
                getSupportFragmentManager()
        );
        mUserPagerAdapter.addFragment(R.drawable.menu_home,new UserManualFragment());
        mUserPagerAdapter.addFragment(R.drawable.menu_newsfeed, new UserNewspeedFragment());
        mUserPagerAdapter.addFragment(R.drawable.menu_note, new WriteDiaryFragment());
        mUserPagerAdapter.addFragment(R.drawable.menu_control, new CubeFragment());
        mUserPagerAdapter.addFragment(R.drawable.menu_my, new UserPageFragment());

        ViewPager mViewPager = (ViewPager) findViewById(R.id.viewpager_user);
        mViewPager.setAdapter(mUserPagerAdapter);


        /* 탭 설정 */
        TabLayout mTab = (TabLayout) findViewById(R.id.tabs);
        mTab.setupWithViewPager(mViewPager);

        for (int i = 0; i < mViewPager.getAdapter().getCount(); i++) {
            mTab.getTabAt(i).setIcon(mUserPagerAdapter.getFragmentInfo(i).getIconResId());
        }
    }
}
