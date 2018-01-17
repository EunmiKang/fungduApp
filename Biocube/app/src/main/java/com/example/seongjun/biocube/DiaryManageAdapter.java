package com.example.seongjun.biocube;

import android.content.Context;
import android.graphics.Bitmap;
import android.os.AsyncTask;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
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
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.List;

/**
 * Created by Seongjun on 2017. 12. 21..
 */

public class DiaryManageAdapter extends BaseAdapter{
    List<DiaryItem> list;
    int authority;
    private LayoutInflater layoutInflater;
    ImageButton deleteButton;
    String nickname = "admin";
    Context context;

    public DiaryManageAdapter(Context context, String nickname, List<DiaryItem> list, int authority) {
        this.list = list;
        layoutInflater = LayoutInflater.from(context);
        this.authority = authority;
        this.nickname = nickname;
        this.context = context;
    }
    public DiaryManageAdapter(Context context, List<DiaryItem> list, int authority) {
        this.list = list;
        layoutInflater = LayoutInflater.from(context);
        this.authority = authority;
        this.context = context;
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
            view = layoutInflater.inflate(R.layout.custom_newspeed, null);

            holder = new ViewHolder();
            holder.nicknameView = (TextView) view.findViewById(R.id.nickname_text);
            holder.plantImgView = (ImageView) view.findViewById(R.id.diaryimg_image);
            holder.contentView = (TextView) view.findViewById(R.id.content_text);
            holder.deleteButtonView = (ImageButton) view.findViewById(R.id.btn_deleteDiary);
            holder.hiddenDiaryNo = (TextView) view.findViewById(R.id.hidden_diaryNo);
            deleteButton = (ImageButton) view.findViewById(R.id.btn_deleteDiary);

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
        final String hiddenNo = holder.hiddenDiaryNo.getText().toString();

        deleteButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                new DeleteDiary().execute(hiddenNo);
            }
        });
        if(authority ==2 || (!nickname.equals("admin")&&!nickname.equals(diaryItem.getNickname()))){
            deleteButton.setVisibility(View.GONE);
        }//전문가 이거나 자기자신의 글이 아니면 삭제버튼이 보이지 않음.
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

}
