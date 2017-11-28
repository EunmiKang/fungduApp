package com.example.seongjun.biocube;

import android.support.design.widget.TabLayout;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;

public class ExpertMainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_expert_main);

        /* 뷰페이저 연결 */
        ExpertPagerAdapter mExpertPagerAdapter = new ExpertPagerAdapter(
                getSupportFragmentManager()
        );
        ViewPager mViewPager = (ViewPager) findViewById(R.id.viewpager_expert);
        mViewPager.setAdapter(mExpertPagerAdapter);

        /* 탭 설정 */
        TabLayout mTab = (TabLayout) findViewById(R.id.tabs);
        mTab.setupWithViewPager(mViewPager);
    }
}
