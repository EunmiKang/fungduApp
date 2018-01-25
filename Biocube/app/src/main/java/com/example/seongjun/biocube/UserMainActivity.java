package com.example.seongjun.biocube;

import android.app.FragmentManager;
import android.app.FragmentTransaction;
import android.os.Bundle;
import android.support.design.widget.TabLayout;
import android.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.Stack;
import java.util.concurrent.ExecutionException;


public class UserMainActivity extends AppCompatActivity {
    private BackPressCloseHandler backPressCloseHandler;

    String userID;
    Bluetooth mBluetooth = new Bluetooth();
    private static final String TAG = "CUBEFRAGEMENT";
    CubeFragment mCubeFragment;
    Fragment fragment;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_user_main);

        backPressCloseHandler = new BackPressCloseHandler(this);

        /* Id 가져오기 */
        try {
            userID = new GetId().execute(this).get();
        } catch (InterruptedException e) {
            e.printStackTrace();
        } catch (ExecutionException e) {
            e.printStackTrace();
        }

        /* 뷰페이저 연결 */
        final PagerAdapter mUserPagerAdapter = new PagerAdapter(
                getSupportFragmentManager()
        );
        mUserPagerAdapter.addFragment(R.drawable.menu_home,new ManualFragment());
        mUserPagerAdapter.addFragment(R.drawable.menu_newsfeed, new NewspeedFragment());
        mUserPagerAdapter.addFragment(R.drawable.menu_note, new WriteDiaryFragment());
        mUserPagerAdapter.addFragment(R.drawable.menu_control, new CubeFragment());
        mUserPagerAdapter.addFragment(R.drawable.menu_my, new UserPageFragment());


        ViewPager mViewPager = (ViewPager) findViewById(R.id.viewpager_user);
        mViewPager.setAdapter(mUserPagerAdapter);
//        FragmentManager fragmentManager = getFragmentManager();
//        fragmentManager.findFragmentByTag("android:switcher:"+ R.id.viewpager_user+":"+2);
//        mCubeFragment = (CubeFragment)getSupportFragmentManager().findFragmentByTag("android:switcher:"+R.id.viewpager_user+":"+mViewPager.getCurrentItem()+1);
        /* 탭 설정 */
        TabLayout mTab = (TabLayout) findViewById(R.id.tabs);
        mTab.setupWithViewPager(mViewPager);

        for (int i = 0; i < mViewPager.getAdapter().getCount(); i++) {
            mTab.getTabAt(i).setIcon(mUserPagerAdapter.getFragmentInfo(i).getIconResId());
        }

        mViewPager.addOnPageChangeListener(new ViewPager.OnPageChangeListener() {

            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {

            }

            @Override
            public void onPageSelected(int position) {
                if(position == 2 || position == 3){
                    if(position==2){
                        if(((CubeFragment)mUserPagerAdapter.getItem(3)).mWorkerThread != null){
                            ((CubeFragment)mUserPagerAdapter.getItem(3)).mWorkerThread.interrupt();
                        }
//                        ViewPager viewPager = (ViewPager) findViewById(R.id.viewpager_user);
//                        int id = viewPager.getCurrentItem();
//                        fragment = getFragmentManager().findFragmentByTag("android:switcher:"+R.id.viewpager_user+":"+id);

                    }
                    if(mBluetooth.mSocket != null) {
                        try {
                            mBluetooth.mSocket.close();
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    }
                }
            }

            @Override
            public void onPageScrollStateChanged(int state) {

            }
        });
    }


    @Override
    public void onBackPressed() {
//        super.onBackPressed();
        backPressCloseHandler.onBackPressed();
    }
//    public void stopThread(){
//        mCubeFragment.mWorkerThread.interrupt();
//    }

}
