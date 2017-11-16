package com.example.seongjun.biocube;

import android.content.ContentValues;
import android.content.Intent;
import android.database.sqlite.SQLiteDatabase;
import android.os.AsyncTask;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.Layout;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.view.ViewGroup.LayoutParams;
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

    final RadioGroup rg = (RadioGroup)findViewById(R.id.group_authority);
    int id = rg.getCheckedRadioButtonId();
    //getCheckedRadioButtonId() 의 리턴값은 선택된 RadioButton 의 id 값.
    RadioButton rb = (RadioButton) findViewById(id);
    LinearLayout layout_join = (LinearLayout)findViewById(R.id.layout_join);

    Button.OnClickListener joinClickListener = new Button.OnClickListener() {
        public void onClick(View v) {
//            new JoinActivity.joinTask().execute(id);
            //thread 실행
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_join);

        findViewById(R.id.btn_join).setOnClickListener(joinClickListener);

        }

    public void joinProcess(View view) {

    }
    public void checkExpertRadio(){
        EditText editPhone = new EditText(this);
        EditText editJob = new EditText(this);
        editPhone.setLayoutParams(new LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT));
        editJob.setLayoutParams(new LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT));

    }
    public void checkUserRadio(){

    }

    public class joinTask extends AsyncTask<Object,Object,Integer>{

        @Override
        protected Integer doInBackground(Object... params) {
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
