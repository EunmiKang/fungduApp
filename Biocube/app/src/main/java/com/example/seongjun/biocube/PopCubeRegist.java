package com.example.seongjun.biocube;

import android.app.Activity;
import android.bluetooth.BluetoothDevice;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.View;
import android.view.Window;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.Toast;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.io.Serializable;
import java.io.UnsupportedEncodingException;
import java.lang.reflect.Method;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutionException;

/**
 * Created by Seongjun on 2017. 11. 23..
 */

public class PopCubeRegist extends Activity {

    Spinner spinner_plantName;
    Context ctx = this;
    String MAC_ADDRESS;
    String cubeName;
    String plantName;
    EditText edit_cubeName;
    BluetoothDevice device;
    String user_id;
    PagerAdapter mPagerAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.popup_regist_cube);

        try {
            user_id = new GetId().execute(this).get();
        } catch (InterruptedException e) {
            e.printStackTrace();
        } catch (ExecutionException e) {
            e.printStackTrace();
        }

        //UI 객체생성
        edit_cubeName = (EditText) findViewById(R.id.edit_cubeName);
        spinner_plantName = (Spinner) findViewById(R.id.spinner_plantName);
        Intent intent = getIntent();
        device = intent.getExtras().getParcelable("bluetoothDevice");
        MAC_ADDRESS = device.getAddress();
        String[] plantNameArray = null;
        if(user_id.equals("admin")) {
            plantNameArray = ((ManualFragment)((AdminMainActivity)AdminMainActivity.context).mAdminPagerAdapter.getItem(0)).adapter.plantNameArray;
        } else {
            plantNameArray = ((ManualFragment)((UserMainActivity)AdminMainActivity.context).mUserPagerAdapter.getItem(0)).adapter.plantNameArray;
        }
        ArrayAdapter<String> dataAdapter = new ArrayAdapter<String>(ctx, android.R.layout.simple_spinner_item, plantNameArray);
        dataAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinner_plantName.setAdapter(dataAdapter);
        //new PopCubeRegist.settingSpinner().execute();
//        mPagerAdapter = (PagerAdapter)intent.getSerializableExtra("adapter");


    }
//    @Override
//    protected void onActivityResult(int requestCode, int resultCode, Intent data){
//        switch (requestCode){
//            case 1:
//                finish();
//                break;
//        }
//    }
    public void mOnClose(View v){
        cubeName = edit_cubeName.getText().toString();
        plantName = spinner_plantName.getSelectedItem().toString();
        try{
            cubeName = URLEncoder.encode(cubeName,"UTF-8");
            plantName = URLEncoder.encode(plantName,"UTF-8");
        } catch(Exception e) {
            e.printStackTrace();
        }
        try {
            cubeName = new String(cubeName.getBytes("UTF-8"));
            plantName = new String(plantName.getBytes("UTF-8"));
        } catch (UnsupportedEncodingException e) {

        }
        new PopCubeRegist.cubeUpTask().execute(cubeName,MAC_ADDRESS,plantName);
        //액티비티(팝업) 닫기
        finish();
    }

    public class settingSpinner extends AsyncTask<Object,Object,Integer> {

        List<String> plantList= new ArrayList<String>();
        // 실제 params 부분에는 execute 함수에서 넣은 인자 값이 들어 있다.
        @Override
        public Integer doInBackground(Object... params) {
            try {
             /* URL 설정하고 접속 */
                URL url = new URL("http://fungdu0624.phps.kr/biocube/returnManualList.php");
                HttpURLConnection http = (HttpURLConnection) url.openConnection();

            /* 전송모드 설정 */
                http.setDefaultUseCaches(false);
                http.setDoInput(true);  //서버에서 읽기 모드로 지정
                http.setDoOutput(true);    //서버에서 쓰기 모드로 지정
                http.setRequestMethod("POST");
                http.setRequestProperty("content-type", "application/x-www-form-urlencoded");   //서버에게 웹에서 <Form>으로 값이 넘어온 것과 같은 방식으로 처리하라는 걸 알려준다

            /* 서버에서 전송 받기 */
                InputStream inStream = http.getInputStream();
                BufferedReader reader = new BufferedReader(new InputStreamReader(inStream, "UTF-8"));
                String str = reader.readLine();
                String[] token = str.split(",");
                String[] plant = new String[Integer.parseInt(token[0])];
                for(int i = 1; i <token.length; i++){
                    String[] tmp = token[i].split(" ");
                    plant[i-1] = tmp[0];
                }

                /* 매뉴얼 수 setting */
                for(int i=0; i<plant.length; i++){
                    plantList.add(plant[i]);
                }
                inStream.close();
                http.disconnect();

            } catch(MalformedURLException e) {
                e.printStackTrace();
            } catch(IOException e) {
                e.printStackTrace();
            }
            //publishProgress(params);    // 중간 중간에 진행 상태 UI 를 업데이트 하기 위해 사용..
            return -1;
        }

        @Override
        public void onPostExecute(Integer result) {
            super.onPostExecute(result);

            ArrayAdapter<String> dataAdapter = new ArrayAdapter<String>(ctx, android.R.layout.simple_spinner_item, plantList);
            dataAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
            spinner_plantName.setAdapter(dataAdapter);
            // Todo: doInBackground() 메소드 작업 끝난 후 처리해야할 작업..

        }

        @Override
        protected void onProgressUpdate(Object[] values) {
            super.onProgressUpdate(values);
            // Todo: publishProgress() 메소드 호출시 처리할 작업..
        }
    }

    public class cubeUpTask extends AsyncTask<String,Object,Integer>{
        String dbUpResult;

        @Override
        protected Integer doInBackground(String... params) {
            try {
                URL url = new URL("http://fungdu0624.phps.kr/biocube/registcube.php");
                HttpURLConnection http = (HttpURLConnection) url.openConnection();

                http.setDefaultUseCaches(false);
                http.setDoInput(true);  //서버에서 읽기 모드로 지정
                http.setDoOutput(true);    //서버에서 쓰기 모드로 지정
                http.setRequestMethod("POST");
                http.setRequestProperty("content-type", "application/x-www-form-urlencoded");   //서버에게 웹에서 <Form>으로 값이 넘어온 것과 같은 방식으로 처리하라는 걸 알려준다

            /* 서버로 값 전송 */
                StringBuffer buffer = new StringBuffer();

                buffer.append("user_id").append("=").append(user_id).append("&");
                buffer.append("cubename").append("=").append(params[0].toString()).append("&");
                buffer.append("device").append("=").append(params[1].toString()).append("&");
                buffer.append("kindOf").append("=").append(params[2].toString());

                OutputStreamWriter outStream = new OutputStreamWriter(http.getOutputStream(), "EUC-KR");
                PrintWriter writer = new PrintWriter(outStream);
                writer.write(buffer.toString());
                writer.flush();
                writer.close();

                InputStream inStream = http.getInputStream();
                BufferedReader reader = new BufferedReader(new InputStreamReader(inStream, "UTF-8"));
                dbUpResult = reader.readLine();

                inStream.close();
                http.disconnect();

            } catch(MalformedURLException e) {
                e.printStackTrace();
            } catch(IOException e) {
                e.printStackTrace();
            }

            return null;
        }

        @Override
        public void onPostExecute(Integer result) {
            super.onPostExecute(result);
            // Todo: doInBackground() 메소드 작업 끝난 후 처리해야할 작업..
            try {
                if(dbUpResult.equals("1")){
                    Toast.makeText(getApplicationContext(), "성공" , Toast.LENGTH_SHORT).show();
                    if(user_id.equals("admin")){
                        ((CubeFragment)((AdminMainActivity)AdminMainActivity.context).mAdminPagerAdapter.getItem(3)).setSpinner();
                    }
                    else{
                        ((CubeFragment)((UserMainActivity)UserMainActivity.context).mUserPagerAdapter.getItem(3)).setSpinner();
                        ((WriteDiaryFragment)((UserMainActivity)UserMainActivity.context).mUserPagerAdapter.getItem(2)).setSpinner();
                    }
                }
                else{
                    Toast.makeText(getApplicationContext(), "실패" , Toast.LENGTH_SHORT).show();
                }
                ((CubeRegister)CubeRegister.mcontext).mBluetooth.mSocket.close();
                //선택한 디바이스 페어링 요청
                Method method = device.getClass().getMethod("createBond", (Class[]) null);
                method.invoke(device, (Object[]) null);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

}
