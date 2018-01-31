package com.example.seongjun.biocube;

import android.Manifest;
import android.content.ComponentName;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v4.content.FileProvider;
import android.support.v4.view.*;
import android.support.v4.view.PagerAdapter;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.Toast;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLEncoder;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.concurrent.ExecutionException;

import me.relex.circleindicator.CircleIndicator;

public class ManualRegistActivity extends AppCompatActivity {

    EditText text_plantName;
    Button btn_selectRepImage, btn_registNewManual, btn_reset;
    ImageView img_repImage;

    private String[] permissions = {Manifest.permission.READ_EXTERNAL_STORAGE, Manifest.permission.WRITE_EXTERNAL_STORAGE}; //권한 설정 변수
    private static final int MULTIPLE_PERMISSIONS = 101; //권한 동의 여부 문의 후 CallBack 함수에 쓰일 변수
    private static final int REQUEST_REP_IMAGE = 0;
    private static final int REQUEST_MANUAL_IMAGES = 1;
    private static final int CROP_FROM_CAMERA = 2;

    private Uri mImageCaptureUri;
    private String mCurrentPhotoPath;
    private File photoFile = null, croppedFile= null;

    ViewPager pager;
    ManualRegistPagerAdapter pagerAdapter;

    // 원본 파일들 경로 리스트
    String[] pathList = new String[10];
    // 넘어온 용
    String[] priorList = new String[10];

    private int position;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_manual_regist);

        checkPermissions();

        text_plantName = (EditText) findViewById(R.id.text_manualRegist_plantName);
        btn_selectRepImage = (Button) findViewById(R.id.btn_selectRepImage);
        btn_registNewManual = (Button) findViewById(R.id.btn_regist);
        btn_reset = (Button) findViewById(R.id.btn_reset_manual);
        img_repImage = (ImageView) findViewById(R.id.img_manual_rep);

        btn_selectRepImage.setOnClickListener(selectRepImageClickListener);
        btn_registNewManual.setOnClickListener(registManualClickListener);
        btn_reset.setOnClickListener(resetClickListener);

        pager = (ViewPager) findViewById(R.id.viewpager_manual);
        //pager.setOffscreenPageLimit(priorList.size()-1);
        pagerAdapter = new ManualRegistPagerAdapter();
        pager.addOnPageChangeListener(new ViewPager.OnPageChangeListener() {

            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {

            }

            @Override
            public void onPageSelected(int position) {
                pagerAdapter.setCurrentPosition(position);
            }

            @Override
            public void onPageScrollStateChanged(int state) {

            }
        });
        pager.setAdapter(pagerAdapter);
        CircleIndicator indicator = (CircleIndicator)  findViewById(R.id.indicator_manualRegist);
        indicator.setViewPager(pager);
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


    /* 매뉴얼 대표 이미지 선택 버튼 클릭 리스너 */
    View.OnClickListener selectRepImageClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View view) {
            // 앨범 호출
            if(checkPermissions()) {
                Intent intent = new Intent(Intent.ACTION_PICK);
                intent.setType(MediaStore.Images.Media.CONTENT_TYPE);
                startActivityForResult(intent, REQUEST_REP_IMAGE);
            } else {
                Toast.makeText(ManualRegistActivity.this, "권한 허용 해주셔야 이미지를 선택할 수 있어요ㅠ", Toast.LENGTH_SHORT).show();
            }
        }
    };


    /* 매뉴얼 이미지 선택 버튼 클릭 */
    public void selectManualImage(int current_position) {
        position = current_position;
        Intent intent = new Intent(Intent.ACTION_PICK);
        intent.setType(MediaStore.Images.Media.CONTENT_TYPE);
        startActivityForResult(intent, REQUEST_MANUAL_IMAGES);
    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        switch (requestCode) {
            case REQUEST_REP_IMAGE:
                if (data == null) {
                    return;
                }
                mImageCaptureUri = data.getData();
                cropImage();
                break;
            case REQUEST_MANUAL_IMAGES:
                if (data == null) {
                    return;
                }

                priorList[position] = data.getData().toString();
                String path = getRealPathFromURI(Uri.parse(priorList[position]));
                //String path = priorList[position].replace("file://","");
                pathList[position] = path;

                pager.setAdapter(pagerAdapter);
                pager.setCurrentItem(position);

                break;
            case CROP_FROM_CAMERA:
                img_repImage.setImageURI(null);
                img_repImage.setImageURI(mImageCaptureUri);

                if(photoFile != null) {
                    photoFile.delete(); // 임시 파일 삭제
                }
                break;
        }
    }

    public String getRealPathFromURI(Uri contentUri) {
        Cursor cursor = null;
        try {
            String[] proj = {MediaStore.Images.Media.DATA};
            cursor = getContentResolver().query(contentUri, proj, null, null, null);
            int column_index = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DATA);
            cursor.moveToFirst();
            return cursor.getString(column_index);
        } finally {
            if (cursor != null) {
                cursor.close();
            }
        }
    }

    //Android N crop image
    public void cropImage() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {   // 마쉬멜로우 이상 버전일 때
            grantUriPermission("com.android.camera", mImageCaptureUri,
                    Intent.FLAG_GRANT_WRITE_URI_PERMISSION | Intent.FLAG_GRANT_READ_URI_PERMISSION);
        }
        Intent intent = new Intent("com.android.camera.action.CROP");
        intent.setDataAndType(mImageCaptureUri, "image/*");

        List<ResolveInfo> list = getPackageManager().queryIntentActivities(intent, 0);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            grantUriPermission(list.get(0).activityInfo.packageName, mImageCaptureUri,
                    Intent.FLAG_GRANT_WRITE_URI_PERMISSION | Intent.FLAG_GRANT_READ_URI_PERMISSION);
        }
        int size = list.size();
        if (size == 0) {
            Toast.makeText(ManualRegistActivity.this, "취소 되었습니다.", Toast.LENGTH_SHORT).show();
            return;
        } else {
            Toast.makeText(ManualRegistActivity.this, "용량이 큰 사진의 경우 시간이 오래 걸릴 수 있습니다.", Toast.LENGTH_SHORT).show();

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
                intent.addFlags(Intent.FLAG_GRANT_WRITE_URI_PERMISSION);
            }
            intent.putExtra("crop", "true");
            intent.putExtra("aspectX", 1);
            intent.putExtra("aspectY", 1);
            intent.putExtra("scale", true);
            croppedFile = null;
            try {
                croppedFile = createImageFile();
            } catch (IOException e) {
                e.printStackTrace();
            }

            File folder = new File(Environment.getExternalStorageDirectory() + "/BioCube/");
            File tempFile = new File(folder.toString(), croppedFile.getName());

            mCurrentPhotoPath = tempFile.getAbsolutePath();
            mImageCaptureUri = FileProvider.getUriForFile(ManualRegistActivity.this,
                    "com.example.seongjun.biocube.provider", tempFile);

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
                intent.addFlags(Intent.FLAG_GRANT_WRITE_URI_PERMISSION);
            }

            intent.putExtra("return-data", false);
            intent.putExtra(MediaStore.EXTRA_OUTPUT, mImageCaptureUri);
            intent.putExtra("outputFormat", Bitmap.CompressFormat.JPEG.toString());

            Intent i = new Intent(intent);
            ResolveInfo res = list.get(0);
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                i.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
                i.addFlags(Intent.FLAG_GRANT_WRITE_URI_PERMISSION);

                grantUriPermission(res.activityInfo.packageName, mImageCaptureUri,
                        Intent.FLAG_GRANT_WRITE_URI_PERMISSION | Intent.FLAG_GRANT_READ_URI_PERMISSION);
            }
            i.setComponent(new ComponentName(res.activityInfo.packageName, res.activityInfo.name));
            startActivityForResult(i, CROP_FROM_CAMERA);
        }
    }

    private File createImageFile() throws IOException {
        String timeStamp = new SimpleDateFormat("yyMMddHHmmss").format(new Date());
        String imageFileName = "biocube_" + timeStamp + "_";
        File storageDir = new File(Environment.getExternalStorageDirectory() + "/BioCube/");
        if (!storageDir.exists()) {
            storageDir.mkdirs();
        }
        File image = File.createTempFile(imageFileName, ".jpg", storageDir);
        mCurrentPhotoPath = "file:" + image.getAbsolutePath();
        return image;
    }

    View.OnClickListener resetClickListener = new View.OnClickListener() {

        @Override
        public void onClick(View v) {
            priorList = new String[10];
            pathList = new String[10];
            pager.setAdapter(pagerAdapter);
            pager.setCurrentItem(0);
        }
    };


    /* 완료 버튼 클릭 리스너 */
    View.OnClickListener registManualClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View view) {
            /* null 체크 */
            String plant_name = text_plantName.getText().toString();
            if(plant_name.equals("")) {
                Toast.makeText(ManualRegistActivity.this, "식물 이름을 입력해주세요.", Toast.LENGTH_SHORT).show();
                return;
            }
            if(croppedFile == null) {
                Toast.makeText(ManualRegistActivity.this, "식물의 대표 이미지를 선택해주세요.", Toast.LENGTH_SHORT).show();
                return;
            }
            if(priorList[0] == null) {
                Toast.makeText(ManualRegistActivity.this, "매뉴얼로 등록할 이미지를 선택해주세요.", Toast.LENGTH_SHORT).show();
                return;
            }


            /* 식물 이름 중복 검사 */
            boolean duplecheck = false;
            try {
                duplecheck = new PlantNameDupleCheck().execute(plant_name).get();
            } catch (InterruptedException e) {
                e.printStackTrace();
            } catch (ExecutionException e) {
                e.printStackTrace();
            }
            if(!duplecheck) {  // 중복 X
                /* 서버에 이미지 업로드하고, 디비 업데이트 */
                if(uploadImages(plant_name)) {
                    boolean updateDB = false;
                    try {
                        updateDB = new UpdateDB().execute(plant_name).get();
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    } catch (ExecutionException e) {
                        e.printStackTrace();
                    }
                    if(updateDB) {
                        Toast.makeText(ManualRegistActivity.this, "등록되었습니다.", Toast.LENGTH_SHORT).show();
                        /* 초기화 */
                        priorList = new String[10];
                        pathList = new String[10];
                        mCurrentPhotoPath = null;
                        mImageCaptureUri = null;
                        img_repImage.setImageDrawable(null);
                        text_plantName.setText("");
                        pager.setAdapter(pagerAdapter);
                    }
                    else {
                        Toast.makeText(ManualRegistActivity.this, "DB 업데이트 실패", Toast.LENGTH_SHORT).show();
                    }
                }
            } else {    // 중복 된 경우
                Toast.makeText(ManualRegistActivity.this, "이미 등록되어 있는 식물 이름입니다.", Toast.LENGTH_SHORT).show();
                text_plantName.setText("");
            }
        }
    };

    /* 식물 이름 중복 검사 쓰레드 */
    public class PlantNameDupleCheck extends AsyncTask<String,Object,Boolean> {
        @Override
        protected Boolean doInBackground(String... params) {
            String plant_name = params[0];
            try{
                plant_name = URLEncoder.encode(plant_name,"UTF-8");
            } catch(Exception e) {
                e.printStackTrace();
            }
            boolean result = true;
            try {
                /* URL 설정하고 접속 */
                URL url = new URL("http://fungdu0624.phps.kr/biocube/checkDuplePlantName.php");
                HttpURLConnection http = (HttpURLConnection) url.openConnection();

                /* 전송모드 설정 */
                http.setDefaultUseCaches(false);
                http.setDoInput(true);  //서버에서 읽기 모드로 지정
                http.setDoOutput(true);    //서버에서 쓰기 모드로 지정
                http.setRequestMethod("POST");
                http.setRequestProperty("content-type", "application/x-www-form-urlencoded");   //서버에게 웹에서 <Form>으로 값이 넘어온 것과 같은 방식으로 처리하라는 걸 알려준다

                StringBuffer buffer = new StringBuffer();
                buffer.append("plant_name").append("=").append(plant_name);

                OutputStreamWriter outStream = new OutputStreamWriter(http.getOutputStream(), "EUC-KR");
                PrintWriter writer = new PrintWriter(outStream);
                writer.write(buffer.toString());
                writer.flush();
                writer.close();

                /* 서버에서 전송 받기 */
                InputStream inStream = http.getInputStream();
                BufferedReader reader = new BufferedReader(new InputStreamReader(inStream, "UTF-8"));
                String readLine = reader.readLine();
                if(readLine.equals("not_duple")) {
                    result = false;
                }

                inStream.close();
                http.disconnect();
            } catch (MalformedURLException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            }

            return result;
        }
    }

    /* 서버에 이미지 업로드 */
    public boolean uploadImages(String plant_name) {
        /* 대표 이미지 업로드 */
        String url = "http://fungdu0624.phps.kr/biocube/uploadRepImageForManual.php";
        String attachmentName = "uploadfile_repimage";
        String attachmentFileName = croppedFile.getName();
        String uploadImgPath = "manual/";

        // 용량 줄이기
        BitmapFactory.Options option = new BitmapFactory.Options();
        option.inSampleSize = 2;    // 1/2만큼 줄임
        Bitmap src = BitmapFactory.decodeFile(mCurrentPhotoPath, option);
        try {
            FileOutputStream out = new FileOutputStream(mCurrentPhotoPath);
            src.compress(Bitmap.CompressFormat.JPEG, 100, out);
            out.close();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }

        // 서버에 이미지 업로드
        try {
            if (new ImageUploadToServer().execute(url, attachmentName, attachmentFileName, uploadImgPath, mCurrentPhotoPath, plant_name).get()) {
                Toast.makeText(ManualRegistActivity.this, "업로드 성공", Toast.LENGTH_SHORT).show();
                croppedFile.delete();
            } else {
                Toast.makeText(ManualRegistActivity.this, "서버에 사진 업로드 실패", Toast.LENGTH_SHORT).show();
                return false;
            }
        } catch (InterruptedException e) {
            e.printStackTrace();
        } catch (ExecutionException e) {
            e.printStackTrace();
        }


        /* 매뉴얼 이미지 업로드 */
        url = "http://fungdu0624.phps.kr/biocube/uploadImageForManual.php";
        attachmentName = "uploadfile_for_manual";
        uploadImgPath = "manual/"+plant_name+"/";
        for(int i=0; i<pathList.length; i++) {
            if(pathList[i] != null) {
                attachmentFileName = pathList[i].substring(pathList[i].lastIndexOf("/")+1);

                // 서버에 이미지 업로드
                try {
                    if (new ImageUploadToServer().execute(url, attachmentName, attachmentFileName, uploadImgPath, pathList[i], plant_name).get()) {
                        //Toast.makeText(ManualRegistActivity.this, "업로드 성공", Toast.LENGTH_SHORT).show();
                        croppedFile.delete();
                    } else {
                        Toast.makeText(ManualRegistActivity.this, "매뉴얼 등록 실패", Toast.LENGTH_SHORT).show();
                        return false;
                    }
                } catch (InterruptedException e) {
                    e.printStackTrace();
                } catch (ExecutionException e) {
                    e.printStackTrace();
                }
            } else {
                break;
            }
        }
        return true;
    }

    /* 다이어리 작성 내용 디비에 업데이트용 쓰레드 */
    public class UpdateDB extends AsyncTask<String,Object,Boolean>{
        @Override
        protected Boolean doInBackground(String... params) {
            boolean result = false;
            try {
             /* URL 설정하고 접속 */
                URL url = new URL("http://fungdu0624.phps.kr/biocube/updateManual.php");
                HttpURLConnection http = (HttpURLConnection) url.openConnection();

            /* 전송모드 설정 */
                http.setDefaultUseCaches(false);
                http.setDoInput(true);  //서버에서 읽기 모드로 지정
                http.setDoOutput(true);    //서버에서 쓰기 모드로 지정
                http.setRequestMethod("POST");
                http.setRequestProperty("content-type", "application/x-www-form-urlencoded");   //서버에게 웹에서 <Form>으로 값이 넘어온 것과 같은 방식으로 처리하라는 걸 알려준다

            /* 서버로 값 전송 */
                StringBuffer buffer = new StringBuffer();
                buffer.append("plant_name").append("=").append("'" + URLEncoder.encode(params[0], "UTF-8") + "'").append("&");
                for(int i=0; i<pathList.length; i++) {
                    if(pathList[i] != null) {
                        buffer.append("manual_" + (i+1)).append("=").append("'" + URLEncoder.encode(pathList[i].substring(pathList[i].lastIndexOf("/") + 1), "UTF-8") + "'");
                    } else {
                        buffer.append("manual_" + (i+1)).append("=").append("NULL");
                    }
                    if(i<(pathList.length-1)) {
                        buffer.append("&");
                    }
                }

                OutputStreamWriter outStream = new OutputStreamWriter(http.getOutputStream(), "EUC-KR");
                PrintWriter writer = new PrintWriter(outStream);
                writer.write(buffer.toString());
                writer.flush();
                writer.close();

            /* 서버에서 전송 받기 */
                InputStream inStream = http.getInputStream();
                BufferedReader reader = new BufferedReader(new InputStreamReader(inStream, "UTF-8"));
                String str = reader.readLine();

                if (str.equals("success")) {  // 업로드 성공
                    result = true;
                }
            } catch (MalformedURLException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            }
            return result;
        }
    }


    public class ManualRegistPagerAdapter extends PagerAdapter {
        private int current_position;
        ImageButton manualImgBtn;

        @Override
        public int getCount() {
            return 10;
        }

        // 뷰페이저가 열릴 때 초기화하는 함수
        @Override
        public Object instantiateItem(ViewGroup container, int position) {
            View manualView = getLayoutInflater().inflate(R.layout.viewpager_manual_regist, null);
            manualImgBtn = (ImageButton) manualView.findViewById(R.id.imgbtn_manual_regist);

            if(pathList[position] != null) {
                manualImgBtn.setImageURI(Uri.parse(pathList[position]));
            }
            manualImgBtn.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    selectManualImage(current_position);
                }
            });

            // 컨테이너에 일단 이미지뷰를 갖고 있는 manualView 레이아웃을 넣어두고, 작업 후 onPostExecute에서 setImageBitmap
            container.addView(manualView);

            return manualView;
        }

        @Override
        public void destroyItem(ViewGroup container, int position, Object object) {
            container.removeView((View) object);
        }

        @Override
        public boolean isViewFromObject(View view, Object object) {
            return (view == object);
        }

        public void setCurrentPosition(int position) {
            this.current_position = position;
        }
    }
}
