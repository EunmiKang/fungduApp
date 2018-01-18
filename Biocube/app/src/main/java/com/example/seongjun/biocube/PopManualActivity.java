package com.example.seongjun.biocube;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.view.*;
import android.support.v4.view.PagerAdapter;
import android.text.Layout;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.ImageView;
import android.widget.TextView;

import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;

import me.relex.circleindicator.CircleIndicator;

public class PopManualActivity extends Activity {
    ViewPager viewPager;
    ShowManualPagerAdapter pagerAdapter;
    CircleIndicator indicator;

    String[] getData;
    Bitmap[] manualArray;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.activity_pop_manual);

        Intent intent = getIntent();
        getData = (String[]) intent.getExtras().get("manual");

        TextView textView = (TextView) findViewById(R.id.text_plantName);
        textView.setText(getData[0]);   // 식물 이름

        manualArray  = new Bitmap[(getData.length - 1)];

        viewPager = (ViewPager) findViewById(R.id.viewpager_showManual);
        pagerAdapter = new ShowManualPagerAdapter();

        indicator = (CircleIndicator) findViewById(R.id.indicator_showManual);

        new SettingManualArray().execute();
    }

    public class ShowManualPagerAdapter extends PagerAdapter {
        Bitmap[] manualArray;

        @Override
        public int getCount() {
            return (getData.length - 1);
        }

        @Override
        public boolean isViewFromObject(View view, Object object) {
            return (view == object);
        }

        public View getView(int position, ViewPager pager) {
            return pager.getChildAt(position);
        }

        @Override
        public Object instantiateItem(ViewGroup container, int position) {
            View view = getLayoutInflater().inflate(R.layout.show_manuals_child, null);

            //만들어진 View 안에 있는 ImageView 객체 참조
            ImageView img = (ImageView) view.findViewById(R.id.img_showManual);

            //ImageView에 현재 position 번째에 해당하는 이미지를 보여주기 위한 작업
            //현재 position에 해당하는 이미지를 setting
            img.setImageBitmap(manualArray[position]);

            container.addView(view);

            return view;
        }

        @Override
        public void destroyItem(ViewGroup container, int position, Object object) {
            container.removeView((View)object);
        }

        public void setManualArray(Bitmap[] manualArray) {
            this.manualArray = manualArray;
        }
    }

    /**
     * 쓰레드: 매뉴얼 세팅용
     */
    public class SettingManualArray extends AsyncTask<Object, Object, Integer> {
        // 실제 params 부분에는 execute 함수에서 넣은 인자 값이 들어 있다.
        @Override
        public Integer doInBackground(Object... params) {
            try {
             /* 각 URL에 접속하여 이미지 가져옴 */
                for(int i=1; i<getData.length; i++) {
                    String readUrl = "http://fungdu0624.phps.kr/biocube/manual/" + getData[0] + "/" + getData[i];
                    URL url = new URL(readUrl);
                    HttpURLConnection http = (HttpURLConnection) url.openConnection();

                    /* 전송모드 설정 */
                    http.setDefaultUseCaches(false);
                    http.setDoInput(true);  //서버에서 읽기 모드로 지정
                    http.setRequestMethod("POST");
                    http.setRequestProperty("content-type", "application/x-www-form-urlencoded");   //서버에게 웹에서 <Form>으로 값이 넘어온 것과 같은 방식으로 처리하라는 걸 알려준다

                    http.connect();

                    /* 서버에서 전송 받기 */
                    InputStream inStream = http.getInputStream();
                    Bitmap readImg = BitmapFactory.decodeStream(inStream);
                    manualArray[(i - 1)] = readImg;

                    inStream.close();
                    http.disconnect();
                }

                pagerAdapter.setManualArray(manualArray);
                return 0;
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

            viewPager.setAdapter(pagerAdapter);
            indicator.setViewPager(viewPager);
        }
    }

}
