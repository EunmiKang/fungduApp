package com.example.seongjun.biocube;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Toast;

import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.UUID;

/**
 * Created by Seongjun on 2017. 11. 7..
 */

public class CubeRegister extends AppCompatActivity {

    BluetoothAdapter mBluetoothAdapter;
    int REQUEST_ENABLE_BT = 1;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_resgist_cube);
        checkBlueTooth();
    }


    void checkBlueTooth() {

        mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        if(mBluetoothAdapter == null) {
            // 장치가 블루투스 지원하지 않는 경우
            finish();   // 어플리케이션 종료
        }

        else {
            // 장치가 블루투스 지원하는 경우
            if(!mBluetoothAdapter.isEnabled()) {
                // 블루투스를 지원하지만 비활성 상태인 경우
                // 블루투스를 활성 상태로 바꾸기 위해 사용자 동의 요첨
                Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
                startActivityForResult(enableBtIntent, REQUEST_ENABLE_BT);
            }

            else {
                // 블루투스를 지원하며 활성 상태인 경우
                // 페어링된 기기 목록을 보여주고 연결할 장치를 선택.
                    selectDevice();
            }
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        switch(requestCode) {
            case 1:
                if(resultCode == RESULT_OK) {
                    selectDevice();
                    // 블루투스가 활성 상태로 변경됨
                }

                else if(resultCode == RESULT_CANCELED) {
                    // 블루투스가 비활성 상태임
                    finish();  //  어플리케이션 종료
                }
                break;
        }
        super.onActivityResult(requestCode, resultCode, data);
    }

    int mPairedDeviceCount;
    Set<BluetoothDevice> mDevices;
    void selectDevice() {
        mDevices = mBluetoothAdapter.getBondedDevices();
        mPairedDeviceCount = mDevices.size();

        if(mPairedDeviceCount == 0 ) {
            //  페어링 된 장치가 없는 경우
            Toast.makeText(getApplicationContext(), "페어링된 장치가 없습니다.", Toast.LENGTH_SHORT).show();
        }


        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("블루투스 장치 선택");


        // 페어링 된 블루투스 장치의 이름 목록 작성
        List<String> listItems = new ArrayList<String>();
        for(BluetoothDevice device : mDevices) {
            listItems.add(device.getName());
            }
            listItems.add("취소");    // 취소 항목 추가

            final CharSequence[] items = listItems.toArray(new CharSequence[listItems.size()]);


            builder.setItems(items, new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog, int item) {
                    if(item == mPairedDeviceCount) {
                        // 연결할 장치를 선택하지 않고 '취소'를 누른 경우
                        finish();
                    }
                    else {
                        // 연결할 장치를 선택한 경우
                        // 선택한 장치와 연결을 시도함
                        connectToSelectedDevices(items[item].toString());
                    }
                }
            });


//            builder.setCancelable(false);    // 뒤로 가기 버튼 사용 금지
            AlertDialog alert = builder.create();
            alert.show();
        }


    BluetoothDevice getDeviceFromBondedList(String name) {
        //이름 주어졌을때 해당하는 블루투스 장치 객체를 페어링 된 장치 목록에서 찾아내는 코드의 예이다.
        BluetoothDevice selectedDevice = null;

        for(BluetoothDevice device : mDevices) {
            if(name.equals(device.getName())) {
                selectedDevice = device;
                break;
            }
        }
        return selectedDevice;
    }


    public void connectToSelectedDevices(String selectedDeviceName) {
        BluetoothDevice mRemoteDevice = getDeviceFromBondedList(selectedDeviceName);
        UUID uuid = UUID.fromString("00001101-0000-1000-8000-00805f9b34fb");

        try {
            // 소켓 생성
            BluetoothSocket mSocket = mRemoteDevice.createRfcommSocketToServiceRecord(uuid);
            // RFCOMM 채널을 통한 연결
            mSocket.connect();

            // 데이터 송수신을 위한 스트림 열기
            OutputStream mOutputStream;
            InputStream mInputStream;
            mOutputStream = mSocket.getOutputStream();
            mInputStream = mSocket.getInputStream();

            // 데이터 수신 준비
//            beginListenForData();
        }catch(Exception e) {
            // 블루투스 연결 중 오류 발생
            finish();   // 어플 종료
        }
    }

}