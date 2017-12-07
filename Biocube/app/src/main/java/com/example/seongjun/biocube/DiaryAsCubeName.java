package com.example.seongjun.biocube;

import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.SimpleAdapter;
import android.widget.TextView;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.w3c.dom.Text;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;

/**
 * Created by Seongjun on 2017. 11. 28..
 */

public class DiaryAsCubeName extends AppCompatActivity{

    String cubename;
    TextView text_diary_cubename;

    String myJSON;

    private static final String TAG_RESULTS="result";
    private static final String TAG_NICKNAME = "nickname";
    private static final String TAG_IMG = "img";
    private static final String TAG_CONTENT ="content";

    JSONArray diary = null;

    ArrayList<HashMap<String, String>> diaryList;

    ListView list_diary_cubename;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_diary_cubename);

        Intent intent = getIntent();
        cubename = intent.getExtras().getString("cubename");
        text_diary_cubename = (TextView) findViewById(R.id.text_diary_cubename);
        text_diary_cubename.setText(cubename);
        list_diary_cubename = (ListView) findViewById(R.id.list_diary_cubename);
        diaryList = new ArrayList<HashMap<String,String>>();
        getData("http://fungdu0624.phps.kr/biocube/getdiarycubename.php");

    }

    protected void showList(){
        try {
            JSONObject jsonObj = new JSONObject(myJSON);
            diary = jsonObj.getJSONArray(TAG_RESULTS);

            for(int i=0;i<diary.length();i++){
                JSONObject c = diary.getJSONObject(i);
                String nickname = c.getString(TAG_NICKNAME);
                String img = c.getString(TAG_IMG);
                String content = c.getString(TAG_CONTENT);

                HashMap<String,String> diarys = new HashMap<String,String>();

                diarys.put(TAG_NICKNAME,nickname);
                diarys.put(TAG_IMG,img);
                diarys.put(TAG_CONTENT,content);

                diaryList.add(diarys);
            }

            ListAdapter adapter = new SimpleAdapter(
                    DiaryAsCubeName.this, diaryList, R.layout.custom_newspeed,
                    new String[]{TAG_NICKNAME,TAG_IMG,TAG_CONTENT},
                    new int[]{R.id.id, R.id.name, R.id.address}
            );

            list_diary_cubename.setAdapter(adapter);

        } catch (JSONException e) {
            e.printStackTrace();
        }

    }

    public void getData(String url){
        class GetDataJSON extends AsyncTask<String, Void, String> {

            @Override
            protected String doInBackground(String... params) {

                String uri = params[0];

                BufferedReader bufferedReader = null;
                try {
                    URL url = new URL(uri);
                    HttpURLConnection con = (HttpURLConnection) url.openConnection();
                    StringBuilder sb = new StringBuilder();

                    bufferedReader = new BufferedReader(new InputStreamReader(con.getInputStream()));

                    String json;
                    while((json = bufferedReader.readLine())!= null){
                        sb.append(json+"\n");
                    }

                    return sb.toString().trim();

                }catch(Exception e){
                    return null;
                }



            }

            @Override
            protected void onPostExecute(String result){
                myJSON=result;
                showList();
            }
        }
        GetDataJSON g = new GetDataJSON();
        g.execute(url);
    }
}
