package com.example.seongjun.biocube;

import android.content.Context;
import android.os.Bundle;
import android.support.design.widget.TabLayout;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.view.LayoutInflater;
import android.widget.ImageView;
import android.widget.LinearLayout;

import java.io.IOException;
import java.util.concurrent.ExecutionException;


public class UserMainActivity extends AppCompatActivity {
    private BackPressCloseHandler backPressCloseHandler;

    String userID;
    Bluetooth mBluetooth = new Bluetooth();
    private static final String TAG = "CUBEFRAGEMENT";
    PagerAdapter mUserPagerAdapter;

    public static Context context;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_user_main);
        context = this;
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
        mUserPagerAdapter = new PagerAdapter(
                getSupportFragmentManager()
        );
        mUserPagerAdapter.addFragment(R.drawable.menu_control, new CubeFragment());
        mUserPagerAdapter.addFragment(R.drawable.menu_note, new WriteDiaryFragment());
        mUserPagerAdapter.addFragment(R.drawable.menu_newsfeed, new NewspeedFragment());
        mUserPagerAdapter.addFragment(R.drawable.menu_my, new UserPageFragment());
        mUserPagerAdapter.addFragment(R.drawable.menu_home,new ManualFragment());



        ViewPager mViewPager = (ViewPager) findViewById(R.id.viewpager_user);
        mViewPager.setAdapter(mUserPagerAdapter);
//        FragmentManager fragmentManager = getFragmentManager();
//        fragmentManager.findFragmentByTag("android:switcher:"+ R.id.viewpager_user+":"+2);
//        mCubeFragment = (CubeFragment)getSupportFragmentManager().findFragmentByTag("android:switcher:"+R.id.viewpager_user+":"+mViewPager.getCurrentItem()+1);
        /* 탭 설정 */
        TabLayout mTab = (TabLayout) findViewById(R.id.tabs);
        mTab.setupWithViewPager(mViewPager);

        for (int i = 0; i < mViewPager.getAdapter().getCount(); i++) {
            LinearLayout tab = (LinearLayout) LayoutInflater.from(this).inflate(R.layout.custom_tab, null);
            ((ImageView)tab.findViewById(R.id.img_tab)).setBackgroundResource(mUserPagerAdapter.getFragmentInfo(i).getIconResId());
            tab.setPadding(20,40,20,40);
            mTab.getTabAt(i).setCustomView(tab);
            //mTab.getTabAt(i).setIcon(mUserPagerAdapter.getFragmentInfo(i).getIconResId());
        }

        mViewPager.addOnPageChangeListener(new ViewPager.OnPageChangeListener() {

            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {

            }

            @Override
            public void onPageSelected(int position) {
                if(position == 0 || position == 1){
                    if(position==1){
                        if(((CubeFragment)mUserPagerAdapter.getItem(0)).mWorkerThread != null){
                            ((CubeFragment)mUserPagerAdapter.getItem(0)).mWorkerThread.interrupt();
                            ((CubeFragment)mUserPagerAdapter.getItem(0)).text_humi_air.setText("");
                            ((CubeFragment)mUserPagerAdapter.getItem(0)).text_temper.setText("");
                            ((CubeFragment)mUserPagerAdapter.getItem(0)).text_humi_soil.setText("");
                            for(int i = 0; i<12; i++) {
                                ((CubeFragment) mUserPagerAdapter.getItem(0)).ledTimeButtonArray[i].setBackgroundResource(R.drawable.cont_time_on_light_65x65);
                                ((CubeFragment) mUserPagerAdapter.getItem(0)).pumpTimeButtonArray[i].setBackgroundResource(R.drawable.cont_time_on_pump_65x65);
                                ((CubeFragment) mUserPagerAdapter.getItem(0)).ledTimeState[i] = 0;
                                ((CubeFragment) mUserPagerAdapter.getItem(0)).pumpTimeState[i] = 0;
                                ((CubeFragment) mUserPagerAdapter.getItem(0)).btn_led.setBackgroundResource(R.drawable.cont_time_light_on_145x135);
                                ((CubeFragment) mUserPagerAdapter.getItem(0)).btn_pump.setBackgroundResource(R.drawable.cont_time_pump_on_139x129);
                            }

                        }
//                        ViewPager viewPager = (ViewPager) findViewById(R.id.viewpager_user);
//                        int id = viewPager.getCurrentItem();
//                        fragment = getFragmentManager().findFragmentByTag("android:switcher:"+R.id.viewpager_user+":"+id);

                    }
                    if(mBluetooth.mSocket != null) {
                        try {
                            mBluetooth.mSocket.close();
                            ((CubeFragment)((UserMainActivity)UserMainActivity.context).mUserPagerAdapter.getItem(0)).bluetoothFlag = false;
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
    public void onDestroy() {//앱 종료시,
        try{
            if(mBluetooth.mSocket != null) {
                mBluetooth.mSocket.close();
            }
        }catch(Exception e){}
        super.onDestroy();
    }
    @Override
    public void onBackPressed() {
//        super.onBackPressed();
        backPressCloseHandler.onBackPressed();
    }
}
