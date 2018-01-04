package com.example.seongjun.biocube;

import android.app.Activity;
import android.bluetooth.BluetoothDevice;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.Window;

/**
 * Created by Seongjun on 2018. 1. 4..
 */

public class PopCheckCube extends Activity {
    BluetoothDevice device;
    CubeRegister mCubeRegister = new CubeRegister();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.popup_checkcube);

        Intent intent = getIntent();
        device = intent.getExtras().getParcelable("bluetoothDevice");
//        mCubeRegister.sendData("check");
    }


    public void mCheckNo(View v){
        //데이터 전달하기
        Intent intent = new Intent();
        setResult(RESULT_CANCELED, intent);

        //액티비티(팝업) 닫기
        finish();
    }
    public void mCheckYes(View v){
        Intent intent = new Intent(this, PopCubeRegist.class);
        intent.putExtra("bluetoothDevice", device);
        setResult(RESULT_OK, intent);
        startActivityForResult(intent, 1);

        finish();
    }
}
