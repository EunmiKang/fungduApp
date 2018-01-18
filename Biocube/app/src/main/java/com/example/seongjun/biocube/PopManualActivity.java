package com.example.seongjun.biocube;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.widget.TextView;

public class PopManualActivity extends Activity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_pop_manual);

        Intent intent = getIntent();
        String[] getData = (String[]) intent.getExtras().get("manual");
        TextView textView = (TextView) findViewById(R.id.testText);

        String manualsName = "";
        for(int i=1; i<getData.length; i++) {
            manualsName += (getData[i] + " ");
        }

        textView.setText(manualsName);
    }
}
