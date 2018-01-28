package com.example.seongjun.biocube;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.EditText;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.SimpleAdapter;
import android.widget.TextView;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.w3c.dom.Text;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.concurrent.ExecutionException;

/**
 * Created by Seongjun on 2017. 11. 28..
 */

public class DiaryAsCubeName extends AppCompatActivity{

    String cubename;
    TextView text_diary_cubename;
    String id;
    private TokenDBHelper helper = new TokenDBHelper(this);
    int authority;

    private static final String TAG_DIARY="diaryinfo";
    private static final String TAG_NICKNAME = "nickname";
    private static final String TAG_IMG = "img";
    private static final String TAG_CONTENT ="content";
    private static final String TAG_DIARYNO = "diaryNo";
    private static final String TAG_LASTCOMMENT = "lastComment";
    private static final String TAG_COUNTCOMMENT = "countComment";
    private static final String TAG_LASTCOMMENTNICK = "lastCmtNick";


    JSONArray diary = null;



    ListView list_diary_cubename;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_diary_cubename);

        Intent intent = getIntent();
        cubename = intent.getExtras().getString("cubename");
        text_diary_cubename = (TextView) findViewById(R.id.text_diary_cubename);
        text_diary_cubename.setText(cubename);
        try{
            cubename = URLEncoder.encode(cubename,"UTF-8");
        } catch(Exception e) {
            e.printStackTrace();
        }
        list_diary_cubename = (ListView) findViewById(R.id.list_diary_cubename);

        try {
            id = new GetId().execute(getApplicationContext()).get();
        } catch (InterruptedException e) {
            e.printStackTrace();
        } catch (ExecutionException e) {
            e.printStackTrace();
        }
        try {
            String[] userInfo = new GetUserInfo().execute(helper).get();
            if (userInfo[1].equals("1")) {
                authority = 1;
            } else if (userInfo[1].equals("2")) {
                authority = 2;
            } else {
                authority = 0;
            }

        } catch (InterruptedException e) {
            e.printStackTrace();
        } catch (ExecutionException e) {
            e.printStackTrace();
        }

        getData("http://fungdu0624.phps.kr/biocube/getdiarycubename.php");

    }

    public void getData(String url){
        class GetDataJSON extends AsyncTask<String, Void, List<DiaryItem>> {

            ProgressDialog asyncDialog = new ProgressDialog(DiaryAsCubeName.this);

            @Override
            protected List<DiaryItem> doInBackground(String... params) {

                String uri = params[0];
                List<DiaryItem> diarylist = new ArrayList<DiaryItem>();

                BufferedReader bufferedReader = null;
                try {
                    URL url = new URL(uri);
                    HttpURLConnection con = (HttpURLConnection) url.openConnection();
                    con.setDefaultUseCaches(false);
                    con.setDoInput(true);  //서버에서 읽기 모드로 지정
                    con.setDoOutput(true);    //서버에서 쓰기 모드로 지정
                    con.setRequestMethod("POST");
                    con.setRequestProperty("content-type", "application/x-www-form-urlencoded");   //서버에게 웹에서 <Form>으로 값이 넘어온 것과 같은 방식으로 처리하라는 걸 알려준다

            /* 서버로 값 전송 */
                    StringBuffer buffer = new StringBuffer();
                    buffer.append("user_id").append("=").append(id).append("&");
                    buffer.append("cubename").append("=").append(cubename);
                    OutputStreamWriter outStream = new OutputStreamWriter(con.getOutputStream(), "EUC-KR");
                    PrintWriter writer = new PrintWriter(outStream);
                    writer.write(buffer.toString());
                    writer.flush();
                    writer.close();

                    bufferedReader = new BufferedReader(new InputStreamReader(con.getInputStream()));
                    StringBuilder sb = new StringBuilder();

                    String json;
                    while((json = bufferedReader.readLine())!= null){
                        sb.append(json+"\n");
                    }

                    try {
                        JSONObject jsonObj = new JSONObject(sb.toString().trim());
                        diary = jsonObj.getJSONArray(TAG_DIARY);
                        Bitmap plantImg;

                        for (int i = 0; i < diary.length(); i++) {
                            JSONObject c = diary.getJSONObject(i);
                            String nickname = c.getString(TAG_NICKNAME);
                            String img = c.getString(TAG_IMG);
                            String content = c.getString(TAG_CONTENT);
                            int diaryNo = c.getInt(TAG_DIARYNO);
                            String lastComment = c.getString(TAG_LASTCOMMENT);
                            int countComment = c.getInt(TAG_COUNTCOMMENT);
                            String lastCmtNick = c.getString(TAG_LASTCOMMENTNICK);

                            if(lastComment.equals("null")){
                                lastComment = "댓글이 없습니다.";
                                lastCmtNick = "";
                            }
                            if(!img.equals("null")) {
                                String readURL = "http://fungdu0624.phps.kr/biocube/users/" + id + "/" + img;
                                HttpURLConnection http = (HttpURLConnection) url.openConnection();
                                url = new URL(readURL);
                                http = (HttpURLConnection) url.openConnection();
                                http.connect();
                                //스트림생성
                                InputStream inStream = http.getInputStream();
                                //스트림에서 받은 데이터를 비트맵 변환
                                BitmapFactory.Options option = new BitmapFactory.Options();
                                option.inSampleSize = 2;
                                plantImg = BitmapFactory.decodeStream(inStream,null,option);
                                diarylist.add(new DiaryItem(diaryNo, nickname, plantImg, content, lastComment, countComment, lastCmtNick));
                            }
                            else{
                                plantImg = null;
                                diarylist.add(new DiaryItem(diaryNo, nickname,plantImg,content, lastComment, countComment, lastCmtNick));
                            }
                        }
                    }catch (JSONException e) {
                        e.printStackTrace();
                    }
                    return diarylist;
                }catch(Exception e){
                    return null;
                }

            }

            @Override
            protected void onPreExecute() {
                asyncDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
                asyncDialog.setMessage("로딩중입니다..");

                // show dialog
                asyncDialog.show();
                super.onPreExecute();
            }
            @Override
            protected void onPostExecute(List<DiaryItem> result){
                list_diary_cubename.setAdapter(new DiaryManageAdapter(DiaryAsCubeName.this, result, authority, id));
                asyncDialog.dismiss();
            }
        }
        GetDataJSON g = new GetDataJSON();
        g.execute(url);
    }
}
