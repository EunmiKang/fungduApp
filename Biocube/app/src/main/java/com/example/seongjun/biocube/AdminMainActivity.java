
package com.example.seongjun.biocube;

import android.support.design.widget.TabLayout;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;

public class AdminMainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_admin_main);

        /* 뷰페이저 연결 */
        AdminPagerAdapter mAdminPagerAdapter = new AdminPagerAdapter(
                getSupportFragmentManager()
        );
        ViewPager mViewPager = (ViewPager) findViewById(R.id.viewpager);
        mViewPager.setAdapter(mAdminPagerAdapter);

        /* 탭 설정 */
        TabLayout mTab = (TabLayout) findViewById(R.id.tabs);
        mTab.setupWithViewPager(mViewPager);
    }
}
