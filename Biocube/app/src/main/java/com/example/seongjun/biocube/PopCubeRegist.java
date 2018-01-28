package com.example.seongjun.biocube;

import android.bluetooth.BluetoothDevice;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
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

public class PopCubeRegist extends AppCompatActivity {

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

        //UI 객체생성
        edit_cubeName = (EditText) findViewById(R.id.edit_cubeName);
        spinner_plantName = (Spinner) findViewById(R.id.spinner_plantName);
        Intent intent = getIntent();
        device = intent.getExtras().getParcelable("bluetoothDevice");
        MAC_ADDRESS = device.getAddress();
        new PopCubeRegist.settingSpinner().execute();
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
        try{
            cubeName = URLEncoder.encode(cubeName,"UTF-8");
        } catch(Exception e) {
            e.printStackTrace();
        }
        try {
            cubeName = new String(cubeName.getBytes("UTF-8"));
        } catch (UnsupportedEncodingException e) {

        }
        plantName = spinner_plantName.getSelectedItem().toString();
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
                URL url = new URL("http://fungdu0624.phps.kr/biocube/manuals.php");
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

    String dbUpResult;
    private TokenDBHelper helper = new TokenDBHelper(this);
    public class cubeUpTask extends AsyncTask<String,Object,Integer>{
        @Override
        protected Integer doInBackground(String... params) {
            try {
                SQLiteDatabase db = helper.getReadableDatabase();
                Cursor cursor;  //여러 개의 데이터가 있을 때 순서대로 접근할 수 있는 포인터
                //select 구문을 실행하고 결과를 cursor에서 접근하도록 설정
                cursor = db.rawQuery("SELECT * FROM TOKEN", null);
                int count = cursor.getCount();
                cursor.moveToFirst();
                String jwt = cursor.getString(0);

                URL url = new URL("http://fungdu0624.phps.kr/biocube/getuserid.php");
                HttpURLConnection http = (HttpURLConnection) url.openConnection();

                http.setDefaultUseCaches(false);
                http.setDoInput(true);  //서버에서 읽기 모드로 지정
                http.setDoOutput(true);    //서버에서 쓰기 모드로 지정
                http.setRequestMethod("POST");
                http.setRequestProperty("content-type", "application/x-www-form-urlencoded");   //서버에게 웹에서 <Form>으로 값이 넘어온 것과 같은 방식으로 처리하라는 걸 알려준다

                StringBuffer buffer = new StringBuffer();
                buffer.append("jwt").append("=").append(jwt);
                OutputStreamWriter outStream = new OutputStreamWriter(http.getOutputStream(), "EUC-KR");
                PrintWriter writer = new PrintWriter(outStream);
                writer.write(buffer.toString());
                writer.flush();
                writer.close();

                /* 서버에서 전송 받기 */
                InputStream inStream = http.getInputStream();
                BufferedReader reader = new BufferedReader(new InputStreamReader(inStream, "UTF-8"));
                String str = reader.readLine();
                user_id = str;
                inStream.close();
                http.disconnect();

                url = new URL("http://fungdu0624.phps.kr/biocube/registcube.php");
                http = (HttpURLConnection) url.openConnection();

                http.setDefaultUseCaches(false);
                http.setDoInput(true);  //서버에서 읽기 모드로 지정
                http.setDoOutput(true);    //서버에서 쓰기 모드로 지정
                http.setRequestMethod("POST");
                http.setRequestProperty("content-type", "application/x-www-form-urlencoded");   //서버에게 웹에서 <Form>으로 값이 넘어온 것과 같은 방식으로 처리하라는 걸 알려준다

            /* 서버로 값 전송 */
                StringBuffer buffer2 = new StringBuffer();

                buffer2.append("user_id").append("=").append(str).append("&");
                buffer2.append("cubename").append("=").append(params[0].toString()).append("&");
                buffer2.append("device").append("=").append(params[1].toString()).append("&");
                buffer2.append("kindOf").append("=").append(params[2].toString());

                OutputStreamWriter outStream2 = new OutputStreamWriter(http.getOutputStream(), "EUC-KR");
                PrintWriter writer2 = new PrintWriter(outStream2);
                writer2.write(buffer2.toString());
                writer2.flush();
                writer2.close();

                InputStream inStream2 = http.getInputStream();
                BufferedReader reader2 = new BufferedReader(new InputStreamReader(inStream2, "UTF-8"));
                dbUpResult = reader2.readLine();

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
