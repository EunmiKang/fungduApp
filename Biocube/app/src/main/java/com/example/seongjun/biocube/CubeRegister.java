package com.example.seongjun.biocube;

import android.Manifest;
import android.app.ProgressDialog;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.ListView;
import android.widget.SimpleAdapter;
import android.widget.TextView;
import android.widget.Toast;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ExecutionException;

/**
 * Created by Seongjun on 2017. 11. 7..
 */

public class CubeRegister extends AppCompatActivity {
    Intent popUpIntent;

    //블루투스 요청 액티비티 코드
    final static int BLUETOOTH_REQUEST_CODE = 100;
    //UI
    Button btnSearch;
    ListView listDevice;

    //Adapter
    SimpleAdapter adapterDevice;
    BluetoothAdapter mBluetoothAdapter;
    //list - Device 목록 저장
    List<Map<String,String>> dataDevice;//블루투스 검색 결과를 key,value 값으로 저장할 list.
    List<BluetoothDevice> bluetoothDevices;//블루투스 검색 결과를 담을 list.


    static final int REQUEST_ENABLE_BT = 10;
    int mPariedDeviceCount = 0;//페어링된 디바이스 수.
    Set<BluetoothDevice> mDevices;
    public static Context mcontext;
    String id;
    ProgressDialog dialog;

    Bluetooth mBluetooth = new Bluetooth();
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_resgist_cube);
        mcontext = this;

        popUpIntent = new Intent(this, PopCubeRegist.class);

        //UI
        btnSearch = (Button) findViewById(R.id.btnSearch);
        listDevice = (ListView) findViewById(R.id.listDevice);

        dataDevice = new ArrayList<>();//리스트 초기화
        bluetoothDevices = new ArrayList<>();
        adapterDevice = new
                SimpleAdapter(this, dataDevice, android.R.layout.simple_list_item_2, new String[]{"name", "address"}, new int[]{android.R.id.text1, android.R.id.text2});
        listDevice.setAdapter(adapterDevice);

        dialog = new ProgressDialog(CubeRegister.this);

        // 블루투스 권한 요청(마쉬멜로우 버전 이상)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (ContextCompat.checkSelfPermission(CubeRegister.this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                if (ActivityCompat.shouldShowRequestPermissionRationale(CubeRegister.this, Manifest.permission.ACCESS_FINE_LOCATION)) {

                } else {
                    ActivityCompat.requestPermissions(CubeRegister.this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, BLUETOOTH_REQUEST_CODE);
                }
            }
            else {

            }
        }
        try {//로그인된 아이디 불러옴
            id = new GetId().execute(getApplicationContext()).get();
        } catch (InterruptedException e) {
            e.printStackTrace();
        } catch (ExecutionException e) {
            e.printStackTrace();
        }
        if(id.equals("admin")){//admin일때,
            if(((((AdminMainActivity)AdminMainActivity.context)).mBluetooth.mSocket != null)){
                try {
                    ((AdminMainActivity)AdminMainActivity.context).mBluetooth.mSocket.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
        else{//user가 아닐때
            if(((((UserMainActivity)UserMainActivity.context)).mBluetooth.mSocket != null)){
                try {
                    ((UserMainActivity)UserMainActivity.context).mBluetooth.mSocket.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }


        //리시버 등록
        IntentFilter searchFilter = new IntentFilter();
        searchFilter.addAction(BluetoothAdapter.ACTION_DISCOVERY_STARTED); //BluetoothAdapter.ACTION_DISCOVERY_STARTED : 블루투스 검색 시작
        searchFilter.addAction(BluetoothDevice.ACTION_FOUND); //BluetoothDevice.ACTION_FOUND : 블루투스 디바이스 찾음
        searchFilter.addAction(BluetoothAdapter.ACTION_DISCOVERY_FINISHED); //BluetoothAdapter.ACTION_DISCOVERY_FINISHED : 블루투스 검색 종료
        registerReceiver(mBluetoothSearchReceiver, searchFilter);

        IntentFilter stateFilter = new IntentFilter();
        stateFilter.addAction(BluetoothDevice.ACTION_ACL_CONNECTED);//블루투스 연결됨
        stateFilter.addAction(BluetoothDevice.ACTION_ACL_DISCONNECTED);//블루투스 끊김
        registerReceiver(mBluetooth.mBluetoothStateReceiver, stateFilter);

        LayoutInflater inflater = (LayoutInflater) getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View view = inflater.inflate(R.layout.fragment_cube, null);

        //블루투스 지원 유무 확인
        checkBluetooth(this);

        listDevice.setOnItemClickListener(new AdapterView.OnItemClickListener() {//검색된 블루투스 목록에서 등록하기 위해 선택했을 때,
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                mBluetoothAdapter.cancelDiscovery();
                new ConnectTask().execute(position);
            }
        });

        btnSearch.setOnClickListener(new View.OnClickListener() {//검색버튼 누를시
            @Override
            public void onClick(View v) {
                mOnBluetoothSearch();//주변 블루투스 검색
                 }
        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {//어떤 요청인지 구분지어 사용.
        switch(requestCode) {
            case REQUEST_ENABLE_BT:
                if(resultCode == RESULT_OK) { // 블루투스 활성화 상태
//                    selectDevice();
                }
                else if(resultCode == RESULT_CANCELED) { // 블루투스 비활성화 상태 (종료)
                    Toast.makeText(getApplicationContext(), "블루투스를 사용할 수 없어 프로그램을 종료합니다", Toast.LENGTH_LONG).show();
                    finish();
                }
                break;
        }
        super.onActivityResult(requestCode, resultCode, data);
    }


    public void mOnPopupClick(BluetoothDevice device){//등록을 위한 팝업창을 띄우는 메소드.
        //데이터 담아서 팝업(액티비티) 호출
        popUpIntent = new Intent(this, PopCheckCube.class);
        popUpIntent.putExtra("bluetoothDevice", device);//블루투스 디바이스 정보를 팝업창에 같이 넘겨줌.
        startActivity(popUpIntent);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String permissions[], int[] grantResults) {//블루투스 권한 요청
        switch (requestCode) {
            case BLUETOOTH_REQUEST_CODE: {
                // If request is cancelled, the result arrays are empty.
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                } else {
                    Toast.makeText(CubeRegister.this, "권한 요청에 동의 해주셔야 큐브 기능 사용이 가능합니다. :)", Toast.LENGTH_SHORT).show();
                    finish();
                }
                return;
            }
        }
    }

    void checkBluetooth(Context context) {
        mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        if(mBluetoothAdapter == null ) {  // 블루투스 미지원
            Toast.makeText(context, "기기가 블루투스를 지원하지 않습니다.", Toast.LENGTH_LONG).show();
            finish();  // 앱종료
        }
        else { // 블루투스 지원
            if(!mBluetoothAdapter.isEnabled()) { // 블루투스 지원하며 비활성 상태인 경우.
                Toast.makeText(context, "현재 블루투스가 비활성 상태입니다.", Toast.LENGTH_LONG).show();
                Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
                startActivityForResult(enableBtIntent, REQUEST_ENABLE_BT);
            }
            else { // 블루투스 지원하며 활성 상태인 경우.
                mDevices = mBluetoothAdapter.getBondedDevices();//페어링된 블루투스 목록을 가져옴
                mPariedDeviceCount = mDevices.size();
            }
        }
    }

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

    @Override
    protected void onDestroy() {//액티비티 종료시
        unregisterReceiver(mBluetoothSearchReceiver);//리시버 해제.
        unregisterReceiver(mBluetooth.mBluetoothStateReceiver);
        try{
            mBluetooth.mSocket.close();
            if(id.equals("admin")) {
                ((CubeFragment)((AdminMainActivity)AdminMainActivity.context).mAdminPagerAdapter.getItem(0)).bluetoothFlag = false;
            } else {
                ((CubeFragment)((UserMainActivity)UserMainActivity.context).mUserPagerAdapter.getItem(0)).bluetoothFlag = false;
            }
        }catch(Exception e){}
        super.onDestroy();
    }

    //블루투스 검색 버튼 클릭
    public void mOnBluetoothSearch(){
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

    class ConnectTask extends AsyncTask<Integer, Void, String> {//디바이스를 찾아 연결시키는 쓰레드.
        @Override
        protected String doInBackground(Integer... params) {
            BluetoothDevice device = bluetoothDevices.get(params[0]);
            if(mBluetooth.connectToSelectedDevice(device.getAddress(), mDevices, device)){//선택한 디바이스를 연결 성공했을 때
                mOnPopupClick(device);//등록을 위한 팝업창이 나타남.
                //데이터 보내는부분
                if(!mBluetooth.sendData("check")){//디바이스에 시그널을 보내는데 실패한 경우
                    Toast.makeText(getApplicationContext(), "데이터 전송중 오류가 발생", Toast.LENGTH_LONG).show();
//                    finish();  // App 종료
                }
            }
            else{//블루투스 연결 실패시,
//                    Toast.makeText(getApplicationContext(), "블루투스 연결 중 오류가 발생했습니다.", Toast.LENGTH_LONG).show();
            }
            return null;
        }
        @Override
        protected void onPostExecute(String result){
            dialog.dismiss();
        }

        @Override
        protected void onPreExecute() {
            dialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
            dialog.setMessage("연결중입니다...");

            // show dialog
            dialog.show();
            super.onPreExecute();
        }
    }
}