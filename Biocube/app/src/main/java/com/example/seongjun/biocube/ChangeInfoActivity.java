package com.example.seongjun.biocube;

import android.content.ContentValues;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by Seongjun on 2017. 11. 28..
 */

public class ChangeInfoActivity extends AppCompatActivity {

    TextView textNick;
    EditText changeNick;
    EditText changePW;
    EditText addFilter;
    EditText changeJob;
    EditText changePhone;
    int authority;
    String nickname;
    String setData = "";
    String id;
    String changenickname;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_change_info);

        id = getIntent().getStringExtra("id");
        textNick = (TextView) findViewById(R.id.text_nickname);
        changeNick = (EditText) findViewById(R.id.edit_changeNickname);
        changePW = (EditText) findViewById(R.id.edit_changePW);
        addFilter = (EditText) findViewById(R.id.edit_addFilter);
        changeJob = (EditText) findViewById(R.id.edit_changeJob);
        changePhone = (EditText) findViewById(R.id.edit_changePhone);
        findViewById(R.id.btn_change).setOnClickListener(changeInfoClickListener);

        new getAuthority().execute();

    }

    Button.OnClickListener changeInfoClickListener= new Button.OnClickListener(){

        @Override
        public void onClick(View v) {
            settingData();
            new changeInfoTask().execute();
        }
    };

    public void settingData(){
        if(!changeNick.getText().toString().equals("")){
            setData = "nickname = '" +changeNick.getText().toString() +"',";
            changenickname = changeNick.getText().toString();
        }
        if(!changePW.getText().toString().equals("")){
            setData = setData + "pw = '"+changePW.getText().toString() + "',";
        }
        if(!changeJob.getText().toString().equals("")){
            setData = setData + "job = '" + changeJob.getText().toString() + "',";
        }
        if(!changePhone.getText().toString().equals("")){
            setData = setData + "phone = '" + changePhone.getText().toString() + "',";
        }
        if(setData.length()>=1) {
            setData = setData.substring(0, setData.length() - 1);
        }
    }
    public class changeInfoTask extends AsyncTask<Object,Object,Integer>{

        @Override
        protected Integer doInBackground(Object... params) {

            try{
                URL url = new URL("http://fungdu0624.phps.kr/biocube/changeinfo.php");
                HttpURLConnection http = (HttpURLConnection) url.openConnection();

                http.setDefaultUseCaches(false);
                http.setDoInput(true);  //서버에서 읽기 모드로 지정
                http.setDoOutput(true);    //서버에서 쓰기 모드로 지정
                http.setRequestMethod("POST");
                http.setRequestProperty("content-type", "application/x-www-form-urlencoded");   //서버에게 웹에서 <Form>으로 값이 넘어온 것과 같은 방식으로 처리하라는 걸 알려준다


                StringBuffer buffer = new StringBuffer();
                buffer.append("userID").append("=").append(id).append("&");
                buffer.append("nickname").append("=").append(changenickname).append("&");
                buffer.append("setData").append("=").append(setData);

                OutputStreamWriter outStream = new OutputStreamWriter(http.getOutputStream(), "EUC-KR");
                PrintWriter writer = new PrintWriter(outStream);
                writer.write(buffer.toString());
                writer.flush();
                writer.close();

                InputStream inStream = http.getInputStream();
                BufferedReader reader = new BufferedReader(new InputStreamReader(inStream, "UTF-8"));
                String str = reader.readLine();

                if(str.equals("1")){
                    return 1;
                }
                else{
                    return -1;
                }
            }catch(MalformedURLException e){
                e.printStackTrace();
            }catch(IOException e){
                e.printStackTrace();
            }

            return null;
        }

        @Override
        public void onPostExecute(Integer result) {
            super.onPostExecute(result);
            // Todo: doInBackground() 메소드 작업 끝난 후 처리해야할 작업..

            if(result == 1 ){
                Toast.makeText(getApplicationContext(), "변경에 성공하였습니다.", Toast.LENGTH_SHORT).show();
                Intent intent;
                intent = new Intent(ChangeInfoActivity.this, ExpertMainActivity.class);
                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK);
                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                startActivity(intent);

            }
            else{
                Toast.makeText(getApplicationContext(), "변경에 실패하였습니다.", Toast.LENGTH_SHORT).show();
            }
        }

    }

    private TokenDBHelper helper = new TokenDBHelper(this);
    public class getAuthority extends AsyncTask<Object,Object,Integer> {
        // 실제 params 부분에는 execute 함수에서 넣은 인자 값이 들어 있다.
        @Override
        public Integer doInBackground(Object... params) {
            try {
                SQLiteDatabase db = helper.getReadableDatabase();
                Cursor cursor;  //여러 개의 데이터가 있을 때 순서대로 접근할 수 있는 포인터
                //select 구문을 실행하고 결과를 cursor에서 접근하도록 설정
                cursor = db.rawQuery("SELECT * FROM TOKEN", null);
                int count = cursor.getCount();
                cursor.moveToFirst();
                String jwt = cursor.getString(0);

                URL url = new URL("http://fungdu0624.phps.kr/biocube/getuserinfo.php");
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
                String[] userinfo = str.split(",");

                inStream.close();
                http.disconnect();

                nickname = userinfo[0];
                if (userinfo[1].equals("1")) {
                    authority = 1;
                } else if (userinfo[1].equals("2")) {
                    authority = 2;
                } else {
                    authority = 0;
                }

            } catch (MalformedURLException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            }
            //publishProgress(params);    // 중간 중간에 진행 상태 UI 를 업데이트 하기 위해 사용..
            return authority;
        }

        @Override
        public void onPostExecute(Integer result) {
            super.onPostExecute(result);
            // Todo: doInBackground() 메소드 작업 끝난 후 처리해야할 작업..

            textNick.setText(nickname);
            if (result == 2) {
                changePhone.setVisibility(View.VISIBLE);
                changeJob.setVisibility(View.VISIBLE);
            }
        }
    }


}
