
package com.example.seongjun.biocube;

import android.content.Context;
import android.support.design.widget.TabLayout;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.widget.ImageView;
import android.widget.LinearLayout;

import java.util.concurrent.ExecutionException;

public class AdminMainActivity extends AppCompatActivity {
    private BackPressCloseHandler backPressCloseHandler;

    String adminID;
    Bluetooth mBluetooth = new Bluetooth();
    PagerAdapter mAdminPagerAdapter;

    public static Context context;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_admin_main);
        context = this;
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
        mAdminPagerAdapter = new PagerAdapter(
                getSupportFragmentManager()
        );

        mAdminPagerAdapter.addFragment(R.drawable.menu_control, new CubeFragment());
        mAdminPagerAdapter.addFragment(R.drawable.menu_newsfeed, new NewspeedFragment());
        mAdminPagerAdapter.addFragment(R.drawable.menu_my, new AdminPageFragment());
        mAdminPagerAdapter.addFragment(R.drawable.menu_home,new ManualFragment());

        ViewPager mViewPager = (ViewPager) findViewById(R.id.viewpager_admin);
        mViewPager.setAdapter(mAdminPagerAdapter);

        /* 탭 설정 */
        TabLayout mTab = (TabLayout) findViewById(R.id.tabs);
        mTab.setupWithViewPager(mViewPager);

        for (int i = 0; i < mViewPager.getAdapter().getCount(); i++) {
            LinearLayout tab = (LinearLayout) LayoutInflater.from(this).inflate(R.layout.custom_tab, null);
            ((ImageView)tab.findViewById(R.id.img_tab)).setBackgroundResource(mAdminPagerAdapter.getFragmentInfo(i).getIconResId());
            tab.setPadding(30,40,30,40);
            mTab.getTabAt(i).setCustomView(tab);
            //mTab.getTabAt(i).setIcon(mAdminPagerAdapter.getFragmentInfo(i).getIconResId());
        }
    }

    @Override
    public void onBackPressed() {
//        super.onBackPressed();
        backPressCloseHandler.onBackPressed();
    }
}
