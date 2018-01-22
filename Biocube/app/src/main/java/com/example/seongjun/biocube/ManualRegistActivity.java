package com.example.seongjun.biocube;

import android.Manifest;
import android.app.Activity;
import android.content.ClipData;
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
import android.widget.ImageView;
import android.widget.Toast;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.URLEncoder;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.concurrent.ExecutionException;

public class ManualRegistActivity extends AppCompatActivity {

    EditText text_plantName;
    Button btn_selectRepImage, btn_selectManualImages, btn_registNewManual;
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
    android.support.v4.view.PagerAdapter pagerAdapter;

    // 원본 파일들 경로 리스트
    ArrayList<String> pathList = new ArrayList<>();
    // 넘어온 용
    ArrayList<String> priorList = new ArrayList<>();


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_manual_regist);

        checkPermissions();

        text_plantName = (EditText) findViewById(R.id.text_manualRegist_plantName);
        btn_selectRepImage = (Button) findViewById(R.id.btn_selectRepImage);
        btn_selectManualImages = (Button) findViewById(R.id.btn_selectManualImage);
        btn_registNewManual = (Button) findViewById(R.id.btn_regist);
        img_repImage = (ImageView) findViewById(R.id.img_manual_rep);

        btn_selectRepImage.setOnClickListener(selectRepImageClickListener);
        btn_selectManualImages.setOnClickListener(selectManualImagesClickListener);
        btn_registNewManual.setOnClickListener(registManualClickListener);

        pager = (ViewPager) findViewById(R.id.viewpager_manual);
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


    /* 매뉴얼 이미지 선택 버튼 클릭 리스너 */
    View.OnClickListener selectManualImagesClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View view) {
            // 앨범 호출
            if(checkPermissions()) {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
                    try {
                        // list 초기화
                        priorList = new ArrayList<>();
                        pathList = new ArrayList<>();

                        Intent intent = new Intent(Intent.ACTION_PICK);
                        intent.setType("image/*");
                        intent.putExtra(Intent.EXTRA_ALLOW_MULTIPLE, true);
                        intent.setType(MediaStore.Images.Media.CONTENT_TYPE);
                        startActivityForResult(Intent.createChooser(intent, "여러 장을 선택하시려면 '포토'를 선택해주세요."), REQUEST_MANUAL_IMAGES);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            } else {
                Toast.makeText(ManualRegistActivity.this, "권한 허용 해주셔야 이미지를 선택할 수 있어요ㅠ", Toast.LENGTH_SHORT).show();
            }
        }
    };

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
                if(resultCode == Activity.RESULT_OK) {
                    // 멀티 선택을 지원하지 않는 기기에서는 getClipdata()가 없음 => getData()로 접근해야 함
                    if(data.getClipData() == null) {
                        priorList.add(String.valueOf(data.getData()));
                    } else {
                        ClipData clipData = data.getClipData();
                        if(clipData.getItemCount() > 10) {
                            Toast.makeText(ManualRegistActivity.this, "사진은 10개까지 선택가능 합니다.", Toast.LENGTH_SHORT).show();
                            return;
                        }
                        else if(clipData.getItemCount() == 1) { // 하나만 선택했을 경우
                            String dataStr = String.valueOf(clipData.getItemAt(0).getUri());
                            priorList.add(dataStr);
                        } else if(clipData.getItemCount() > 1 && clipData.getItemCount() < 10) {
                            for(int i=0; i<clipData.getItemCount(); i++) {
                                priorList.add(String.valueOf(clipData.getItemAt(i).getUri()));
                            }
                        }
                    }
                    selectImage();
                } else {
                    Toast.makeText(ManualRegistActivity.this, "사진 선택을 취소하였습니다.", Toast.LENGTH_SHORT).show();
                }
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

    private void selectImage() {
        pager.setAdapter(null);
        String flag = priorList.get(0).substring(0,7);

        // pathList에 원본 넣어둠
        for(int i=0; i<priorList.size(); i++) {
            if(flag.equals("content")) {
                // content:// -> /storage... (포토)
                String path = getRealPathFromURI(Uri.parse(priorList.get(i)));
                pathList.add(path);
            } else {
                // 갤러리: 파일 절대 경로 리턴함(변환은 필요없고, file://를 빼줘야 업로드시 new File에서 이용)
                String path = priorList.get(i).replace("file://","");
                pathList.add(path);
            }
        }

        pager.setOffscreenPageLimit(priorList.size()-1);
        pagerAdapter = new PagerAdapter() {
            @Override
            public int getCount() {
                return priorList.size();
            }

            // 뷰페이저가 열릴 때 초기화하는 함수
            @Override
            public Object instantiateItem(ViewGroup container, int position) {
                View manualView = getLayoutInflater().inflate(R.layout.viewpager_manual_regist, null);
                final ImageView manualImgView = (ImageView) manualView.findViewById(R.id.img_manual_regist);

                Uri uri = Uri.parse(pathList.get(position));
                final String path = uri.getPath();

                // Async
                LoadBitmap loadBitmap = new LoadBitmap(manualImgView);
                loadBitmap.execute(path);

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
                //return false;
                return view == object;
            }
        };

        pager.setAdapter(pagerAdapter);
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
            if(cursor != null) {
                cursor.close();
            }
        }
    }

    class LoadBitmap extends AsyncTask<String, String, Bitmap> {
        ImageView targetView;

        LoadBitmap(ImageView imageView) {
            targetView = imageView;
        }

        @Override
        protected Bitmap doInBackground(String[] params) {
            BitmapFactory.Options options = new BitmapFactory.Options();
            options.inSampleSize = 2;
            Bitmap bitmap = BitmapFactory.decodeFile(params[0], options);
            return bitmap;
        }

        @Override
        protected void onPostExecute(Bitmap bitmap) {
            targetView.setImageBitmap(bitmap);
        }
    }


    /* 완료 버튼 클릭 리스너 */
    View.OnClickListener registManualClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View view) {
            // null 체크

            // 식물 이름 중복 검사


            // 서버에 이미지 업로드
            uploadImages();

            // 디비에 업로드
            uploadDB();
        }
    };

    public void uploadImages() {
        /* 서버에 이미지 올리기 */
        String url = "http://fungdu0624.phps.kr/biocube/uploadImageForManual.php";
        String attachmentName = "uploadfile_for_diary";
        //String attachmentFileName = croppedFile.getName();
        //String uploadImgPath = "users/" + ((UserMainActivity) getActivity()).userID + "/";

        // 서버에 이미지 업로드

    }

    public void uploadDB() {
        /* 다이어리 작성 내용 디비에 저장하기 */

        // 디비에 업로드
    }


}
