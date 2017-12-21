package com.example.seongjun.biocube;

import android.Manifest;
import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.ListView;
import android.widget.SimpleAdapter;
import android.widget.TextView;
import android.widget.Toast;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.lang.reflect.Method;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

/**
 * Created by Seongjun on 2017. 11. 7..
 */

public class CubeRegister extends AppCompatActivity {
    BluetoothAdapter mBluetoothAdapter;
    Intent popUpIntent = new Intent(this, PopCubeRegist.class);

    //블루투스 요청 액티비티 코드
    final static int BLUETOOTH_REQUEST_CODE = 100;

    //UI
    TextView txtState;
    Button btnSearch;
    ListView listDevice;

    //Adapter
    SimpleAdapter adapterDevice;

    //list - Device 목록 저장
    List<Map<String,String>> dataDevice;
    //
    List<BluetoothDevice> bluetoothDevices;

    int selectDevice;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_resgist_cube);

        //UI
        txtState = (TextView) findViewById(R.id.txtState);
        btnSearch = (Button) findViewById(R.id.btnSearch);
        listDevice = (ListView) findViewById(R.id.listDevice);

        //Adapter
        dataDevice = new ArrayList<>();
        bluetoothDevices = new ArrayList<>();
        selectDevice = -1;
        adapterDevice = new
                SimpleAdapter(this, dataDevice, android.R.layout.simple_list_item_2, new String[]{"name", "address"}, new int[]{android.R.id.text1, android.R.id.text2});
        listDevice.setAdapter(adapterDevice);

        //블루투스 지원 유무 확인
        mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();

        //블루투스를 지원하지 않으면 null을 리턴한다
        if (mBluetoothAdapter == null) {
            Toast.makeText(this, "블루투스를 지원하지 않는 단말기 입니다.", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        // 블루투스 권한 요청(마쉬멜로우 버전 이상)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (ContextCompat.checkSelfPermission(CubeRegister.this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                // Should we show an explanation?


                if (ActivityCompat.shouldShowRequestPermissionRationale(CubeRegister.this, Manifest.permission.ACCESS_FINE_LOCATION)) {
                    // Show an expanation to the user *asynchronously* -- don't block
                    // this thread waiting for the user's response! After the user
                    // sees the explanation, try again to request the permission.


                } else {
                    // No explanation needed, we can request the permission.
                    ActivityCompat.requestPermissions(CubeRegister.this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, BLUETOOTH_REQUEST_CODE);

                    // BLUETOOTH_REQUEST_CODE is an
                    // app-defined int constant. The callback method gets the
                    // result of the request.
                }
            }
            else {
                Toast.makeText(CubeRegister.this, "들어옴", Toast.LENGTH_SHORT).show();
            }
        }

        //블루투스 브로드캐스트 리시버 등록
        //리시버1
        IntentFilter stateFilter = new IntentFilter();
        stateFilter.addAction(BluetoothAdapter.ACTION_STATE_CHANGED); //BluetoothAdapter.ACTION_STATE_CHANGED : 블루투스 상태변화 액션
        registerReceiver(mBluetoothStateReceiver, stateFilter);
        //리시버2
        IntentFilter searchFilter = new IntentFilter();
        searchFilter.addAction(BluetoothAdapter.ACTION_DISCOVERY_STARTED); //BluetoothAdapter.ACTION_DISCOVERY_STARTED : 블루투스 검색 시작
        searchFilter.addAction(BluetoothDevice.ACTION_FOUND); //BluetoothDevice.ACTION_FOUND : 블루투스 디바이스 찾음
        searchFilter.addAction(BluetoothAdapter.ACTION_DISCOVERY_FINISHED); //BluetoothAdapter.ACTION_DISCOVERY_FINISHED : 블루투스 검색 종료
        registerReceiver(mBluetoothSearchReceiver, searchFilter);


        //1. 블루투스가 꺼져있으면 활성화
//        if(!mBluetoothAdapter.isEnabled()){
//            mBluetoothAdapter.enable(); //강제 활성화
//        }

        //2. 블루투스가 꺼져있으면 사용자에게 활성화 요청하기
        if(!mBluetoothAdapter.isEnabled()){
            Intent intent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            startActivityForResult(intent, BLUETOOTH_REQUEST_CODE);
        }
        mBluetoothAdapter.startDiscovery();

        listDevice.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {

                BluetoothDevice device = bluetoothDevices.get(position);
                mOnPopupClick(device);

            }
        });
    }




    //블루투스 상태변화 BroadcastReceiver
    BroadcastReceiver mBluetoothStateReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            //BluetoothAdapter.EXTRA_STATE : 블루투스의 현재상태 변화
            int state = intent.getIntExtra(BluetoothAdapter.EXTRA_STATE, -1);

            //블루투스 활성화
            if(state == BluetoothAdapter.STATE_ON){
                txtState.setText("블루투스 활성화");
            }
            //블루투스 활성화 중
            else if(state == BluetoothAdapter.STATE_TURNING_ON){
                txtState.setText("블루투스 활성화 중...");
            }
            //블루투스 비활성화
            else if(state == BluetoothAdapter.STATE_OFF){
                txtState.setText("블루투스 비활성화");
            }
            //블루투스 비활성화 중
            else if(state == BluetoothAdapter.STATE_TURNING_OFF){
                txtState.setText("블루투스 비활성화 중...");
            }
        }
    };

    //블루투스 검색결과 BroadcastReceiver
    BroadcastReceiver mBluetoothSearchReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            switch(action){
                //블루투스 디바이스 검색 종료
                case BluetoothAdapter.ACTION_DISCOVERY_STARTED:
                    dataDevice.clear();
                    bluetoothDevices.clear();
                    Toast.makeText(CubeRegister.this, "블루투스 검색 시작", Toast.LENGTH_SHORT).show();
                    break;
                //블루투스 디바이스 찾음
                case BluetoothDevice.ACTION_FOUND:
                    //검색한 블루투스 디바이스의 객체를 구한다
                    BluetoothDevice device = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
                    //데이터 저장
                    Map map = new HashMap();
                    map.put("name", device.getName()); //device.getName() : 블루투스 디바이스의 이름
                    map.put("address", device.getAddress()); //device.getAddress() : 블루투스 디바이스의 MAC 주소
                    int i;
                    for(i=0;i<dataDevice.size();i++){
                        if(dataDevice.get(i).get("address").equals(map.get("address"))){
                            break;
                        }
                    }
                    if(i==dataDevice.size()){
                        dataDevice.add(map);
                        bluetoothDevices.add(device);
                        adapterDevice.notifyDataSetChanged();
                    }
                    //리스트 목록갱신

                    break;
                //블루투스 디바이스 검색 종료
                case BluetoothAdapter.ACTION_DISCOVERY_FINISHED:
                    Toast.makeText(CubeRegister.this, "블루투스 검색 종료", Toast.LENGTH_SHORT).show();
                    btnSearch.setEnabled(true);
                    break;
            }
        }
    };


    //블루투스 검색 버튼 클릭
    public void mOnBluetoothSearch(View v){
        //검색버튼 비활성화
        btnSearch.setEnabled(false);
        //mBluetoothAdapter.isDiscovering() : 블루투스 검색중인지 여부 확인
        //mBluetoothAdapter.cancelDiscovery() : 블루투스 검색 취소
        if(mBluetoothAdapter.isDiscovering()){
            mBluetoothAdapter.cancelDiscovery();
        }
        //mBluetoothAdapter.startDiscovery() : 블루투스 검색 시작
        mBluetoothAdapter.startDiscovery();
    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        switch(requestCode){
            case BLUETOOTH_REQUEST_CODE:
                //블루투스 활성화 승인
                if(resultCode == Activity.RESULT_OK){

                }
                //블루투스 활성화 거절
                else{
                    Toast.makeText(this, "블루투스를 활성화해야 합니다.", Toast.LENGTH_SHORT).show();
                    finish();
                    return;
                }
                break;
        }
    }

    @Override
    protected void onDestroy() {
        unregisterReceiver(mBluetoothStateReceiver);
        unregisterReceiver(mBluetoothSearchReceiver);
        super.onDestroy();
    }

    public void mOnPopupClick(BluetoothDevice device){
        //데이터 담아서 팝업(액티비티) 호출
        popUpIntent = new Intent(this, PopCubeRegist.class);
        popUpIntent.putExtra("bluetoothDevice", device);
        startActivity(popUpIntent);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String permissions[], int[] grantResults) {
        switch (requestCode) {
            case BLUETOOTH_REQUEST_CODE: {
                // If request is cancelled, the result arrays are empty.
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {

                    // permission was granted, yay! Do the
                    // contacts-related task you need to do.

                } else {
                    Toast.makeText(CubeRegister.this, "권한 요청에 동의 해주셔야 큐브 기능 사용이 가능합니다. :)", Toast.LENGTH_SHORT).show();
                    finish();
                    // permission denied, boo! Disable the
                    // functionality that depends on this permission.
                }
                return;
            }

            // other 'case' lines to check for other
            // permissions this app might request
        }
    }

//    public class joinTask extends AsyncTask<String,Object,Integer>{
//
//        @Override
//        protected Integer doInBackground(String... params) {
//            try {
//             /* URL 설정하고 접속 */
//                URL url = new URL("http://fungdu0624.phps.kr/biocube/join.php");
//                HttpURLConnection http = (HttpURLConnection) url.openConnection();
//
//            /* 전송모드 설정 */
//                http.setDefaultUseCaches(false);
//                http.setDoInput(true);  //서버에서 읽기 모드로 지정
//                http.setDoOutput(true);    //서버에서 쓰기 모드로 지정
//                http.setRequestMethod("POST");
//                http.setRequestProperty("content-type", "application/x-www-form-urlencoded");   //서버에게 웹에서 <Form>으로 값이 넘어온 것과 같은 방식으로 처리하라는 걸 알려준다
//
//            /* 서버로 값 전송 */
//                StringBuffer buffer = new StringBuffer();
//                buffer.append("userID").append("=").append(params[0].toString()).append("&");
//                buffer.append("userPW").append("=").append(params[1].toString()).append("&");
//                buffer.append("nickname").append("=").append(params[2].toString()).append("&");
//                buffer.append("authority").append("=").append(params[3].toString()).append("&");
//                buffer.append("job").append("=").append(params[4].toString()).append("&");
//                buffer.append("phone").append("=").append(params[5].toString());
//
//                OutputStreamWriter outStream = new OutputStreamWriter(http.getOutputStream(), "EUC-KR");
//                PrintWriter writer = new PrintWriter(outStream);
//                writer.write(buffer.toString());
//                writer.flush();
//                writer.close();
//
//            /* 서버에서 전송 받기 */
//                InputStream inStream = http.getInputStream();
//                BufferedReader reader = new BufferedReader(new InputStreamReader(inStream, "UTF-8"));
//                String str = reader.readLine();
//
//                if(str.equals("1")){//회원가입 성공시
//                    return 0;
//                }
//                else{//가입 실패시
//                    return -1;
//                }
//            } catch(MalformedURLException e) {
//                e.printStackTrace();
//            } catch(IOException e) {
//                e.printStackTrace();
//            }
//
//            return -1;
//        }
//
//        @Override
//        public void onPostExecute(Integer result) {
//            super.onPostExecute(result);
//            // Todo: doInBackground() 메소드 작업 끝난 후 처리해야할 작업..
//            Intent intent;
//            if(result == 0) {   //회원가입 성공
//                intent = new Intent(JoinActivity.this, LoginActivity.class);
//                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK);
//                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
//                startActivity(intent);
//            } else {    //실패
//                Toast.makeText(getApplicationContext(), "실패하셨어요", Toast.LENGTH_LONG).show();
//            }
//        }
//    }
}