package com.example.seongjun.biocube;

import android.graphics.Bitmap;

/**
 * Created by Seongjun on 2017. 12. 21..
 */

public class DiaryItem {

    private int diaryNo;
    private String nickname;
    private Bitmap plantImg;
    private String content;
    private String lastComment;
    private int countComment;
    private String lastCmtNick;
    private String day;


    public DiaryItem(int diaryNo, String nickname, Bitmap plantImg, String content, String lastComment, int countComment, String lastCmtNick, String day) {
        this.diaryNo = diaryNo;
        this.nickname = nickname;
        this.plantImg = plantImg;
        this.content = content;
        this.lastComment = lastComment;
        this.countComment = countComment;
        this.lastCmtNick = lastCmtNick;
        this.day = day;
    }
    public DiaryItem(int diaryNo, String nickname, String content, String lastComment, int countComment, String lastCmtNick, String day){
        this.diaryNo = diaryNo;
        this.nickname = nickname;
        this.content = content;
        this.lastComment = lastComment;
        this.countComment = countComment;
        this.lastCmtNick = lastCmtNick;
        this.day = day;
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

    public String getLastComment() {
        return lastComment;
    }
    public void setLastComment(String lastComment) {
        this.lastComment = lastComment;
    }

    public int getCountComment() {
        return countComment;
    }
    public void setCountComment(int countComment) {
        this.countComment = countComment;
    }

    public String getLastCmtNick() {
        return lastCmtNick;
    }
    public void setLastCmtNick(String lastCmtNick) {
        this.lastCmtNick = lastCmtNick;
    }

    public String getDay() {
        return day;
    }
    public void setDay(String day) {
        this.day = day;
    }
}