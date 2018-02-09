package com.example.seongjun.biocube;

import android.app.Activity;
import android.content.Context;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.View;
import android.view.Window;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLDecoder;
import java.net.URLEncoder;
import java.util.List;
import java.util.concurrent.ExecutionException;

/**
 * Created by Seongjun on 2017. 11. 28..
 */

public class ChangeInfoActivity extends Activity {

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
    String changenickname = "";
    private TokenDBHelper helper = new TokenDBHelper(this);
    Spinner spinner_filter;
    List filterItems;
    Context context = this;
    private static final String TAG_FILTERS = "filteritems";
    private static final String TAG_FILTER = "filter";

    ArrayAdapter<String> dataAdapter;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.activity_change_info);

        id = getIntent().getStringExtra("id");
        textNick = (TextView) findViewById(R.id.text_nickname);
        changeNick = (EditText) findViewById(R.id.edit_changeNickname);
        changePW = (EditText) findViewById(R.id.edit_changePW);
        addFilter = (EditText) findViewById(R.id.edit_addFilter);
        changeJob = (EditText) findViewById(R.id.edit_changeJob);
        changePhone = (EditText) findViewById(R.id.edit_changePhone);
        findViewById(R.id.btn_change).setOnClickListener(changeInfoClickListener);
        findViewById(R.id.btn_addFilter).setOnClickListener(addFilterClickListener);
        spinner_filter = (Spinner) findViewById(R.id.spinner_filter);
        findViewById(R.id.btn_deleteFilter).setOnClickListener(deleteFilterClickListener);


        try {
            filterItems = new GetFilter().execute("http://fungdu0624.phps.kr/biocube/getMyfilter.php", id).get();
            dataAdapter = new ArrayAdapter<String>(this, android.R.layout.simple_spinner_item, filterItems);
            dataAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
            spinner_filter.setAdapter(dataAdapter);

        } catch (InterruptedException e) {
            e.printStackTrace();
        } catch (ExecutionException e) {
            e.printStackTrace();
        }

        try {

            String[] userInfo = new GetUserInfo().execute(helper).get();
            nickname = userInfo[0];
            if (userInfo[1].equals("1")) {
                authority = 1;
            } else if (userInfo[1].equals("2")) {
                authority = 2;
            } else {
                authority = 0;
            }

            textNick.setText(nickname);
            if (authority == 2) {
                changePhone.setVisibility(View.VISIBLE);
                changeJob.setVisibility(View.VISIBLE);
            }

        } catch (InterruptedException e) {
            e.printStackTrace();
        } catch (ExecutionException e) {
            e.printStackTrace();
        }

    }
    Button.OnClickListener addFilterClickListener = new Button.OnClickListener(){//필터추가버튼
        @Override
        public void onClick(View v) {
            String addFilterName = addFilter.getText().toString();
            try {
                addFilterName = URLEncoder.encode(addFilterName,"UTF-8");
            } catch (UnsupportedEncodingException e) {
                e.printStackTrace();
            }
            new ModifyFilterTask().execute("ADD",addFilterName);
        }
    };

    Button.OnClickListener deleteFilterClickListener = new Button.OnClickListener(){//필터삭제버튼

        @Override
        public void onClick(View v) {
            String deleteFilterName = spinner_filter.getSelectedItem().toString();
            try {
                deleteFilterName = URLEncoder.encode(deleteFilterName,"UTF-8");
            } catch (UnsupportedEncodingException e) {
                e.printStackTrace();
            }
            new ModifyFilterTask().execute("DELETE",deleteFilterName);
        }
    };

    Button.OnClickListener changeInfoClickListener= new Button.OnClickListener(){//변경버튼 클릭시

        @Override
        public void onClick(View v) {
            settingData();
            new changeInfoTask().execute();
        }
    };

    public void settingData(){
        String changejob="";
        if(!(changenickname = changeNick.getText().toString()).equals("")){
            try{
                changenickname = URLEncoder.encode(changenickname,"UTF-8");
            } catch(Exception e) {
                e.printStackTrace();
            }
            setData = "nickname = '" + changenickname +"',";
        }
        if(!changePW.getText().toString().equals("")){
            setData = setData + "pw = '"+changePW.getText().toString() + "',";
        }
        if(!(changejob = changeJob.getText().toString()).equals("")){
            try{
                changejob = URLEncoder.encode(changejob,"UTF-8");
            } catch(Exception e) {
                e.printStackTrace();
            }
            setData = setData + "job = '" + changejob + "',";
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

                try{
                    nickname = URLEncoder.encode(nickname,"UTF-8");
                } catch(Exception e) {
                    e.printStackTrace();
                }

                StringBuffer buffer = new StringBuffer();
                buffer.append("userID").append("=").append(id).append("&");
                buffer.append("nickname").append("=").append(nickname).append("&");
                buffer.append("changeNickname").append("=").append(changenickname).append("&");
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
                if(!changenickname.equals("")) {
                    if(authority == 2) {
                        try {
                            changenickname = URLDecoder.decode(changenickname, "UTF-8");
                        } catch (UnsupportedEncodingException e) {
                            e.printStackTrace();
                        }
                        ((ExpertPageFragment) ((ExpertMainActivity) ExpertMainActivity.mContext).mExpertPagerAdapter.getItem(2)).settingPage(changenickname);
                    } else if(authority == 1) {
                        ((UserPageFragment) ((UserMainActivity) UserMainActivity.context).mUserPagerAdapter.getItem(4)).settingNickname();
                    }
                }
                finish();
            }
            else{
                Toast.makeText(getApplicationContext(), "변경에 실패하였습니다.", Toast.LENGTH_SHORT).show();
            }
        }

    }


    public class ModifyFilterTask extends AsyncTask<Object,Object,String>{

        @Override
        protected String doInBackground(Object... params) {

            try{
                URL url;
                if(params[0].toString().equals("ADD")){//추가시
                    url = new URL("http://fungdu0624.phps.kr/biocube/addFilter.php");
                }
                else{//삭제시
                    url = new URL("http://fungdu0624.phps.kr/biocube/deleteFilter.php");
                }

                HttpURLConnection http = (HttpURLConnection) url.openConnection();

                http.setDefaultUseCaches(false);
                http.setDoInput(true);  //서버에서 읽기 모드로 지정
                http.setDoOutput(true);    //서버에서 쓰기 모드로 지정
                http.setRequestMethod("POST");
                http.setRequestProperty("content-type", "application/x-www-form-urlencoded");   //서버에게 웹에서 <Form>으로 값이 넘어온 것과 같은 방식으로 처리하라는 걸 알려준다

                StringBuffer buffer = new StringBuffer();
                buffer.append("user_id").append("=").append(id).append("&");
                buffer.append("filter").append("=").append(params[1].toString());

                OutputStreamWriter outStream = new OutputStreamWriter(http.getOutputStream(), "EUC-KR");
                PrintWriter writer = new PrintWriter(outStream);
                writer.write(buffer.toString());
                writer.flush();
                writer.close();

                InputStream inStream = http.getInputStream();
                BufferedReader reader = new BufferedReader(new InputStreamReader(inStream, "UTF-8"));
                String result = reader.readLine();

                return result;
            }catch(MalformedURLException e){
                e.printStackTrace();
            }catch(IOException e){
                e.printStackTrace();
            }

            return null;
        }

        @Override
        public void onPostExecute(String result) {
            super.onPostExecute(result);
            // Todo: doInBackground() 메소드 작업 끝난 후 처리해야할 작업..
            switch (result){
                case "add_success":
                    Toast.makeText(ChangeInfoActivity.this, "필터추가에 성공하였습니다.", Toast.LENGTH_SHORT).show();
                    addFilter.setText("");
                    break;
                case "add_fail":
                    Toast.makeText(ChangeInfoActivity.this, "필터추가에 실패하였습니다.", Toast.LENGTH_SHORT).show();
                    break;
                case "delete_success":
                    Toast.makeText(ChangeInfoActivity.this, "필터삭제에 성공하였습니다.", Toast.LENGTH_SHORT).show();
                    break;
                case "delete_fail":
                    Toast.makeText(ChangeInfoActivity.this, "필터삭제에 실패하였습니다.", Toast.LENGTH_SHORT).show();
                    break;
            }
            try {
                filterItems = new GetFilter().execute("http://fungdu0624.phps.kr/biocube/getMyfilter.php", id).get();
                dataAdapter = new ArrayAdapter<String>(context, android.R.layout.simple_spinner_item, filterItems);
                dataAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                spinner_filter.setAdapter(dataAdapter);
            } catch (InterruptedException e) {
                e.printStackTrace();
            } catch (ExecutionException e) {
                e.printStackTrace();
            }

        }

    }

}
