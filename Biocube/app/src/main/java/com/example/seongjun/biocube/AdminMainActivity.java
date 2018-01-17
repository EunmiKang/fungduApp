
package com.example.seongjun.biocube;

import android.support.design.widget.TabLayout;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;

import java.util.concurrent.ExecutionException;

public class AdminMainActivity extends AppCompatActivity {
    private BackPressCloseHandler backPressCloseHandler;

    String adminID;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_admin_main);

        backPressCloseHandler = new BackPressCloseHandler(this);

        /* Id 가져오기 */
        try {
            adminID = new GetId().execute(this).get();
        } catch (InterruptedException e) {
            e.printStackTrace();
        } catch (ExecutionException e) {
            e.printStackTrace();
        }

        /* 뷰페이저 연결 */
        PagerAdapter mAdminPagerAdapter = new PagerAdapter(
                getSupportFragmentManager()
        );

        mAdminPagerAdapter.addFragment(R.drawable.menu_home,new AdminManualFragment());
        mAdminPagerAdapter.addFragment(R.drawable.menu_newsfeed, new UserNewspeedFragment());
        mAdminPagerAdapter.addFragment(R.drawable.menu_control, new CubeFragment());
        mAdminPagerAdapter.addFragment(R.drawable.menu_my, new AdminPageFragment());

        ViewPager mViewPager = (ViewPager) findViewById(R.id.viewpager_admin);
        mViewPager.setAdapter(mAdminPagerAdapter);

        /* 탭 설정 */
        TabLayout mTab = (TabLayout) findViewById(R.id.tabs);
        mTab.setupWithViewPager(mViewPager);

        for (int i = 0; i < mViewPager.getAdapter().getCount(); i++) {
            mTab.getTabAt(i).setIcon(mAdminPagerAdapter.getFragmentInfo(i).getIconResId());
        }
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        backPressCloseHandler.onBackPressed();
    }
}
