package com.example.seongjun.biocube;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
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
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutionException;

public class ManualManageActivity extends AppCompatActivity {

    public static Context mContext;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_manual_manage);

        mContext = this;

        /* 매뉴얼 등록 버튼 */
        Button registManualBtn = (Button) findViewById(R.id.btn_manual_regist);
        registManualBtn.setOnClickListener(registManualClickListener);

        /* 매뉴얼 보이게 하기 */
        showManuals();
    }

    Button.OnClickListener registManualClickListener = new Button.OnClickListener() {
        public void onClick(View v) {
            Intent intent = new Intent(ManualManageActivity.this, ManualRegistActivity.class);
            startActivity(intent);
        }
    };

    public void showManuals() {
        List<ManualItem> manualList = new ArrayList<ManualItem>();
        String[] plantNameArray = ((ManualFragment)((AdminMainActivity)AdminMainActivity.context).mAdminPagerAdapter.getItem(0)).adapter.plantNameArray;
        Bitmap[] manualInitImgArray = ((ManualFragment)((AdminMainActivity)AdminMainActivity.context).mAdminPagerAdapter.getItem(0)).adapter.manualInitImgArray;
        for(int i=0; i<plantNameArray.length; i++) {
            manualList.add(new ManualItem(manualInitImgArray[i], plantNameArray[i]));
        }

        GridView gridView = (GridView) findViewById(R.id.grid_manual);
        gridView.setAdapter(new ManualManageAdapter(this, manualList));
        gridView.setOnItemClickListener(manualItemClickListener);
    }

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
            final String plant_name = ((TextView) view.findViewById(R.id.text_manualitem)).getText().toString();
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
                        showManualByPlant(plant_name);
                    } else if (mChoicedArrayItem == 1) {    // 매뉴얼 삭제
                        AlertDialog.Builder builder = new AlertDialog.Builder(ManualManageActivity.this)
                                .setMessage("매뉴얼을 삭제하시겠습니까?")
                                .setPositiveButton("네", new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialog, int which) {
                                        deleteManual(plant_name);
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

    public void showManualByPlant(String plant_name) {
        /* 매뉴얼 이미지 파일 이름들 가져오기 */
        ArrayList<String> manualNameList = new ArrayList<>();
        try {
            manualNameList = new getManualNameList().execute(plant_name).get();
        } catch (InterruptedException e) {
            e.printStackTrace();
        } catch (ExecutionException e) {
            e.printStackTrace();
        }

        Intent intent = new Intent(ManualManageActivity.this, PopManualActivity.class);
        intent.putExtra("plantName", plant_name);
        intent.putExtra("manual", manualNameList);
        startActivity(intent);
    }

    public class getManualNameList extends AsyncTask<String, Object, ArrayList<String>> {
        @Override
        public ArrayList<String> doInBackground(String... params) {
            ArrayList<String> imageNameList = new ArrayList<>();
            try {
            /* parameter로 받은 것들 저장 */
                String plant_name = URLEncoder.encode(params[0],"utf-8");

             /* URL 설정하고 접속 */
                URL url = new URL("http://fungdu0624.phps.kr/biocube/getManualListByPlant.php");
                HttpURLConnection http = (HttpURLConnection) url.openConnection();

            /* 전송모드 설정 */
                http.setDefaultUseCaches(false);
                http.setDoInput(true);  //서버에서 읽기 모드로 지정
                http.setDoOutput(true);    //서버에서 쓰기 모드로 지정
                http.setRequestMethod("POST");
                http.setRequestProperty("content-type", "application/x-www-form-urlencoded");   //서버에게 웹에서 <Form>으로 값이 넘어온 것과 같은 방식으로 처리하라는 걸 알려준다

            /* 서버로 값 전송 */
                StringBuffer buffer = new StringBuffer();
                buffer.append("plant_name").append("=").append(plant_name);
                OutputStreamWriter outStream = new OutputStreamWriter(http.getOutputStream(), "EUC-KR");
                PrintWriter writer = new PrintWriter(outStream);
                writer.write(buffer.toString());
                writer.flush();
                writer.close();

            /* 서버에서 전송 받기 */
                InputStream inStream = http.getInputStream();
                BufferedReader reader = new BufferedReader(new InputStreamReader(inStream, "UTF-8"));
                //String str = reader.readLine();
                StringBuilder sb = new StringBuilder();

                String json;
                while ((json = reader.readLine()) != null) {
                    sb.append(json + "\n");
                }

                inStream.close();
                http.disconnect();

                try {
                    JSONObject jsonObj = new JSONObject(sb.toString().trim());
                    JSONArray manualArray = jsonObj.getJSONArray("manual_array");

                /* 매뉴얼 리스트 저장 */
                    for (int i = 0; i < manualArray.length(); i++) {
                        JSONObject manualObject = manualArray.getJSONObject(i);

                        for (int j = 1; j <= 10; j++) {
                            String imageName = manualObject.getString("image" + j);
                            if (!imageName.equals("null")) {
                                imageNameList.add(imageName);
                            } else {
                                break;
                            }
                        }
                    }
                    inStream.close();
                    http.disconnect();
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            } catch (MalformedURLException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            }

            return imageNameList;
        }
    }


    public void deleteManual(String plant_name) {
        /* 디비, 서버에서 삭제 */
        Boolean result = false;
        try {
            result = new deleteManual().execute(plant_name).get();
        } catch (InterruptedException e) {
            e.printStackTrace();
        } catch (ExecutionException e) {
            e.printStackTrace();
        }
        if(result) {
            /* 매뉴얼 관리 페이지 업데이트 */
            try {
                new SettingManuals().execute(((ManualFragment)((AdminMainActivity)AdminMainActivity.context).mAdminPagerAdapter.getItem(0)).pager, ((ManualFragment)((AdminMainActivity)AdminMainActivity.context).mAdminPagerAdapter.getItem(0)).adapter, ((ManualFragment)((AdminMainActivity)AdminMainActivity.context).mAdminPagerAdapter.getItem(0)).indicator).get();
            } catch (InterruptedException e) {
                e.printStackTrace();
            } catch (ExecutionException e) {
                e.printStackTrace();
            }
            showManuals();
            Toast.makeText(ManualManageActivity.this, plant_name + " 매뉴얼이 삭제되었습니다.", Toast.LENGTH_SHORT).show();
        } else {
            Toast.makeText(ManualManageActivity.this, "매뉴얼 삭제를 실패하였습니다.", Toast.LENGTH_SHORT).show();
        }
    }

    public class deleteManual extends AsyncTask<String, Object, Boolean> {
        @Override
        public Boolean doInBackground(String... params) {
            String result = "-1";
            ArrayList<String> imageNameList = new ArrayList<>();
            try {
            /* parameter로 받은 것들 저장 */
                String plant_name = URLEncoder.encode(params[0],"utf-8");

             /* URL 설정하고 접속 */
                URL url = new URL("http://fungdu0624.phps.kr/biocube/deleteManual.php");
                HttpURLConnection http = (HttpURLConnection) url.openConnection();

            /* 전송모드 설정 */
                http.setDefaultUseCaches(false);
                http.setDoInput(true);  //서버에서 읽기 모드로 지정
                http.setDoOutput(true);    //서버에서 쓰기 모드로 지정
                http.setRequestMethod("POST");
                http.setRequestProperty("content-type", "application/x-www-form-urlencoded");   //서버에게 웹에서 <Form>으로 값이 넘어온 것과 같은 방식으로 처리하라는 걸 알려준다

            /* 서버로 값 전송 */
                StringBuffer buffer = new StringBuffer();
                buffer.append("plant_name").append("=").append(plant_name);
                OutputStreamWriter outStream = new OutputStreamWriter(http.getOutputStream(), "EUC-KR");
                PrintWriter writer = new PrintWriter(outStream);
                writer.write(buffer.toString());
                writer.flush();
                writer.close();

            /* 서버에서 전송 받기 */
                InputStream inStream = http.getInputStream();
                BufferedReader reader = new BufferedReader(new InputStreamReader(inStream, "UTF-8"));
                result = reader.readLine();

            } catch (MalformedURLException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            }

            if(result.equals("1")) {
                return true;
            } else {
                return false;
            }
        }
    }
}
