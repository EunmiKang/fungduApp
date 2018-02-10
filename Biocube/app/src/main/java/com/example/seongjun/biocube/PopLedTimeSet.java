package com.example.seongjun.biocube;

import android.app.Activity;
import android.os.Bundle;
import android.view.Window;

/**
 * Created by Seongjun on 2018. 2. 9..
 */

public class PopLedTimeSet extends Activity{
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.set_time_led);
    }
}
