package com.example.seongjun.biocube;

import android.app.AlertDialog;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.TextView;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutionException;

public class ManualManageActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_manual_manage);

        /* 매뉴얼 등록 버튼 */
        Button registManualBtn = (Button) findViewById(R.id.btn_manual_regist);
        registManualBtn.setOnClickListener(registManualClickListener);

        /* 매뉴얼 보이게 하기 */
        List<ManualItem> manualList = null;
        try {
            manualList = new GetManualList().execute().get();
        } catch (InterruptedException e) {
            e.printStackTrace();
        } catch (ExecutionException e) {
            e.printStackTrace();
        }
        GridView gridView = (GridView) findViewById(R.id.grid_manual);
        gridView.setAdapter(new ManualManageAdapter(this, manualList));
        gridView.setOnItemLongClickListener(manualItemClickListener);
    }

    Button.OnClickListener registManualClickListener = new Button.OnClickListener() {
        public void onClick(View v) {
            Intent intent = new Intent(ManualManageActivity.this, ManualRegistActivity.class);
            startActivity(intent);
        }
    };

    public class ManualItem {
        private Bitmap plantImg;
        private String plantName;

        public ManualItem(Bitmap plantImg, String plantName) {
            this.plantImg = plantImg;
            this.plantName = plantName;
        }

        public Bitmap getPlantImg() {
            return plantImg;
        }

        public void setPlantImg(Bitmap plantImg) {
            this.plantImg = plantImg;
        }

        public String getPlantName() {
            return plantName;
        }

        public void setPlantName(String plantName) {
            this.plantName = plantName;
        }
    }

    public class GetManualList extends AsyncTask<Object, Object, List<ManualItem>> {

        @Override
        protected List<ManualItem> doInBackground(Object[] objects) {
            List<ManualItem> returnList = new ArrayList<ManualItem>();

            try {
                /* URL 설정하고 접속 */
                URL url = new URL("http://fungdu0624.phps.kr/biocube/returnManualList.php");
                HttpURLConnection http = (HttpURLConnection) url.openConnection();

                /* 전송모드 설정 */
                http.setDefaultUseCaches(false);
                http.setDoInput(true);  //서버에서 읽기 모드로 지정
                http.setDoOutput(true);    //서버에서 쓰기 모드로 지정
                http.setRequestMethod("POST");
                http.setRequestProperty("content-type", "application/x-www-form-urlencoded");   //서버에게 웹에서 <Form>으로 값이 넘어온 것과 같은 방식으로 처리하라는 걸 알려준다

                /* 서버에서 전송 받기 */
                String[] manualList;
                InputStream inStream = http.getInputStream();
                BufferedReader reader = new BufferedReader(new InputStreamReader(inStream, "UTF-8"));
                String readResult = reader.readLine();
                if (readResult != null) {
                    manualList = readResult.split(",");
                } else {
                    manualList = new String[0];
                }
                inStream.close();
                http.disconnect();

                /* 리턴할 리스트에 읽어들인 것으로 처리 */
                String plantName;
                Bitmap plantImg;
                for (int i = 1; i < manualList.length; i++) {
                    plantName = manualList[i];

                    /* 이미지 처리 */
                    String readURL = "http://fungdu0624.phps.kr/biocube/manual/" + URLEncoder.encode(plantName, "euc-kr") + ".jpg";
                    url = new URL(readURL);
                    http = (HttpURLConnection) url.openConnection();
                    http.connect();
                    //스트림생성
                    inStream = http.getInputStream();
                    //스트림에서 받은 데이터를 비트맵 변환
                    plantImg = BitmapFactory.decodeStream(inStream);

                    /* 리턴 리스트에 추가 */
                    returnList.add(new ManualItem(plantImg, plantName));
                }

            } catch (MalformedURLException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            }

            return returnList;
        }
    }

    public class ManualManageAdapter extends BaseAdapter {
        List<ManualItem> list;
        private LayoutInflater layoutInflater;

        public ManualManageAdapter(Context context, List<ManualItem> list) {
            this.list = list;
            layoutInflater = LayoutInflater.from(context);
        }

        @Override
        public int getCount() {
            return list.size();
        }

        @Override
        public Object getItem(int position) {
            return list.get(position);
        }

        @Override
        public long getItemId(int position) {
            return position;
        }

        @Override
        public View getView(int position, View view, ViewGroup viewGroup) {
            ViewHolder holder;

            if (view == null) {
                view = layoutInflater.inflate(R.layout.custom_manual_item, null);
                holder = new ViewHolder();
                holder.plantImgView = (ImageView) view.findViewById(R.id.img_manualitem);
                holder.plantNameView = (TextView) view.findViewById(R.id.text_manualitem);
                view.setTag(holder);
            } else {
                holder = (ViewHolder) view.getTag();
            }

            ManualItem manualItem = this.list.get(position);
            holder.plantNameView.setText(manualItem.getPlantName());
            holder.plantImgView.setImageBitmap(manualItem.getPlantImg());

            return view;
        }

        class ViewHolder {
            ImageView plantImgView;
            TextView plantNameView;
        }
    }

    GridView.OnItemLongClickListener manualItemClickListener = new GridView.OnItemLongClickListener() {
        @Override
        public boolean onItemLongClick(AdapterView<?> parent, View view, int position, long id) {

            return false;
        }
    };
}
