package com.example.seongjun.biocube;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.support.v4.view.ViewPager;
import android.widget.ImageView;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;

/**
 * Created by Eunmi on 2017-11-29.
 */

/**
 * 쓰레드: 매뉴얼 세팅용
 */
public class SettingManuals extends AsyncTask<Object, Object, Integer> {
    private ManualsAdapter adapter;
    private ViewPager pager;
    private ImageView logo;

    private Bitmap logoImg;

    // 실제 params 부분에는 execute 함수에서 넣은 인자 값이 들어 있다.
    @Override
    public Integer doInBackground(Object... params) {
        try {
            /* parameter로 받은 것들 저장 */
            logo = (ImageView) params[0];
            pager = (ViewPager) params[1];
            adapter = (ManualsAdapter) params[2];

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

            inStream.close();
            http.disconnect();

            String[] token = str.split(" ");

                /* 매뉴얼 수 setting */
            adapter.setManualNum(Integer.parseInt(token[0]));
            Bitmap[] manualInitArray = new Bitmap[Integer.parseInt(token[0])];

                /* 매뉴얼 처음 이미지 setting */
            for(int i=1; i<=Integer.parseInt(token[0]); i++) {
                String readURL = "http://fungdu0624.phps.kr/biocube/manual/" + token[i] + "/" + token[i] + ".jpg";
                url = new URL(readURL);
                http = (HttpURLConnection) url.openConnection();
                http.connect();
                //스트림생성
                inStream = http.getInputStream();
                //스트림에서 받은 데이터를 비트맵 변환
                //인터넷에서 이미지 가져올 때는 Bitmap 사용해야 함
                Bitmap readImg = BitmapFactory.decodeStream(inStream);
                manualInitArray[(i-1)] = readImg;
            }
            inStream.close();
            http.disconnect();
            adapter.setManualInitImg(manualInitArray);

                /* 매뉴얼 화면에 로고 이미지 가져오기 */
                /*
            String readURL = "http://fungdu0624.phps.kr/biocube/img/testimg.jpg";
            url = new URL(readURL);
            http = (HttpURLConnection) url.openConnection();
            http.connect();
            //스트림생성
            inStream = http.getInputStream();
            logoImg = BitmapFactory.decodeStream(inStream);

            inStream.close();
            http.disconnect();
            */
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
        // Todo: doInBackground() 메소드 작업 끝난 후 처리해야할 작업..
        pager.setAdapter(adapter);
        //logo.setImageBitmap(logoImg);
    }

    @Override
    protected void onProgressUpdate(Object[] values) {
        super.onProgressUpdate(values);
        // Todo: publishProgress() 메소드 호출시 처리할 작업..
    }
}
