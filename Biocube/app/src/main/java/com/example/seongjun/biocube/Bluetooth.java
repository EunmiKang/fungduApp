package com.example.seongjun.biocube;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.widget.Toast;

import java.io.InputStream;
import java.io.OutputStream;
import java.util.Set;
import java.util.UUID;

/**
 * Created by Seongjun on 2018. 1. 10..
 */

public class Bluetooth {

    static final int REQUEST_ENABLE_BT = 10;
    int mPariedDeviceCount = 0;
    Set<BluetoothDevice> mDevices;//검색한 디바이스들을 담을 것
    BluetoothAdapter mBluetoothAdapter;
    BluetoothDevice mRemoteDevice;
    BluetoothSocket mSocket = null;// 스마트폰과 페어링 된 디바이스간 통신 채널에 대응 하는 BluetoothSocket
    OutputStream mOutputStream = null;
    InputStream mInputStream = null;
    String mStrDelimiter = "\n";
    
    BluetoothDevice getDeviceFromBondedListForMAC(String selectedDeviceMAC) {//맥주소로 디바이스 찾는 메소드
        // BluetoothDevice : 페어링 된 기기 목록을 얻어옴.
        BluetoothDevice selectedDevice = null;
        // getBondedDevices 함수가 반환하는 페어링 된 기기 목록은 Set 형식이며,
        for(BluetoothDevice device : mDevices) {
            // getName() : 단말기의 Bluetooth Adapter 이름을 반환
            if(selectedDeviceMAC.equals(device.getAddress())) {//선택한 디바이스의 맥주소를 찾으면,
                selectedDevice = device;//찾은 디바이스를 리턴한다.
                break;
            }
        }
        return selectedDevice;
    }

    boolean connectToSelectedDevice(String selectedDevice, Set<BluetoothDevice> mDevices, BluetoothDevice device) {
        //선택한 디바이스와 폰을 블루투스로 연결하는 메소드
        this.mDevices = mDevices;
        mPariedDeviceCount = mDevices.size();
        mRemoteDevice = getDeviceFromBondedListForMAC(selectedDevice);//선택한 디바이스를 맥주소를 통해 페어링 되었던 목록에서 찾는다.
        if(mRemoteDevice == null){//찾지 못했으면 연결된 적이 없는 디바이스. 바로 그냥 선택한 디바이스를 사용.
            mRemoteDevice = device;
        }
        // java.util.UUID.fromString : 자바에서 중복되지 않는 Unique 키 생성.
        UUID uuid = java.util.UUID.fromString("00001101-0000-1000-8000-00805f9b34fb");

        try {
            // 소켓 생성, RFCOMM 채널을 통한 연결.
            // createRfcommSocketToServiceRecord(uuid) : 이 함수를 사용하여 원격 블루투스 장치와 통신할 수 있는 소켓을 생성함.
            // 이 메소드가 성공하면 스마트폰과 페어링 된 디바이스간 통신 채널에 대응하는 BluetoothSocket 오브젝트를 리턴함.
            mSocket = mRemoteDevice.createRfcommSocketToServiceRecord(uuid);
            mSocket.connect(); // 소켓이 생성 되면 connect() 함수를 호출함으로써 두기기의 연결은 완료된다.
            // 1. 데이터를 보내기 위한 OutputStrem
            // 2. 데이터를 받기 위한 InputStream
            mOutputStream = mSocket.getOutputStream();
            mInputStream = mSocket.getInputStream();

            return true;

        }catch(Exception e) { // 블루투스 연결 중 오류 발생
            e.printStackTrace();
//            Toast.makeText(getApplicationContext(), "블루투스 연결 중 오류가 발생했습니다.", Toast.LENGTH_LONG).show();
            return false;  // App 종료
        }
    }

    BroadcastReceiver mBluetoothStateReceiver = new BroadcastReceiver() {//블루투스 연결 상태에 따른 토스트 메시지를 띄움
        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            switch(action){
                case BluetoothDevice.ACTION_ACL_CONNECTED://연결되었을 때
                    Toast.makeText(context, "연결됨", Toast.LENGTH_SHORT).show();
                    break;
                case BluetoothDevice.ACTION_ACL_DISCONNECTED://연결이 해제 되었을 때
                    Toast.makeText(context, "연결해제", Toast.LENGTH_SHORT).show();
                    break;
                case "connectfail"://연결을 실패했을 때,
                    Toast.makeText(context, "연결실패", Toast.LENGTH_SHORT).show();
                    break;
            }
        }
    };


    //데이터 전송
    boolean sendData(String msg) {
        msg += mStrDelimiter;  // 문자열 종료표시 (\n)
        try{
            // getBytes() : String을 byte로 변환
            // OutputStream.write : 데이터를 쓸때는 write(byte[]) 메소드를 사용함. byte[] 안에 있는 데이터를 한번에 기록해 준다.
            mOutputStream.write(msg.getBytes());  // 문자열 전송.
            return true;
        }catch(Exception e) {  // 문자열 전송 도중 오류가 발생한 경우
//            Toast.makeText(getApplicationContext(), "데이터 전송중 오류가 발생", Toast.LENGTH_LONG).show();
//            finish();  // App 종료
            return false;
        }
    }

    int checkBluetooth(Context context) {//블루투스 연결이 가능한지 확인하는 메소드

        mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();/* getDefaultAdapter() : 만일 폰에 블루투스 모듈이 없으면 null 을 리턴한다.
        이경우 Toast를 사용해 에러메시지를 표시하고 앱을 종료한다.*/
        if(mBluetoothAdapter == null ) {  // 블루투스 미지원
            return 0;
        }
        else { // 블루투스 지원
            if(!mBluetoothAdapter.isEnabled()) { // 블루투스 지원하며 비활성 상태인 경우.
                return 1;
            }
            else { // 블루투스 지원하며 활성 상태인 경우.
                return 2;
            }
        }
    }
}
