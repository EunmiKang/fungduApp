package com.example.seongjun.biocube;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.os.AsyncTask;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.ListView;
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
import java.util.ArrayList;
import java.util.concurrent.ExecutionException;

public class CommentListActivity extends AppCompatActivity {
    private String id;
    private ListViewAdapter listAdapter;
    private ListView comment_listView;
    private TokenDBHelper helper = new TokenDBHelper(this);

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_comment_list);

        id = getIntent().getStringExtra("id");
        comment_listView = (ListView) findViewById(R.id.list_comment);
        comment_listView.setOnItemClickListener(commentItemClickListener);
        showComments();
    }

    public void showComments() {
        listAdapter = new ListViewAdapter();
        try {
            new returnCommentList().execute().get();
        } catch (InterruptedException e) {
            e.printStackTrace();
        } catch (ExecutionException e) {
            e.printStackTrace();
        }
    }

    ListView.OnItemClickListener commentItemClickListener = new ListView.OnItemClickListener() {

        @Override
        public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
            final String commentNo = ((TextView)view.findViewById(R.id.hidden_commentNo)).getText().toString();
            AlertDialog.Builder builder = new AlertDialog.Builder(CommentListActivity.this)
                    .setMessage("선택한 코멘트를 삭제하시겠습니까?")
                    .setPositiveButton("네", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            deleteComment(commentNo);
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
    };

    public void deleteComment(String commentNo) {
        /* 디비에서 삭제 */
        Boolean result = false;
        try {
            result = new deleteCommentTask().execute(commentNo).get();
        } catch (InterruptedException e) {
            e.printStackTrace();
        } catch (ExecutionException e) {
            e.printStackTrace();
        }
        if(result) {
            /* My Comment 페이지 업데이트 */
            showComments();

            /* 전문가일 경우 마이페이지 코멘트 수 업데이트 */
            if(!id.equals("admin")) {
                ((ExpertPageFragment)((ExpertMainActivity)ExpertMainActivity.mContext).mExpertPagerAdapter.getItem(1)).setCommentNum();
            }

            Toast.makeText(CommentListActivity.this, "삭제되었습니다.", Toast.LENGTH_SHORT).show();
        } else {
            Toast.makeText(CommentListActivity.this, "삭제를 실패하였습니다.", Toast.LENGTH_SHORT).show();
        }
    }

    public class deleteCommentTask extends AsyncTask<String, Object, Boolean> {
        @Override
        public Boolean doInBackground(String... params) {
            String result = "-1";
            ArrayList<String> imageNameList = new ArrayList<>();
            try {
            /* parameter로 받은 것들 저장 */
                String commentNo = params[0];

             /* URL 설정하고 접속 */
                URL url = new URL("http://fungdu0624.phps.kr/biocube/deleteComment.php");
                HttpURLConnection http = (HttpURLConnection) url.openConnection();

            /* 전송모드 설정 */
                http.setDefaultUseCaches(false);
                http.setDoInput(true);  //서버에서 읽기 모드로 지정
                http.setDoOutput(true);    //서버에서 쓰기 모드로 지정
                http.setRequestMethod("POST");
                http.setRequestProperty("content-type", "application/x-www-form-urlencoded");   //서버에게 웹에서 <Form>으로 값이 넘어온 것과 같은 방식으로 처리하라는 걸 알려준다

            /* 서버로 값 전송 */
                StringBuffer buffer = new StringBuffer();
                buffer.append("comment_no").append("=").append(commentNo);
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

    public class returnCommentList extends AsyncTask<String,Object,Integer> {

        @Override
        protected Integer doInBackground(String... params) {
            try {
                /* URL 설정하고 접속 */
                URL url = new URL("http://fungdu0624.phps.kr/biocube/returnComment.php");
                HttpURLConnection http = (HttpURLConnection) url.openConnection();

                /* 전송모드 설정 */
                http.setDefaultUseCaches(false);
                http.setDoInput(true);  //서버에서 읽기 모드로 지정
                http.setDoOutput(true);    //서버에서 쓰기 모드로 지정
                http.setRequestMethod("POST");
                http.setRequestProperty("content-type", "application/x-www-form-urlencoded");   //서버에게 웹에서 <Form>으로 값이 넘어온 것과 같은 방식으로 처리하라는 걸 알려준다

                /* 서버로 값 전송 */
                StringBuffer buffer = new StringBuffer();
                buffer.append("user_id").append("=").append(id);

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

                inStream.close();
                http.disconnect();

                try {
                    JSONObject jsonObj = new JSONObject(sb.toString().trim());
                    JSONArray commentArray = jsonObj.getJSONArray("comment_array");
                    for (int i = 0; i < commentArray.length(); i++) {
                        JSONObject manualObject = commentArray.getJSONObject(i);
                        listAdapter.addItem(manualObject.getString("writer_nickname"), manualObject.getString("plant_name"), manualObject.getString("comment"), manualObject.getString("commentNo"));
                    }
                }catch (JSONException e) {
                    e.printStackTrace();
                }

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
            if(listAdapter.getCount() == 0) {
                findViewById(R.id.text_nohavecomment).setVisibility(View.VISIBLE);
            }
            comment_listView.setAdapter(listAdapter);
        }
    }

    public class ListViewAdapter extends BaseAdapter {
        // Adapter에 추가된 데이터를 저장하기 위한 ArrayList
        private ArrayList<CommentItem> commentItemList = new ArrayList<>() ;

        // ListViewAdapter의 생성자
        public ListViewAdapter() {

        }

        // Adapter에 사용되는 데이터의 개수를 리턴. : 필수 구현
        @Override
        public int getCount() {
            return commentItemList.size() ;
        }

        // position에 위치한 데이터를 화면에 출력하는데 사용될 View를 리턴. : 필수 구현
        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            final int pos = position;
            final Context context = parent.getContext();

            // "listview_item" Layout을 inflate하여 convertView 참조 획득.
            if (convertView == null) {
                LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
                convertView = inflater.inflate(R.layout.custom_comment_item, parent, false);
            }

            // 화면에 표시될 View(Layout이 inflate된)으로부터 위젯에 대한 참조 획득
            TextView nicknameTextView = (TextView) convertView.findViewById(R.id.text_comment_nick) ;
            TextView plantTextView = (TextView) convertView.findViewById(R.id.text_comment_plant) ;
            TextView commentTextView = (TextView) convertView.findViewById(R.id.text_comment_comment);
            TextView commentNoTextView = (TextView) convertView.findViewById(R.id.hidden_commentNo);

            // Data Set(commentItemList)에서 position에 위치한 데이터 참조 획득
            CommentItem commentItem = commentItemList.get(position);

            // 아이템 내 각 위젯에 데이터 반영
            nicknameTextView.setText("일지 작성자 : " + commentItem.getNickname());
            plantTextView.setText("식물 종류 : " + commentItem.getPlant());
            commentTextView.setText(commentItem.getComment());
            commentNoTextView.setText(commentItem.getCommentNo());

            return convertView;
        }

        // 지정한 위치(position)에 있는 데이터와 관계된 아이템(row)의 ID를 리턴. : 필수 구현
        @Override
        public long getItemId(int position) {
            return position ;
        }

        // 지정한 위치(position)에 있는 데이터 리턴 : 필수 구현
        @Override
        public Object getItem(int position) {
            return commentItemList.get(position) ;
        }

        // 아이템 데이터 추가를 위한 함수. 개발자가 원하는대로 작성 가능.
        public void addItem(String nickname, String plant, String comment, String commentNo) {
            CommentItem item = new CommentItem(nickname, plant, comment, commentNo);

            commentItemList.add(item);
        }
    }

    public class CommentItem {
        private String nickname ;
        private String plant ;
        private String comment;
        private String commentNo;

        public CommentItem(String nickname, String plant, String comment, String commentNo) {
            this.nickname = nickname;
            this.plant = plant;
            this.comment = comment;
            this.commentNo = commentNo;
        }

        public void setNickname(String nickname) {
            this.nickname = nickname;
        }
        public void setPlant(String plant) {
            this.plant = plant ;
        }
        public void setComment(String comment) { this.comment = comment; }
        public void setCommentNo(String commentNo) { this.commentNo = commentNo; }

        public String getNickname() {
            return this.nickname ;
        }
        public String getPlant() {
            return this.plant ;
        }
        public String getComment() { return this.comment ; }
        public String getCommentNo() { return this.commentNo; }
    }

}
