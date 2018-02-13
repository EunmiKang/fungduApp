package com.example.seongjun.biocube;

import android.app.Activity;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Color;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.design.widget.TabLayout;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.view.ViewPager;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AlertDialog;
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
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.concurrent.ExecutionException;

import static android.R.attr.color;
import static android.R.attr.dial;
import static android.R.attr.id;

/**
 * Created by Seongjun on 2017. 11. 28..
 */

public class CubeListActivity extends AppCompatActivity {
    String user_id;
    ListView list_cube;
    String[] cubeList;
    ArrayAdapter adapter;

    private TokenDBHelper helper = new TokenDBHelper(this);

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_cube_list);

        list_cube = (ListView) findViewById(R.id.list_cube);
        user_id = getIntent().getStringExtra("id");

        try {
            String[] getList = new ReturnCubeList().execute(user_id).get();
            cubeList = new String[getList.length-1];
            for(int i=0; i<getList.length-1; i++) {
                cubeList[i] = getList[i+1];
            }
            if(cubeList.length == 0) {
                findViewById(R.id.text_nohavecube).setVisibility(View.VISIBLE);
            }
            adapter = new ArrayAdapter(CubeListActivity.this, android.R.layout.simple_list_item_1, cubeList) ;
            list_cube.setAdapter(adapter);
        } catch (InterruptedException e) {
            e.printStackTrace();
        } catch (ExecutionException e) {
            e.printStackTrace();
        }

        if(!user_id.equals("admin")) {
            list_cube.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                @Override
                public void onItemClick(AdapterView parent, View v, int position, long id) {
                    String cubename = (String) parent.getItemAtPosition(position);
                    Intent intent;
                    intent = new Intent(CubeListActivity.this, DiaryAsCubeName.class);
                    intent.putExtra("cubename", cubename);
                    startActivity(intent);

                    // TODO : use strText
                }
            });

            list_cube.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
                @Override
                public boolean onItemLongClick(AdapterView<?> parent, View view, int position, long id) {
                    AlertDialog.Builder builder = new AlertDialog.Builder(CubeListActivity.this);
                    final String cubename = (String) parent.getItemAtPosition(position);
                    builder.setTitle("Cube 삭제 확인");
                    builder.setMessage("해당 큐브를 삭제 하시겠습니까?");
                    builder.setPositiveButton("삭제", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            try {
                                new DeleteCube().execute(cubename).get();
                            } catch (InterruptedException e) {
                                e.printStackTrace();
                            } catch (ExecutionException e) {
                                e.printStackTrace();
                            }
                            list_cube.setAdapter(adapter);
                            ((CubeFragment)((UserMainActivity)UserMainActivity.context).mUserPagerAdapter.getItem(0)).setSpinner();
//                            ((WriteDiaryFragment)((UserMainActivity)UserMainActivity.context).mUserPagerAdapter.getItem(1)).setSpinner();
//                            ((UserPageFragment)((UserMainActivity)UserMainActivity.context).mUserPagerAdapter.getItem(4)).settingCubeNum();
//                            ((UserPageFragment)((UserMainActivity)UserMainActivity.context).mUserPagerAdapter.getItem(4)).settingDiaryNum();
                        }
                    });
                    builder.setNegativeButton("취소", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            dialog.cancel();
                        }
                    });
                    AlertDialog dialog = builder.create();
                    dialog.show();
                    return true;
                }
            });
        }
        else{
//            list_cube.setClickable(false);
            list_cube.setSelector(R.color.white);
            list_cube.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
                @Override
                public boolean onItemLongClick(AdapterView<?> parent, View view, int position, long id) {
                    AlertDialog.Builder builder = new AlertDialog.Builder(CubeListActivity.this);
                    final String cubename = (String) parent.getItemAtPosition(position);
                    builder.setTitle("Cube 삭제 확인");
                    builder.setMessage("해당 큐브를 삭제 하시겠습니까?");
                    builder.setPositiveButton("삭제", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            try {
                                new DeleteCube().execute(cubename).get();
                            } catch (InterruptedException e) {
                                e.printStackTrace();
                            } catch (ExecutionException e) {
                                e.printStackTrace();
                            }
                            list_cube.setAdapter(adapter);
                            ((CubeFragment)((AdminMainActivity)AdminMainActivity.context).mAdminPagerAdapter.getItem(0)).setSpinner();
                            ((AdminPageFragment)((AdminMainActivity)AdminMainActivity.context).mAdminPagerAdapter.getItem(3)).new SettingAdminPage().execute();
                        }
                    });
                    builder.setNegativeButton("취소", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            dialog.cancel();
                        }
                    });
                    AlertDialog dialog = builder.create();
                    dialog.show();
                    return true;
                }
            });
        }
    }
    public class DeleteCube extends AsyncTask<Object,Object,String> {
        // 실제 params 부분에는 execute 함수에서 넣은 인자 값이 들어 있다.
        @Override
        public String doInBackground(Object... params) {
            try {
                String cubename = "";
             /* URL 설정하고 접속 */
                URL url = new URL("http://fungdu0624.phps.kr/biocube/deletecube.php");
                HttpURLConnection http = (HttpURLConnection) url.openConnection();

            /* 전송모드 설정 */
                http.setDefaultUseCaches(false);
                http.setDoInput(true);  //서버에서 읽기 모드로 지정
                http.setDoOutput(true);    //서버에서 쓰기 모드로 지정
                http.setRequestMethod("POST");
                http.setRequestProperty("content-type", "application/x-www-form-urlencoded");   //서버에게 웹에서 <Form>으로 값이 넘어온 것과 같은 방식으로 처리하라는 걸 알려준다

                try {
                    cubename = URLEncoder.encode(params[0].toString(),"UTF-8");
                } catch (UnsupportedEncodingException e) {
                    e.printStackTrace();
                }
            /* 서버로 값 전송 */
                StringBuffer buffer = new StringBuffer();
                buffer.append("user_id").append("=").append(user_id).append("&");
                buffer.append("cubename").append("=").append(cubename);
                OutputStreamWriter outStream = new OutputStreamWriter(http.getOutputStream(), "EUC-KR");
                PrintWriter writer = new PrintWriter(outStream);
                writer.write(buffer.toString());
                writer.flush();
                writer.close();

            /* 서버에서 전송 받기 */
                InputStream inStream = http.getInputStream();
                BufferedReader reader = new BufferedReader(new InputStreamReader(inStream, "UTF-8"));
                String result = reader.readLine();

                return result;

            } catch(MalformedURLException e) {
                e.printStackTrace();
            } catch(IOException e) {
                e.printStackTrace();
            }
            //publishProgress(params);    // 중간 중간에 진행 상태 UI 를 업데이트 하기 위해 사용..
            return null;
        }

        @Override
        public void onPostExecute(String result) {
            super.onPostExecute(result);
            if(result.equals("false")){
                Toast.makeText(CubeListActivity.this, "삭제를 실패했습니다.", Toast.LENGTH_SHORT).show();
            }
            else{
                Toast.makeText(CubeListActivity.this, "삭제되었습니다.", Toast.LENGTH_SHORT).show();
                try {
                    String[] getList = new ReturnCubeList().execute(user_id).get();
                    cubeList = new String[getList.length-1];
                    for(int i=0; i<getList.length-1; i++) {
                        cubeList[i] = getList[i+1];
                    }
                    if(cubeList.length == 0) {
                        findViewById(R.id.text_nohavecube).setVisibility(View.VISIBLE);
                    }
                    adapter = new ArrayAdapter(CubeListActivity.this, android.R.layout.simple_list_item_1, cubeList) ;
                    list_cube.setAdapter(adapter);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                } catch (ExecutionException e) {
                    e.printStackTrace();
                }
            }
        }
    }
}
