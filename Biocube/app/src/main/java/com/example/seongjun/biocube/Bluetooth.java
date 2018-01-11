package com.example.seongjun.biocube;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Handler;
import android.view.View;
import android.widget.Toast;

import java.io.InputStream;
import java.io.OutputStream;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

/**
 * Created by Seongjun on 2018. 1. 10..
 */

public class Bluetooth {

    final static int BLUETOOTH_REQUEST_CODE = 100;
    static final int REQUEST_ENABLE_BT = 10;
    int mPariedDeviceCount = 0;
    Set<BluetoothDevice> mDevices;
    // 폰의 블루투스 모듈을 사용하기 위한 오브젝트.
    BluetoothAdapter mBluetoothAdapter;
    /**
     BluetoothDevice 로 기기의 장치정보를 알아낼 수 있는 자세한 메소드 및 상태값을 알아낼 수 있다.
     연결하고자 하는 다른 블루투스 기기의 이름, 주소, 연결 상태 등의 정보를 조회할 수 있는 클래스.
     현재 기기가 아닌 다른 블루투스 기기와의 연결 및 정보를 알아낼 때 사용.
     */
    BluetoothDevice mRemoteDevice;
    // 스마트폰과 페어링 된 디바이스간 통신 채널에 대응 하는 BluetoothSocket
    BluetoothSocket mSocket = null;
    OutputStream mOutputStream = null;
    InputStream mInputStream = null;
    String mStrDelimiter = "\n";
    char mCharDelimiter =  '\n';
    Thread mWorkerThread = null;
    byte[] readBuffer;
    int readBufferPosition;


//    BluetoothDevice getDeviceFromBondedList(String name) {
//        // BluetoothDevice : 페어링 된 기기 목록을 얻어옴.
//        BluetoothDevice selectedDevice = null;
//        // getBondedDevices 함수가 반환하는 페어링 된 기기 목록은 Set 형식이며,
//        // Set 형식에서는 n 번째 원소를 얻어오는 방법이 없으므로 주어진 이름과 비교해서 찾는다.
//        for(BluetoothDevice device : mDevices) {
//            // getName() : 단말기의 Bluetooth Adapter 이름을 반환
//            if(name.equals(device.getName())) {
//                selectedDevice = device;
//                break;
//            }
//        }
//        return selectedDevice;
//    }

    BluetoothDevice getDeviceFromBondedListForMAC(String selectedDeviceMAC) {//맥주소로 디바이스 찾음
        // BluetoothDevice : 페어링 된 기기 목록을 얻어옴.
        BluetoothDevice selectedDevice = null;
        // getBondedDevices 함수가 반환하는 페어링 된 기기 목록은 Set 형식이며,
        // Set 형식에서는 n 번째 원소를 얻어오는 방법이 없으므로 주어진 이름과 비교해서 찾는다.
        for(BluetoothDevice device : mDevices) {
            // getName() : 단말기의 Bluetooth Adapter 이름을 반환
            if(selectedDeviceMAC.equals(device.getAddress())) {
                selectedDevice = device;
                break;
            }
        }
        return selectedDevice;
    }

    boolean connectToSelectedDevice(String selectedDevice, Set<BluetoothDevice> mDevices, BluetoothDevice device) {
        // BluetoothDevice 원격 블루투스 기기를 나타냄.
        this.mDevices = mDevices;
        mPariedDeviceCount = mDevices.size();
        mRemoteDevice = getDeviceFromBondedListForMAC(selectedDevice);
        if(mRemoteDevice == null){
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
            // 데이터 송수신을 위한 스트림 얻기.
            // BluetoothSocket 오브젝트는 두개의 Stream을 제공한다.
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

    BroadcastReceiver mBluetoothStateReceiver = new BroadcastReceiver() {//블루투스 연결되었을 때 Toast 띄움.
        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            switch(action){
                case BluetoothDevice.ACTION_ACL_CONNECTED:
                    Toast.makeText(context, "연결됨", Toast.LENGTH_SHORT).show();
                    break;
                case BluetoothDevice.ACTION_ACL_DISCONNECTED:
                    Toast.makeText(context, "연결해제", Toast.LENGTH_SHORT).show();
                    break;
                case "connectfail":
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

    int checkBluetooth(Context context) {
        /**
         * getDefaultAdapter() : 만일 폰에 블루투스 모듈이 없으면 null 을 리턴한다.
         이경우 Toast를 사용해 에러메시지를 표시하고 앱을 종료한다.
         */
        mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        if(mBluetoothAdapter == null ) {  // 블루투스 미지원
            return 0;
        }
        else { // 블루투스 지원
            /** isEnable() : 블루투스 모듈이 활성화 되었는지 확인.
             *               true : 지원 ,  false : 미지원
             */
            if(!mBluetoothAdapter.isEnabled()) { // 블루투스 지원하며 비활성 상태인 경우.

                // REQUEST_ENABLE_BT : 블루투스 활성 상태의 변경 결과를 App 으로 알려줄 때 식별자로 사용(0이상)
                /**
                 startActivityForResult 함수 호출후 다이얼로그가 나타남
                 "예" 를 선택하면 시스템의 블루투스 장치를 활성화 시키고
                 "아니오" 를 선택하면 비활성화 상태를 유지 한다.
                 선택 결과는 onActivityResult 콜백 함수에서 확인할 수 있다.
                 */
                return 1;
            }
            else { // 블루투스 지원하며 활성 상태인 경우.
                return 2;
//                selectDevice();
            }
        }
    }
    //새로운 소스적용 끝
}
