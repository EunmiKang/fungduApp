package com.example.seongjun.biocube;

/**
 * Created by Eunmi on 2017-10-23.
 */

import android.annotation.SuppressLint;
import android.os.StrictMode;

public class NetworkUtil {
    @SuppressLint("NewApi")
    static public void setNetworkPolicy() {
        if (android.os.Build.VERSION.SDK_INT > 9) {
            StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();
            StrictMode.setThreadPolicy(policy);
        }
    }
}
