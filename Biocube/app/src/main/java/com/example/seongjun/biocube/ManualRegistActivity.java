package com.example.seongjun.biocube;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;

public class ManualRegistActivity extends AppCompatActivity {

    EditText text_plantName;
    Button btn_selectImage;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_manual_regist);

        text_plantName = (EditText) findViewById(R.id.text_manualRegist_plantName);
        btn_selectImage = (Button) findViewById(R.id.btn_selectManualIamge);

    }


}
