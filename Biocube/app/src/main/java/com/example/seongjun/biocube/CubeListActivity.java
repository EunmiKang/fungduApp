package com.example.seongjun.biocube;

import android.app.Activity;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.design.widget.TabLayout;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.view.ViewPager;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
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

import static android.R.attr.id;

/**
 * Created by Seongjun on 2017. 11. 28..
 */

public class CubeListActivity extends AppCompatActivity {
    String user_id;
    ListView list_cube;
    String[] cubelist;
    ArrayAdapter adapter;
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_cube_list);
        list_cube = (ListView) findViewById(R.id.list_cube);
        new CubeListActivity.returnCubeList().execute(user_id);

        list_cube.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView parent, View v, int position, long id) {

                // get TextView's Text.
                String strText = (String) parent.getItemAtPosition(position) ;

                // TODO : use strText
            }
        }) ;
    }

    private TokenDBHelper helper = new TokenDBHelper(this);

    public class returnCubeList extends AsyncTask<String,Object,Integer> {

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

                InputStream inStream = http.getInputStream();
                BufferedReader reader = new BufferedReader(new InputStreamReader(inStream, "UTF-8"));
                user_id = reader.readLine();

                inStream.close();
                http.disconnect();

             /* URL 설정하고 접속 */
                url = new URL("http://fungdu0624.phps.kr/biocube/returncube.php");
                http = (HttpURLConnection) url.openConnection();

            /* 전송모드 설정 */
                http.setDefaultUseCaches(false);
                http.setDoInput(true);  //서버에서 읽기 모드로 지정
                http.setDoOutput(true);    //서버에서 쓰기 모드로 지정
                http.setRequestMethod("POST");
                http.setRequestProperty("content-type", "application/x-www-form-urlencoded");   //서버에게 웹에서 <Form>으로 값이 넘어온 것과 같은 방식으로 처리하라는 걸 알려준다

            /* 서버로 값 전송 */
                buffer = new StringBuffer();
                buffer.append("user_id").append("=").append(user_id);

                outStream = new OutputStreamWriter(http.getOutputStream(), "EUC-KR");
                writer = new PrintWriter(outStream);
                writer.write(buffer.toString());
                writer.flush();
                writer.close();
            /* 서버에서 전송 받기 */

                InputStream inStream2 = http.getInputStream();
                reader = new BufferedReader(new InputStreamReader(inStream2, "UTF-8"));
                String cube = reader.readLine();
                cubelist = cube.split(",");

                inStream.close();
                http.disconnect();

            } catch(MalformedURLException e) {
                e.printStackTrace();
            } catch(IOException e) {
                e.printStackTrace();
            }

            return -1;
        }

        @Override
        public void onPostExecute(Integer result) {
            super.onPostExecute(result);
            // Todo: doInBackground() 메소드 작업 끝난 후 처리해야할 작업..
            adapter = new ArrayAdapter(CubeListActivity.this, android.R.layout.simple_list_item_1, cubelist) ;
            list_cube.setAdapter(adapter);

        }
    }

}
