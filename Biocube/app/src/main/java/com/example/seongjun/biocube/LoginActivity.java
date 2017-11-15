package com.example.seongjun.biocube;


import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.content.Intent;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
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

/**
 * Created by Seongjun on 2017. 11. 1..
 */

public class LoginActivity extends AppCompatActivity {

    private int status = -1;

    //sqlite를 사용하기 위한 클래스
    class TokenDBHelper extends SQLiteOpenHelper {
        public TokenDBHelper(Context context) {
            super(context, "tokenDB.db", null, 1);
        }

        @Override
        public void onCreate(SQLiteDatabase db) {
            //테이블 생성
            db.execSQL("CREATE TABLE TOKEN(token varchar(50) primary key);");
        }

        @Override
        public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
            //테이블을 지우는 구문을 수행
            db.execSQL("DROP TABLE if exists TOKEN;");
            //테이블 다시 생성
            onCreate(db);
        }
    }

    private TokenDBHelper helper = new TokenDBHelper(this);

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        new checkDBTask().execute(helper);
    }

    /**
     * 내부 DB의 TOKEN table에 토큰이 있나 없나 확인
     * 있으면 유효한지 확인 후
     * 유효하면 자동로그인 되고 메인페이지 보이고,
     * 유효하지 않거나 없으면 로그인페이지 보임.
     */
    protected void checkDB(TokenDBHelper helper) {
        SQLiteDatabase db = helper.getReadableDatabase();

        //select 구문의 결과를 접근할 수 있는 포인터 변수
        Cursor cursor;  //여러 개의 데이터가 있을 때 순서대로 접근할 수 있는 포인터
        //select 구문을 실행하고 결과를 cursor에서 접근하도록 설정
        cursor = db.rawQuery("SELECT * FROM TOKEN", null);
        int count = cursor.getCount();
        cursor.moveToFirst();
        String test = cursor.getColumnName(0);
        int test2 = cursor.getColumnCount();

        if (count >= 1) {   //토큰 있음
            String jwt = cursor.getString(0);
            try {
                /* URL 설정하고 접속 */
                URL url = new URL("http://fungdu0624.phps.kr/biocube/tokenLogin.php");
                HttpURLConnection http = (HttpURLConnection) url.openConnection();

                /* 전송모드 설정 */
                http.setDefaultUseCaches(false);
                http.setDoInput(true);  //서버에서 읽기 모드로 지정
                http.setDoOutput(true);    //서버에서 쓰기 모드로 지정
                http.setRequestMethod("POST");
                http.setRequestProperty("content-type", "application/x-www-form-urlencoded");   //서버에게 웹에서 <Form>으로 값이 넘어온 것과 같은 방식으로 처리하라는 걸 알려준다

                /* 서버로 값 전송 */
                StringBuffer buffer = new StringBuffer();
                buffer.append("jwt").append("=").append(jwt);
                OutputStreamWriter outStream = new OutputStreamWriter(http.getOutputStream(), "EUC-KR");
                PrintWriter writer = new PrintWriter(outStream);
                writer.write(buffer.toString());
                writer.flush();

                /* 서버에서 전송 받기 */
                InputStreamReader tmp = new InputStreamReader(http.getInputStream(), "EUC-KR");
                BufferedReader reader = new BufferedReader(tmp);
                String str = reader.readLine();
                //결과 0:관리자, 1:일반사용자, 2:전문가 -> 토큰 유효함! 로그인 진행
                if(str.equals("0")) {   //관리자
                    status = 0;
                } else if(str.equals("1")) {    //일반 사용자
                    status = 1;
                } else if(str.equals("2")) {    //전문가
                    status = 2;
                } else {    //토큰 만료됨 -> 내부 DB에서 토큰 삭제
                    db = helper.getWritableDatabase();
                    db.delete("TOKEN", "token = '" + jwt + "'", null);
                    status = -2;
                }
            } catch (MalformedURLException e) {

            } catch (IOException e) {

            }
        } else {  //토큰 없음
            status = -1;
        }

        Intent intent;
        // 내부DB 접근 - 토큰 있나 없나 확인
        if(status < 0) {
            setContentView(R.layout.activity_login);
            findViewById(R.id.loginBtn).setOnClickListener(loginClickListener);
            findViewById(R.id.joinBtn).setOnClickListener(joinClickListener);
            if(status == -1) {
                Toast.makeText(getApplication(), "로그인하세욤.", Toast.LENGTH_SHORT).show();
            }
            else {
                Toast.makeText(getApplication(), "로그아웃 되었습니다.", Toast.LENGTH_SHORT).show();
            }
        }
        else {
            if(status == 0) {
                intent = new Intent(LoginActivity.this, AdminMainActivity.class);
            }
            else if(status == 1) {
                intent = new Intent(LoginActivity.this, UserMainActivity.class);
            } else {    //status == 2
                intent = new Intent(LoginActivity.this, ExpertMainActivity.class);
            }
            startActivity(intent);
        }
    }

    /* login 버튼 클릭했을 때 */
    Button.OnClickListener loginClickListener = new Button.OnClickListener() {
        public void onClick(View v) {
            new loginTask().execute(helper);
        }
    };

    private void loginProgress(TokenDBHelper helper) {
        try {
             /* URL 설정하고 접속 */
            URL url = new URL("http://fungdu0624.phps.kr/biocube/login.php");
            HttpURLConnection http = (HttpURLConnection) url.openConnection();

            /* 전송모드 설정 */
            http.setDefaultUseCaches(false);
            http.setDoInput(true);  //서버에서 읽기 모드로 지정
            http.setDoOutput(true);    //서버에서 쓰기 모드로 지정
            http.setRequestMethod("POST");
            http.setRequestProperty("content-type", "application/x-www-form-urlencoded");   //서버에게 웹에서 <Form>으로 값이 넘어온 것과 같은 방식으로 처리하라는 걸 알려준다

            /* 서버로 값 전송 */
            EditText idText = (EditText) findViewById(R.id.idText);
            EditText pwText = (EditText) findViewById(R.id.pwText);
            String id = idText.getText().toString();
            String pw = pwText.getText().toString();
            StringBuffer buffer = new StringBuffer();
            buffer.append("userID").append("=").append(id).append("&");
            buffer.append("userPW").append("=").append(pw);
            OutputStreamWriter outStream = new OutputStreamWriter(http.getOutputStream(), "EUC-KR");
            PrintWriter writer = new PrintWriter(outStream);
            writer.write(buffer.toString());
            writer.flush();
            writer.close();

            /* 서버에서 전송 받기 */
            InputStream inStream = http.getInputStream();
            BufferedReader reader = new BufferedReader(new InputStreamReader(inStream, "UTF-8"));
            String str = reader.readLine();
            String[] token = str.split(" ");
            String[] jwt = token[1].split("\"");
            Intent intent;
            SQLiteDatabase db = helper.getWritableDatabase();
            //결과 0:관리자, 1:일반사용자, 2:전문가 -> 로그인 진행
            if(token[0].equals("0")) {   //관리자
                ContentValues row = new ContentValues();
                row.put("token", jwt[1]);
                db.insert("TOKEN", null, row);

                intent = new Intent(LoginActivity.this, AdminMainActivity.class);
                startActivity(intent);
            } else if(token[0].equals("1")) {    //일반 사용자
                ContentValues row = new ContentValues();
                row.put("token", jwt[0]);
                db.insert("TOKEN", null, row);

                intent = new Intent(LoginActivity.this, UserMainActivity.class);
                startActivity(intent);
            } else if(token[0].equals("2")) {    //전문가
                ContentValues row = new ContentValues();
                row.put("token", jwt[0]);
                db.insert("TOKEN", null, row);

                intent = new Intent(LoginActivity.this, ExpertMainActivity.class);
                startActivity(intent);
            } else {    //결과 -1:아이디, 비번 잘못 입력
                Toast.makeText(getApplication(), "아이디, 비밀번호를 잘못 입력하셨습니다.", Toast.LENGTH_SHORT).show();
            }
        } catch(MalformedURLException e) {
            e.printStackTrace();
        } catch(IOException e) {
            e.printStackTrace();
        }
    }

    /* 회원가입 클릭했을 때 */
    Button.OnClickListener joinClickListener = new Button.OnClickListener() {
        public void onClick(View v) {
            Intent intent = new Intent(LoginActivity.this, JoinActivity.class);
            startActivity(intent);
        }
    };

    public class checkDBTask extends AsyncTask<Object, Object, Void> {
        @Override
        public Void doInBackground(Object... params) {
            checkDB((TokenDBHelper)params[0]);
            return null;
        }
    }

    public class loginTask extends AsyncTask<Object, Void, Void> {
        @Override
        public Void doInBackground(Object... params) {
            loginProgress((TokenDBHelper)params[0]);
            return null;
        }
    }
}