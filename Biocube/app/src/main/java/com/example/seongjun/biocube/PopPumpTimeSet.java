package com.example.seongjun.biocube;

import android.app.Activity;
import android.content.Context;
import android.os.Bundle;
import android.view.View;
import android.view.Window;
import android.widget.Button;

import java.util.concurrent.ExecutionException;

/**
 * Created by Seongjun on 2018. 2. 9..
 */

public class PopPumpTimeSet extends Activity {
    //UI
    Button btn_am;
    Button btn_pm;
    Button[] timeButtonArray = new Button[24];

    //var
    String id;
    public static Context mContext;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.set_time_led);

        mContext = this;

        try {//로그인된 아이디 불러옴
            id = new GetId().execute(getApplicationContext()).get();
        } catch (InterruptedException e) {
            e.printStackTrace();
        } catch (ExecutionException e) {
            e.printStackTrace();
        }

        btn_am = (Button)findViewById(R.id.btn_am);
        btn_pm = (Button)findViewById(R.id.btn_pm);
        timeButtonArray[0] =(Button)findViewById(R.id.am_00);
        timeButtonArray[1] =(Button)findViewById(R.id.am_01);
        timeButtonArray[2] = (Button)findViewById(R.id.am_02);
        timeButtonArray[3] = (Button)findViewById(R.id.am_03);
        timeButtonArray[4] = (Button)findViewById(R.id.am_04);
        timeButtonArray[5] = (Button)findViewById(R.id.am_05);
        timeButtonArray[6] = (Button)findViewById(R.id.am_06);
        timeButtonArray[7] = (Button)findViewById(R.id.am_07);
        timeButtonArray[8] = (Button)findViewById(R.id.am_08);
        timeButtonArray[9] = (Button)findViewById(R.id.am_10);
        timeButtonArray[10] = (Button)findViewById(R.id.am_09);
        timeButtonArray[11] = (Button)findViewById(R.id.am_11);
        timeButtonArray[12] = (Button)findViewById(R.id.pm_12);
        timeButtonArray[13] = (Button)findViewById(R.id.pm_13);
        timeButtonArray[14] = (Button)findViewById(R.id.pm_14);
        timeButtonArray[15] = (Button)findViewById(R.id.pm_15);
        timeButtonArray[16] = (Button)findViewById(R.id.pm_16);
        timeButtonArray[17] = (Button)findViewById(R.id.pm_17);
        timeButtonArray[18] = (Button)findViewById(R.id.pm_18);
        timeButtonArray[19] = (Button)findViewById(R.id.pm_19);
        timeButtonArray[20] = (Button)findViewById(R.id.pm_20);
        timeButtonArray[21] = (Button)findViewById(R.id.pm_21);
        timeButtonArray[22] = (Button)findViewById(R.id.pm_22);
        timeButtonArray[23] = (Button)findViewById(R.id.pm_23);

        btn_am.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                for(int i = 0; i < 12; i++){
                    timeButtonArray[i].setVisibility(View.VISIBLE);
                }
                for(int i = 12; i < timeButtonArray.length; i++){
                    timeButtonArray[i].setVisibility(View.GONE);
                }
            }
        });

        btn_pm.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                for(int i = 0; i < 12; i++){
                    timeButtonArray[i].setVisibility(View.GONE);
                }
                for(int i = 12; i < timeButtonArray.length; i++){
                    timeButtonArray[i].setVisibility(View.VISIBLE);
                }
            }
        });

        for(int i = 0; i < timeButtonArray.length; i++){
            timeButtonArray[i].setOnClickListener(timeClickListener);
        }
        setColor();
    }

    View.OnClickListener timeClickListener = new View.OnClickListener(){
        @Override
        public void onClick(View v) {
            String time_string = ((Button)v).getText().toString();
            setTime("set_timepump"+time_string);
            int time_int = Integer.parseInt(time_string);

            if(id.equals("admin")){
                if(((CubeFragment)((AdminMainActivity)AdminMainActivity.context).mAdminPagerAdapter.getItem(3)).pumpState[time_int] == 0){
                    ((CubeFragment)((AdminMainActivity)AdminMainActivity.context).mAdminPagerAdapter.getItem(3)).pumpState[time_int] = 1;
                    ((Button)v).setBackgroundResource(R.color.navy);
                }
                else{
                    ((CubeFragment)((AdminMainActivity)AdminMainActivity.context).mAdminPagerAdapter.getItem(3)).pumpState[time_int] = 0;
                    ((Button)v).setBackgroundResource(R.color.lightYellow);
                }
            }
            else{
                if(((CubeFragment)((UserMainActivity)UserMainActivity.context).mUserPagerAdapter.getItem(3)).pumpState[time_int] == 0){
                    ((CubeFragment)((UserMainActivity)UserMainActivity.context).mUserPagerAdapter.getItem(3)).pumpState[time_int] = 1;
                    ((Button)v).setBackgroundResource(R.color.navy);
                }
                else{
                    ((CubeFragment)((UserMainActivity)UserMainActivity.context).mUserPagerAdapter.getItem(3)).pumpState[time_int] = 0;
                    ((Button)v).setBackgroundResource(R.color.lightYellow);
                }
            }
        }
    };
    public void setTime(String msg){
        if(id.equals("admin")){//admin일때,
            if(((((AdminMainActivity)AdminMainActivity.context)).mBluetooth.mSocket != null)){
                (((AdminMainActivity)AdminMainActivity.context)).mBluetooth.sendData(msg);
            }
        }
        else{//user가 아닐때
            if(((((UserMainActivity)UserMainActivity.context)).mBluetooth.mSocket != null)){
                ((UserMainActivity)UserMainActivity.context).mBluetooth.sendData(msg);
            }
        }
    }
    public void setColor(){
        if(id.equals("admin")){
            for(int i = 0; i < 24; i++){
                if( ((CubeFragment)((AdminMainActivity)AdminMainActivity.context).mAdminPagerAdapter.getItem(3)).pumpState[i] == 1){
                    timeButtonArray[i].setBackgroundResource(R.color.navy);
                }
            }
        }
        else{
            for(int i = 0; i < 24; i++){
                if(((CubeFragment)((UserMainActivity)UserMainActivity.context).mUserPagerAdapter.getItem(3)).pumpState[i] == 1){
                    timeButtonArray[i].setBackgroundResource(R.color.navy);
                }
            }
        }
    }
}
