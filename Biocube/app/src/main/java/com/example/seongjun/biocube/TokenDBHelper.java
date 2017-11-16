package com.example.seongjun.biocube;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

/**
 * Created by Eunmi on 2017-11-16.
 */

//sqlite를 사용하기 위한 클래스
public class TokenDBHelper extends SQLiteOpenHelper {
    public TokenDBHelper(Context context) {
        super(context, "tokenDB.db", null, 1);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        //테이블 생성
        db.execSQL("CREATE TABLE TOKEN(token varchar(50) primary key);");
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        //테이블을 지우는 구문을 수행
        db.execSQL("DROP TABLE if exists TOKEN;");
        //테이블 다시 생성
        onCreate(db);
    }
}
