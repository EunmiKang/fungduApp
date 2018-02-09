package com.example.seongjun.biocube;

import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.AsyncTask;

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
 * Created by Seongjun on 2017. 12. 6..
 */

public class GetUserInfo extends AsyncTask<Object,Object,String[]> {
    // 실제 params 부분에는 execute 함수에서 넣은 인자 값이 들어 있다.
    private TokenDBHelper helper;
    String[] userinfo;
    @Override
    public String[] doInBackground(Object... params) {
        try {
            helper = (TokenDBHelper)params[0];
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
            userinfo = str.split(",");

            inStream.close();
            http.disconnect();


        } catch (MalformedURLException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        //publishProgress(params);    // 중간 중간에 진행 상태 UI 를 업데이트 하기 위해 사용..
        return userinfo;
    }

    @Override
    public void onPostExecute(String[] result) {
        super.onPostExecute(result);
        // Todo: doInBackground() 메소드 작업 끝난 후 처리해야할 작업..

    }
}