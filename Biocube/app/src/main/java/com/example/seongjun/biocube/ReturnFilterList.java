package com.example.seongjun.biocube;

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
import java.util.ArrayList;

/**
 * Created by Eunmi on 2017-12-06.
 */

public class ReturnFilterList extends AsyncTask<String, Object, String[]> {
    private String[] filterList;

    @Override
    protected String[] doInBackground(String... params) {
        try {
                /* URL 설정하고 접속 */
            URL url = new URL("http://fungdu0624.phps.kr/biocube/returnFilter.php");
            HttpURLConnection http = (HttpURLConnection) url.openConnection();

                /* 전송모드 설정 */
            http.setDefaultUseCaches(false);
            http.setDoInput(true);  //서버에서 읽기 모드로 지정
            http.setDoOutput(true);    //서버에서 쓰기 모드로 지정
            http.setRequestMethod("POST");
            http.setRequestProperty("content-type", "application/x-www-form-urlencoded");   //서버에게 웹에서 <Form>으로 값이 넘어온 것과 같은 방식으로 처리하라는 걸 알려준다

                /* 서버로 값 전송 */
            StringBuffer buffer = new StringBuffer();
            buffer.append("user_id").append("=").append(params[0]);

            OutputStreamWriter outStream = new OutputStreamWriter(http.getOutputStream(), "EUC-KR");
            PrintWriter writer = new PrintWriter(outStream);
            writer.write(buffer.toString());
            writer.flush();
            writer.close();

                /* 서버에서 전송 받기 */
            InputStream inStream = http.getInputStream();
            BufferedReader reader = new BufferedReader(new InputStreamReader(inStream, "UTF-8"));
            String filter = reader.readLine();
            if(filter != null) {
                filterList = filter.split(",");
            }

            inStream.close();
            http.disconnect();

        } catch(MalformedURLException e) {
            e.printStackTrace();
        } catch(IOException e) {
            e.printStackTrace();
        }

        return filterList;
    }
}
