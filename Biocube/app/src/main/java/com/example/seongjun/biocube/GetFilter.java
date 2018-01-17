package com.example.seongjun.biocube;

import android.os.AsyncTask;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

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
 * Created by Seongjun on 2018. 1. 17..
 */

public class GetFilter extends AsyncTask<String, Void, List<String>> {

    List filterItems;
    private static final String TAG_FILTERS = "filteritems";
    private static final String TAG_FILTER = "filter";
    JSONArray filter = null;

    @Override
    protected List<String> doInBackground(String... params) {
        filterItems = new ArrayList();
        try {
     /* URL 설정하고 접속 */
            URL url = new URL(params[0]);
            HttpURLConnection http = (HttpURLConnection) url.openConnection();

    /* 전송모드 설정 */
            http.setDefaultUseCaches(false);
            http.setDoInput(true);  //서버에서 읽기 모드로 지정
            http.setDoOutput(true);    //서버에서 쓰기 모드로 지정
            http.setRequestMethod("POST");
            http.setRequestProperty("content-type", "application/x-www-form-urlencoded");   //서버에게 웹에서 <Form>으로 값이 넘어온 것과 같은 방식으로 처리하라는 걸 알려준다

    /* 서버로 값 전송 */
            StringBuffer buffer = new StringBuffer();
            buffer.append("user_id").append("=").append(params[1].toString());


            OutputStreamWriter outStream = new OutputStreamWriter(http.getOutputStream(), "EUC-KR");
            PrintWriter writer = new PrintWriter(outStream);
            writer.write(buffer.toString());
            writer.flush();
            writer.close();

    /* 서버에서 전송 받기 */
            InputStream inStream = http.getInputStream();
            BufferedReader reader = new BufferedReader(new InputStreamReader(inStream, "UTF-8"));
            StringBuilder sb = new StringBuilder();

            String json;
            while((json = reader.readLine())!= null){
                sb.append(json+"\n");
            }

            try {
                JSONObject jsonObj = new JSONObject(sb.toString().trim());
                filter = jsonObj.getJSONArray(TAG_FILTERS);

                for (int i = 0; i < filter.length(); i++) {
                    JSONObject c = filter.getJSONObject(i);
                    String filter = c.getString(TAG_FILTER);
                    filterItems.add(filter);
                }
            }catch (JSONException e) {
                e.printStackTrace();
            }


        } catch(MalformedURLException e) {
            e.printStackTrace();
        } catch(IOException e) {
            e.printStackTrace();
        }

        return filterItems;
    }

    @Override
    protected void onPostExecute(List<String> result){

    }
}
