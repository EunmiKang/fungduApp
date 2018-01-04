package com.example.seongjun.biocube;

import android.content.Context;
import android.graphics.Bitmap;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

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

    public DiaryManageAdapter(Context context, String nickname, List<DiaryItem> list, int authority) {
        this.list = list;
        layoutInflater = LayoutInflater.from(context);
        this.authority = authority;
        this.nickname = nickname;
    }
    public DiaryManageAdapter(Context context, List<DiaryItem> list, int authority) {
        this.list = list;
        layoutInflater = LayoutInflater.from(context);
        this.authority = authority;
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
            holder.idTestView = (TextView) view.findViewById(R.id.idTest);
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
        if(authority ==2 || (!nickname.equals("admin")&&!nickname.equals(diaryItem.getNickname()))){
            deleteButton.setVisibility(View.GONE);
        }//전문가 이거나 자기자신의 글이 아니면 삭제버튼이 보이지 않음.
//        holder.deleteButtonView.setId(diaryItem.getDiaryNo());
        holder.plantImgView.setImageBitmap(diaryItem.getPlantImg());
        holder.contentView.setText(diaryItem.getContent());


//        deleteButton.setOnClickListener(new ImageButton.OnClickListener(){//삭제버튼 클릭시
//
//            @Override
//            public void onClick(View v) {
//            }
//        });
        return view;
    }

    class ViewHolder {
        TextView nicknameView;
        ImageView plantImgView;
        TextView contentView;
        ImageButton deleteButtonView;

        TextView idTestView;
    }

}
