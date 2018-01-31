package com.example.seongjun.biocube;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ListView;
import android.widget.TextView;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.w3c.dom.Text;

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

/**
 * Created by Seongjun on 2018. 1. 31..
 */

public class CommentAsDiaryActivity extends AppCompatActivity {

    private ListView list_comment;
    private String id;
    private String diaryNo;
    private String content;
    private TextView text_diaryOwner;
    private TextView text_diaryContent;
    private JSONArray comments = null;
    private ListViewAdapter listAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_comment);

        list_comment = (ListView) findViewById(R.id.list_commentAsDiary);
        Intent intent = getIntent();
        id = intent.getExtras().getString("id");
        diaryNo = intent.getExtras().getString("diaryNo");
        content = intent.getExtras().getString("content");
        text_diaryOwner = (TextView) findViewById(R.id.text_diaryOwner);
        text_diaryContent = (TextView) findViewById(R.id.text_diaryContent);
        text_diaryOwner.setText(id);
        text_diaryContent.setText(content);

        listAdapter = new ListViewAdapter();

        new returnCommentList().execute();
    }

    public class ListViewAdapter extends BaseAdapter {
        // Adapter에 추가된 데이터를 저장하기 위한 ArrayList
        private ArrayList<ListViewItem> listViewItemList = new ArrayList<ListViewItem>();

        // ListViewAdapter의 생성자
        public ListViewAdapter() {

        }

        // Adapter에 사용되는 데이터의 개수를 리턴. : 필수 구현
        @Override
        public int getCount() {
            return listViewItemList.size();
        }

        // position에 위치한 데이터를 화면에 출력하는데 사용될 View를 리턴. : 필수 구현
        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            final int pos = position;
            final Context context = parent.getContext();

            // "listview_item" Layout을 inflate하여 convertView 참조 획득.
            if (convertView == null) {
                LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
                convertView = inflater.inflate(R.layout.custom_comment_item2, parent, false);
            }

            // 화면에 표시될 View(Layout이 inflate된)으로부터 위젯에 대한 참조 획득
            TextView nicknameTextView = (TextView) convertView.findViewById(R.id.text_cmtNick);
            TextView plantTextView = (TextView) convertView.findViewById(R.id.text_cmt);

            // Data Set(listViewItemList)에서 position에 위치한 데이터 참조 획득
            ListViewItem listViewItem = listViewItemList.get(position);

            // 아이템 내 각 위젯에 데이터 반영
            nicknameTextView.setText(listViewItem.getNickname());
            plantTextView.setText(listViewItem.getComment());

            return convertView;
        }

        // 지정한 위치(position)에 있는 데이터와 관계된 아이템(row)의 ID를 리턴. : 필수 구현
        @Override
        public long getItemId(int position) {
            return position;
        }

        // 지정한 위치(position)에 있는 데이터 리턴 : 필수 구현
        @Override
        public Object getItem(int position) {
            return listViewItemList.get(position);
        }

        // 아이템 데이터 추가를 위한 함수. 개발자가 원하는대로 작성 가능.
        public void addItem(String nickname, String comment) {
            ListViewItem item = new ListViewItem(nickname, comment);

            listViewItemList.add(item);
        }
    }

    public class ListViewItem {
        private String nickname;
        private String comment;

        public ListViewItem(String nickname, String comment) {
            this.nickname = nickname;
            this.comment = comment;
        }

        public void setNickname(String nickname) {
            this.nickname = nickname;
        }

        public void setComment(String comment) {
            this.comment = comment;
        }

        public String getNickname() {
            return this.nickname;
        }

        public String getComment() {
            return this.comment;
        }
    }

    public class returnCommentList extends AsyncTask<String, Object, Integer> {

        @Override
        protected Integer doInBackground(String... params) {
            try {
                /* URL 설정하고 접속 */
                URL url = new URL("http://fungdu0624.phps.kr/biocube/getcommentlist.php");
                HttpURLConnection http = (HttpURLConnection) url.openConnection();

                /* 전송모드 설정 */
                http.setDefaultUseCaches(false);
                http.setDoInput(true);  //서버에서 읽기 모드로 지정
                http.setDoOutput(true);    //서버에서 쓰기 모드로 지정
                http.setRequestMethod("POST");
                http.setRequestProperty("content-type", "application/x-www-form-urlencoded");   //서버에게 웹에서 <Form>으로 값이 넘어온 것과 같은 방식으로 처리하라는 걸 알려준다

                /* 서버로 값 전송 */
                StringBuffer buffer = new StringBuffer();
                buffer.append("diaryNo").append("=").append(diaryNo);

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
                    comments = jsonObj.getJSONArray("commentlist");
                    Bitmap plantImg;


                    for (int i = 0; i < comments.length(); i++) {
                        JSONObject c = comments.getJSONObject(i);
                        String nickname = c.getString("nickname");
                        String comment = c.getString("comment");

                        listAdapter.addItem(nickname, comment);
                    }
                }catch (JSONException e) {
                    e.printStackTrace();
                }            } catch (MalformedURLException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            }

            return -1;
        }

        @Override
        public void onPostExecute(Integer result) {
            super.onPostExecute(result);
            // Todo: doInBackground() 메소드 작업 끝난 후 처리해야할 작업..
            list_comment.setAdapter(listAdapter);
        }

    }

}
