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

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

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
        List<ManualItem> manualList = new ArrayList<ManualItem>();
        String[] plantNameArray = ((ManualFragment)((AdminMainActivity)AdminMainActivity.context).mAdminPagerAdapter.getItem(0)).adapter.plantNameArray;
        Bitmap[] manualInitImgArray = ((ManualFragment)((AdminMainActivity)AdminMainActivity.context).mAdminPagerAdapter.getItem(0)).adapter.manualInitImgArray;
        for(int i=0; i<plantNameArray.length; i++) {
            manualList.add(new ManualItem(manualInitImgArray[i], plantNameArray[i]));
        }
        /*
        try {
            manualList = new GetManualList().execute().get();
        } catch (InterruptedException e) {
            e.printStackTrace();
        } catch (ExecutionException e) {
            e.printStackTrace();
        }
        */
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
