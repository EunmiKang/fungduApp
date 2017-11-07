package com.example.seongjun.biocube;


import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.content.Intent;
import android.view.View;

/**
 * Created by Seongjun on 2017. 11. 1..
 */

public class LoginActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
    }

    /* login 버튼 클릭했을 때 */
    public void loginProcess(View view) {
        Intent intent = new Intent(LoginActivity.this, UserMainActivity.class);
        startActivity(intent);
    }

    /* 회원가입 클릭했을 때 */
    public void goJoin(View view) {
        Intent intent = new Intent(LoginActivity.this, JoinActivity.class);
        startActivity(intent);
    }
}
