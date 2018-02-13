package com.example.seongjun.biocube;

import android.content.Context;
import android.support.design.widget.TabLayout;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;

import java.util.concurrent.ExecutionException;

public class ExpertMainActivity extends AppCompatActivity {

    private BackPressCloseHandler backPressCloseHandler;

    String expertID;

    PagerAdapter mExpertPagerAdapter;

    public static Context mContext;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_expert_main);

        mContext = this;

        backPressCloseHandler = new BackPressCloseHandler(this);

        /* Id 가져오기 */
        try {
            expertID = new GetId().execute(this).get();
        } catch (InterruptedException e) {
            e.printStackTrace();
        } catch (ExecutionException e) {
            e.printStackTrace();
        }

        /* 뷰페이저 연결 */
        mExpertPagerAdapter = new PagerAdapter(
                getSupportFragmentManager()
        );

        mExpertPagerAdapter.addFragment(R.drawable.menu_newsfeed, new NewspeedFragment());
        mExpertPagerAdapter.addFragment(R.drawable.menu_home,new ManualFragment());
        mExpertPagerAdapter.addFragment(R.drawable.menu_my, new ExpertPageFragment());

        ViewPager mViewPager = (ViewPager) findViewById(R.id.viewpager_expert);
        mViewPager.setAdapter(mExpertPagerAdapter);

        /* 탭 설정 */
        TabLayout mTab = (TabLayout) findViewById(R.id.tabs);
        mTab.setupWithViewPager(mViewPager);

        for (int i = 0; i < mViewPager.getAdapter().getCount(); i++) {
            mTab.getTabAt(i).setIcon(mExpertPagerAdapter.getFragmentInfo(i).getIconResId());
        }
    }

    @Override
    public void onBackPressed() {
//        super.onBackPressed();
        backPressCloseHandler.onBackPressed();
    }
}
