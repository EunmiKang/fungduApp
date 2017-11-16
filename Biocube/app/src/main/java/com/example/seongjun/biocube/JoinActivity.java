package com.example.seongjun.biocube;

import android.content.ContentValues;
import android.content.Intent;
import android.database.sqlite.SQLiteDatabase;
import android.os.AsyncTask;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.Layout;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;

public class JoinActivity extends AppCompatActivity {

    RadioGroup rg;
    RadioButton rb;
    LinearLayout layout_join;
    EditText editId;
    EditText editPw;
    EditText editNickname;
    EditText editPhone;
    EditText editJob;

    Button.OnClickListener joinClickListener = new Button.OnClickListener() {
        public void onClick(View v) {
            String id = editId.getText().toString();
            String pw = editPw.getText().toString();
            String nickname = editNickname.getText().toString();
            String phone = editPhone.getText().toString();
            String job = editJob.getText().toString();
            String authority;
            if(rb.getText().toString().equals("일반사용자")){
                authority = "1";
            }
            else{
                authority = "2";
            }
            new JoinActivity.joinTask().execute(id,pw,nickname,authority,phone,job);
            //thread 실행
        }
    };

    RadioButton.OnClickListener radioExpertClickListener = new RadioButton.OnClickListener() {
        public void onClick(View v) {
            checkExpertRadio();
        }
    };

    RadioButton.OnClickListener radioUserClickListener = new RadioButton.OnClickListener() {
        public void onClick(View v) {
            checkUserRadio();
        }
    };
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_join);
        rg = (RadioGroup)findViewById(R.id.group_authority);
        int authority = rg.getCheckedRadioButtonId();
        //getCheckedRadioButtonId() 의 리턴값은 선택된 RadioButton 의 id 값.
        rb = (RadioButton) findViewById(authority);
        layout_join = (LinearLayout)findViewById(R.id.layout_join);
        editId = (EditText)findViewById(R.id.idText);
        editPw = (EditText)findViewById(R.id.pwText);
        editNickname = (EditText)findViewById(R.id.nicknameText);
        editPhone = (EditText)findViewById(R.id.editPhone);
        editJob = (EditText)findViewById(R.id.editJob);
        findViewById(R.id.btn_join).setOnClickListener(joinClickListener);
        findViewById(R.id.radio_expert).setOnClickListener(radioExpertClickListener);
        findViewById(R.id.radio_user).setOnClickListener(radioUserClickListener);

        }

    public void joinProcess(View view) {

    }

    public void checkExpertRadio(){
        editPhone.setVisibility(View.VISIBLE);
        editJob.setVisibility(View.VISIBLE);
    }
    public void checkUserRadio(){
        editPhone.setVisibility(View.GONE);
        editJob.setVisibility(View.GONE);
    }

    public class joinTask extends AsyncTask<String,Object,Integer>{

        @Override
        protected Integer doInBackground(String... params) {
            try {
             /* URL 설정하고 접속 */
                URL url = new URL("http://fungdu0624.phps.kr/biocube/join.php");
                HttpURLConnection http = (HttpURLConnection) url.openConnection();

            /* 전송모드 설정 */
                http.setDefaultUseCaches(false);
                http.setDoInput(true);  //서버에서 읽기 모드로 지정
                http.setDoOutput(true);    //서버에서 쓰기 모드로 지정
                http.setRequestMethod("POST");
                http.setRequestProperty("content-type", "application/x-www-form-urlencoded");   //서버에게 웹에서 <Form>으로 값이 넘어온 것과 같은 방식으로 처리하라는 걸 알려준다

            /* 서버로 값 전송 */
                StringBuffer buffer = new StringBuffer();
                buffer.append("userID").append("=").append(params[1].toString()).append("&");
                buffer.append("userPW").append("=").append(params[2].toString());
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
            } catch(MalformedURLException e) {
                e.printStackTrace();
            } catch(IOException e) {
                e.printStackTrace();
            }

            return null;
        }
    }
}
