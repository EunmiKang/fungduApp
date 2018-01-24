package com.example.seongjun.biocube;

import android.content.Context;
import android.graphics.Bitmap;
import android.os.AsyncTask;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
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
import java.util.List;
import java.util.concurrent.ExecutionException;

/**
 * Created by Seongjun on 2017. 12. 21..
 */

public class DiaryManageAdapter extends BaseAdapter{
    List<DiaryItem> list;
    int authority;
    private LayoutInflater layoutInflater;
    ImageButton deleteButton;
    String nickname = "adminNick";
    Context context;
    Button btn_registComment;
    EditText cmt_edit;
    View views;
    String id;

//    TextView text_cmtNick;
    public DiaryManageAdapter(Context context, String nickname, List<DiaryItem> list, int authority, String id) {
        this.list = list;
        layoutInflater = LayoutInflater.from(context);
        this.authority = authority;
        this.nickname = nickname;
        this.context = context;
        this.id = id;
    }
    public DiaryManageAdapter(Context context, List<DiaryItem> list, int authority, String id) {
        this.list = list;
        layoutInflater = LayoutInflater.from(context);
        this.authority = authority;
        this.context = context;
        this.id = id;
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
        this.views=view;
        if (view == null) {
            view = layoutInflater.inflate(R.layout.custom_newspeed, null);

            holder = new ViewHolder();
            holder.nicknameView = (TextView) view.findViewById(R.id.nickname_text);
            holder.plantImgView = (ImageView) view.findViewById(R.id.diaryimg_image);
            holder.contentView = (TextView) view.findViewById(R.id.content_text);
            holder.deleteButtonView = (ImageButton) view.findViewById(R.id.btn_deleteDiary);
            holder.hiddenDiaryNo = (TextView) view.findViewById(R.id.hidden_diaryNo);
            holder.registButtonView = (Button) view.findViewById(R.id.btn_registComment);
            holder.commentView = (TextView) view.findViewById(R.id.comment_text);
            holder.commentCountView = (TextView) view.findViewById(R.id.cmtCount_text);
            holder.cmtNicknameView = (TextView) view.findViewById(R.id.text_cmtNick);
            deleteButton = (ImageButton) view.findViewById(R.id.btn_deleteDiary);
            btn_registComment = (Button) view.findViewById(R.id.btn_registComment);

            cmt_edit = (EditText) view.findViewById(R.id.cmt_edit);
            view.setTag(holder);
        } else {
            holder = (ViewHolder) view.getTag();
        }
        if(authority == 1) {
            EditText cmt_edit = (EditText) view.findViewById(R.id.cmt_edit);
            cmt_edit.setVisibility(View.GONE);
        }
        DiaryItem diaryItem = this.list.get(position);
        holder.nicknameView.setText(diaryItem.getNickname());
        holder.hiddenDiaryNo.setText(String.valueOf(diaryItem.getDiaryNo()));
        holder.cmtNicknameView.setText(diaryItem.getLastCmtNick());
        holder.commentView.setText(diaryItem.getLastComment());
        holder.commentCountView.setText("등 "+diaryItem.getCountComment()+"개");
        final String hiddenNo = holder.hiddenDiaryNo.getText().toString();
        cmt_edit.setTag(hiddenNo);

        deleteButton.setOnClickListener(new View.OnClickListener() {//삭제버튼 눌렀을 때
            @Override
            public void onClick(View v) {
                View v_grandParent = (View) v.getParent().getParent();
                TextView hiddenDiaryNo = (TextView) v_grandParent.findViewById(R.id.hidden_diaryNo);
                String hiddenNo = hiddenDiaryNo.getText().toString();
                new DeleteDiary().execute(hiddenNo);
            }
        });

        btn_registComment.setOnClickListener(new View.OnClickListener() {//등록버튼을 눌렀을 때,
            @Override
            public void onClick(View v) {
                View v_parent =(View) v.getParent();
                EditText edit_comment = (EditText) v_parent.findViewWithTag(hiddenNo);
//                Toast.makeText(context, test.getText().toString() ,Toast.LENGTH_SHORT).show();
                String comment = edit_comment.getText().toString();
                View v_grandParent = (View) v.getParent().getParent();
                TextView textDiaryNo = (TextView) v_grandParent.findViewById(R.id.hidden_diaryNo);
                String diaryNo = textDiaryNo.getText().toString();
                try {
                    comment = URLEncoder.encode(comment,"UTF-8");
                    String result = new RegistComment().execute(comment,diaryNo, id).get();//댓글 성공여부를 string으로 리턴.
                    if(result.equals("comment_success")) {
                        Toast.makeText(context, "등록에 성공하였습니다.", Toast.LENGTH_SHORT).show();
                        edit_comment.setText("");
                    }
                    else{
                        Toast.makeText(context, "등록에 실패하였습니다.", Toast.LENGTH_SHORT).show();
                        edit_comment.setText("");
                    }
                } catch (UnsupportedEncodingException e) {
                    e.printStackTrace();
                } catch (InterruptedException e) {
                    e.printStackTrace();
                } catch (ExecutionException e) {
                    e.printStackTrace();
                }
            }
        });

        if(authority ==2 || (!nickname.equals("adminNick")&&!nickname.equals(diaryItem.getNickname()))){
            deleteButton.setVisibility(View.GONE);
        }//전문가 이거나 자기자신의 글이 아니면 삭제버튼이 보이지 않음.
        if(authority == 1){
            btn_registComment.setVisibility(View.GONE);
        }
//        holder.deleteButtonView.setId(diaryItem.getDiaryNo());
        holder.plantImgView.setImageBitmap(diaryItem.getPlantImg());
        holder.contentView.setText(diaryItem.getContent());

        return view;
    }

    class ViewHolder {
        TextView nicknameView;
        ImageView plantImgView;
        TextView contentView;
        ImageButton deleteButtonView;
        TextView hiddenDiaryNo;
        Button registButtonView;
        TextView commentView;
        TextView commentCountView;
        TextView cmtNicknameView;
    }
    class DeleteDiary extends AsyncTask<String, Void, String> {

        @Override
        protected String doInBackground(String... params) {
            String result = "";
            try {
         /* URL 설정하고 접속 */
                URL url = new URL("http://fungdu0624.phps.kr/biocube/deleteDiary.php");
                HttpURLConnection http = (HttpURLConnection) url.openConnection();

        /* 전송모드 설정 */
                http.setDefaultUseCaches(false);
                http.setDoInput(true);  //서버에서 읽기 모드로 지정
                http.setDoOutput(true);    //서버에서 쓰기 모드로 지정
                http.setRequestMethod("POST");
                http.setRequestProperty("content-type", "application/x-www-form-urlencoded");   //서버에게 웹에서 <Form>으로 값이 넘어온 것과 같은 방식으로 처리하라는 걸 알려준다

        /* 서버로 값 전송 */
                StringBuffer buffer = new StringBuffer();
                buffer.append("diaryNo").append("=").append(params[0].toString());


                OutputStreamWriter outStream = new OutputStreamWriter(http.getOutputStream(), "EUC-KR");
                PrintWriter writer = new PrintWriter(outStream);
                writer.write(buffer.toString());
                writer.flush();
                writer.close();

        /* 서버에서 전송 받기 */
                InputStream inStream = http.getInputStream();
                BufferedReader reader = new BufferedReader(new InputStreamReader(inStream, "UTF-8"));
                result = reader.readLine();


            } catch(MalformedURLException e) {
                e.printStackTrace();
            } catch(IOException e) {
                e.printStackTrace();
            }
            return result;
        }

        @Override
        protected void onPostExecute(String result){
            if(result.equals("success")) {
                Toast.makeText(context, "성공적으로 삭제하였습니다.", Toast.LENGTH_SHORT).show();
            }
            else{
                Toast.makeText(context, "삭제를 실패하였습니다.", Toast.LENGTH_SHORT).show();
            }
        }
    }

    class RegistComment extends AsyncTask<String, Void, String> {

        @Override
        protected String doInBackground(String... params) {
            String result = "";
            try {
         /* URL 설정하고 접속 */
                URL url = new URL("http://fungdu0624.phps.kr/biocube/registComment.php");
                HttpURLConnection http = (HttpURLConnection) url.openConnection();

        /* 전송모드 설정 */
                http.setDefaultUseCaches(false);
                http.setDoInput(true);  //서버에서 읽기 모드로 지정
                http.setDoOutput(true);    //서버에서 쓰기 모드로 지정
                http.setRequestMethod("POST");
                http.setRequestProperty("content-type", "application/x-www-form-urlencoded");   //서버에게 웹에서 <Form>으로 값이 넘어온 것과 같은 방식으로 처리하라는 걸 알려준다

        /* 서버로 값 전송 */
                StringBuffer buffer = new StringBuffer();
                buffer.append("diaryNo").append("=").append(params[1]).append("&");
                buffer.append("comment").append("=").append(params[0].toString()).append("&");
                buffer.append("user_id").append("=").append(params[2].toString());


                OutputStreamWriter outStream = new OutputStreamWriter(http.getOutputStream(), "EUC-KR");
                PrintWriter writer = new PrintWriter(outStream);
                writer.write(buffer.toString());
                writer.flush();
                writer.close();

        /* 서버에서 전송 받기 */
                InputStream inStream = http.getInputStream();
                BufferedReader reader = new BufferedReader(new InputStreamReader(inStream, "UTF-8"));
                result = reader.readLine();


            } catch(MalformedURLException e) {
                e.printStackTrace();
            } catch(IOException e) {
                e.printStackTrace();
            }
            return result;
        }

        @Override
        protected void onPostExecute(String result){
        }
    }

}
