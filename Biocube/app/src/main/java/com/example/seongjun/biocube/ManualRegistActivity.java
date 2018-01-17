package com.example.seongjun.biocube;

import android.Manifest;
import android.content.pm.PackageManager;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.List;

public class ManualRegistActivity extends AppCompatActivity {

    EditText text_plantName;
    Button btn_selectImage;

    private String[] permissions = {Manifest.permission.READ_EXTERNAL_STORAGE, Manifest.permission.WRITE_EXTERNAL_STORAGE}; //권한 설정 변수
    private static final int MULTIPLE_PERMISSIONS = 101; //권한 동의 여부 문의 후 CallBack 함수에 쓰일 변수

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_manual_regist);

        text_plantName = (EditText) findViewById(R.id.text_manualRegist_plantName);
        btn_selectImage = (Button) findViewById(R.id.btn_selectManualIamge);

        checkPermissions();

        btn_selectImage.setOnClickListener(selectImageClickListener);
    }

    /**
     * 사용자에게 권한 묻기
     */
    private boolean checkPermissions() {
        int result;
        List<String> permissionList = new ArrayList<>();
        for (String pm : permissions) {
            result = ContextCompat.checkSelfPermission(ManualRegistActivity.this, pm);
            if (result != PackageManager.PERMISSION_GRANTED) {  //사용자가 해당 권한을 가지고 있지 않을 경우 리스트에 해당 권한명 추가
                permissionList.add(pm);
            }
        }
        if (!permissionList.isEmpty()) {    //권한이 추가되었으면 해당 리스트가 empty가 아니므로 권한을 request(요청)함
            ActivityCompat.requestPermissions(ManualRegistActivity.this, permissionList.toArray(new String[permissionList.size()]), MULTIPLE_PERMISSIONS);
            return false;
        }
        return true;
    }

    /**
     * 권한 요청 Callback 함수
     * PERMISSION_GRANTED로 권한을 획득했는지 확인할 수 있음.
     */
    @Override
    public void onRequestPermissionsResult(int requestCode, String permissions[], int[] grantResults) {
        switch (requestCode) {
            case MULTIPLE_PERMISSIONS: {
                if (grantResults.length > 0) {
                    for (int i = 0; i < permissions.length; i++) {
                        if (permissions[i].equals(this.permissions[0])) {
                            if (grantResults[i] != PackageManager.PERMISSION_GRANTED) {
                                showNoPermissionToastAndFinish();
                            }
                        } else if (permissions[i].equals(this.permissions[1])) {
                            if (grantResults[i] != PackageManager.PERMISSION_GRANTED) {
                                showNoPermissionToastAndFinish();
                            }
                        }
                    }
                } else {
                    showNoPermissionToastAndFinish();
                }
                return;
            }
        }
    }

    /**
     * 권한 획득에 동의하지 않았을 경우 아래 Toast 메세지 띄우며 종료시킴.
     */
    private void showNoPermissionToastAndFinish() {
        Toast.makeText(ManualRegistActivity.this, "권한 요청에 동의 해주셔야 이용 가능합니다. 설정에서 권한 허용 하시기 바랍니다.", Toast.LENGTH_SHORT).show();
        finish();
    }

    View.OnClickListener selectImageClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View view) {

        }
    };

}
