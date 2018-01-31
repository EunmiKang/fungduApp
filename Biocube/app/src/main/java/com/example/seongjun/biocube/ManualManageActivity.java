package com.example.seongjun.biocube;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
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
import android.widget.Toast;

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
        gridView.setOnItemClickListener(manualItemClickListener);
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

    GridView.OnItemClickListener manualItemClickListener = new GridView.OnItemClickListener() {
        int mChoicedArrayItem;
        @Override
        public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
            String plant_name = ((TextView) view.findViewById(R.id.text_manualitem)).getText().toString();
            AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(ManualManageActivity.this);
            alertDialogBuilder.setTitle(plant_name);
            CharSequence[] mArrayItem = {"매뉴얼 보기", "매뉴얼 삭제"};

            alertDialogBuilder.setSingleChoiceItems(mArrayItem, 0, new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog, int whichButton) {
                    mChoicedArrayItem = whichButton;
                }
            });
            alertDialogBuilder.setPositiveButton("선택", new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog, int whichButton) {
                    if (mChoicedArrayItem == 0) {   // 매뉴얼 보기
                        Toast.makeText(ManualManageActivity.this, "매뉴얼 보기", Toast.LENGTH_SHORT).show();
                        dialog.dismiss();
                    } else if (mChoicedArrayItem == 1) {    // 매뉴얼 삭제
                        AlertDialog.Builder builder = new AlertDialog.Builder(ManualManageActivity.this)
                                .setMessage("매뉴얼을 삭제하시겠습니까?")
                                .setPositiveButton("네", new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialog, int which) {
                                        Toast.makeText(ManualManageActivity.this, "매뉴얼 삭제", Toast.LENGTH_SHORT).show();
                                        dialog.dismiss();
                                    }
                                })
                                .setNegativeButton("아니오", new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialog, int which) {
                                        dialog.dismiss();
                                    }
                                });
                        AlertDialog deleteDialog = builder.create();  //알림창 객체 생성
                        deleteDialog.show();  //알림창 띄우기
                    }
                }
            });
            alertDialogBuilder.setNegativeButton("취소", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    dialog.dismiss();
                }
            });

            AlertDialog dialog = alertDialogBuilder.create();  //알림창 객체 생성
            dialog.show();  //알림창 띄우기
        }
    };
}
