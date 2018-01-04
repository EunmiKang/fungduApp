package com.example.seongjun.biocube;

import android.graphics.Bitmap;
import android.widget.ImageButton;

/**
 * Created by Seongjun on 2017. 12. 21..
 */

public class DiaryItem {

    private int diaryNo;
    private String nickname;
    private Bitmap plantImg;
    private String content;


    public DiaryItem(int diaryNo, String nickname, Bitmap plantImg, String content) {
        this.diaryNo = diaryNo;
        this.nickname = nickname;
        this.plantImg = plantImg;
        this.content = content;

    }
    public DiaryItem(int diaryNo, String nickname, String content){
        this.diaryNo = diaryNo;
        this.nickname = nickname;
        this.content = content;
    }

    public String getNickname() {
        return nickname;
    }
    public void setNickName(String nickname) {
        this.nickname = nickname;
    }

    public Bitmap getPlantImg() {
        return plantImg;
    }
    public void setPlantImg(Bitmap plantImg) {
        this.plantImg = plantImg;
    }

    public String getContent() {
        return content;
    }
    public void setContent(String content) {
        this.content = content;
    }
    public int getDiaryNo() {
        return diaryNo;
    }
    public void setDiaryNo(int diaryNo) {
        this.diaryNo = diaryNo;
    }
}