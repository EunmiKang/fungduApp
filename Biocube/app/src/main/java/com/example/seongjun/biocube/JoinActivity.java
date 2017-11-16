package com.example.seongjun.biocube;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.RadioButton;
import android.widget.RadioGroup;

public class JoinActivity extends AppCompatActivity {

    final RadioGroup rg = (RadioGroup)findViewById(R.id.group_authority);
    int id = rg.getCheckedRadioButtonId();
    //getCheckedRadioButtonId() 의 리턴값은 선택된 RadioButton 의 id 값.
    RadioButton rb = (RadioButton) findViewById(id);

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_join);

        }

    public void joinProcess(View view) {

    }
}
