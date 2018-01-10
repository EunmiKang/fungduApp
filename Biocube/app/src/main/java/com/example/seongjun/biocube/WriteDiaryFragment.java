package com.example.seongjun.biocube;

import android.Manifest;
import android.app.AlertDialog;
import android.app.DatePickerDialog;
import android.app.Dialog;
import android.content.ComponentName;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.graphics.Bitmap;
import android.media.MediaScannerConnection;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.Fragment;
import android.support.v4.content.ContextCompat;
import android.support.v4.content.FileProvider;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.ImageButton;
import android.widget.Spinner;
import android.widget.Toast;

import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.concurrent.ExecutionException;

import static android.app.Activity.RESULT_OK;


/**
 * A simple {@link Fragment} subclass.
 * Activities that contain this fragment must implement the
 * {@link WriteDiaryFragment.OnFragmentInteractionListener} interface
 * to handle interaction events.
 * Use the {@link WriteDiaryFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class WriteDiaryFragment extends Fragment {
    private View view;

    private ImageButton iv_UserPhoto;

    private static final int PICK_FROM_CAMERA = 0;  //사진을 촬영하고 찍힌 이미지를 처리
    private static final int PICK_FROM_ALBUM = 1;   //앨범에서 사진을 고르고 이미지를 처리
    private static final int CROP_FROM_CAMERA = 2;   //이미지를 크롭

    private Uri mImageCaptureUri;
    private File photoFile = null, croppedFile;
    private String[] permissions = {Manifest.permission.READ_EXTERNAL_STORAGE, Manifest.permission.WRITE_EXTERNAL_STORAGE, Manifest.permission.CAMERA}; //권한 설정 변수
    private static final int MULTIPLE_PERMISSIONS = 101; //권한 동의 여부 문의 후 CallBack 함수에 쓰일 변수

    private String mCurrentPhotoPath;

    private OnFragmentInteractionListener mListener;
    int mChoicedArrayItem;

    private Spinner cubeSpinner, filterSpinner;

    Button dateBtn;
    private int iYear, iMonth, iDay;

    public WriteDiaryFragment() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @return A new instance of fragment WriteDiaryFragment.
     */
    // TODO: Rename and change types and number of parameters
    public static WriteDiaryFragment newInstance() {
        Bundle args = new Bundle();

        WriteDiaryFragment fragment = new WriteDiaryFragment();
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        checkPermissions();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        view = inflater.inflate(R.layout.fragment_write_diary, container, false);

        /* Toolbar 설정 */
        Toolbar toolbar = (Toolbar) view.findViewById(R.id.toolbar_diary);
        ((AppCompatActivity)getActivity()).setSupportActionBar(toolbar);
        ActionBar actionBar = ((AppCompatActivity)getActivity()).getSupportActionBar();
        actionBar.setDisplayShowCustomEnabled(true);
        actionBar.setDisplayShowTitleEnabled(false);

        /* 큐브, 필터 spinner 설정 */
        String[] cubeList, filterList;

        cubeSpinner = view.findViewById(R.id.spinner_cube);
        try {
            String[] getList = new ReturnCubeList().execute(((UserMainActivity)getActivity()).userID).get();
            cubeList = new String[getList.length-1];
            for(int i=0; i<getList.length-1; i++) {
                cubeList[i] = getList[i+1];
            }
            ArrayAdapter<String> cubeAdapter = new ArrayAdapter<String>(getContext(), android.R.layout.simple_spinner_dropdown_item, cubeList);
            cubeSpinner.setAdapter(cubeAdapter);
        } catch (InterruptedException e) {
            e.printStackTrace();
        } catch (ExecutionException e) {
            e.printStackTrace();
        }

        filterSpinner = view.findViewById(R.id.spinner_filter);
        try {
            String[] getList = new ReturnFilterList().execute(((UserMainActivity)getActivity()).userID).get();
            filterList = new String[getList.length];
            filterList[0] = "필터 없음";
            for(int i=1; i<getList.length; i++) {
                filterList[i] = getList[i];
            }
            ArrayAdapter<String> filterAdapter = new ArrayAdapter<String>(getContext(), android.R.layout.simple_spinner_dropdown_item, filterList);
            filterSpinner.setAdapter(filterAdapter);
        } catch (InterruptedException e) {
            e.printStackTrace();
        } catch (ExecutionException e) {
            e.printStackTrace();
        }

        /* 이미지 버튼 설정 */
        iv_UserPhoto = (ImageButton) view.findViewById(R.id.btn_addImage);
        iv_UserPhoto.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                selectImage(view);
            }
        });

        /* 날짜 버튼 설정 */
        dateBtn = view.findViewById(R.id.btn_date);
        dateBtn.setText(getTodayDate());
        dateBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String pickedDate = dateBtn.getText().toString();
                pickedDate = pickedDate.replace("년","/").replace("월","/").replace("일","/");

                SimpleDateFormat sdf = new SimpleDateFormat("yyyy/MM/dd/");

                try{
                    Date pickDate = new Date(pickedDate);
                    Calendar cal = Calendar.getInstance();
                    cal.setTime(pickDate);
                    Dialog dialog = null;
                    dialog = new DatePickerDialog(getContext(), dateSetListener, cal.get(Calendar.YEAR), cal.get(Calendar.MONTH), cal.get(Calendar.DAY_OF_MONTH));
                    dialog.show();
                }
                catch (Exception e){
                    e.printStackTrace();
                }
            }
        });

        /* 작성 완료 버튼 리스너 설정 */
        Button registerBtn = (Button) view.findViewById(R.id.btn_diary_register);
        registerBtn.setOnClickListener(diaryRegisterListener);

        return view;
    }

    // TODO: Rename method, update argument and hook method into UI event
    public void onButtonPressed(Uri uri) {
        if (mListener != null) {
            mListener.onFragmentInteraction(uri);
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mListener = null;
    }

    /**
     * This interface must be implemented by activities that contain this
     * fragment to allow an interaction in this fragment to be communicated
     * to the activity and potentially other fragments contained in that
     * activity.
     * <p>
     * See the Android Training lesson <a href=
     * "http://developer.android.com/training/basics/fragments/communicating.html"
     * >Communicating with Other Fragments</a> for more information.
     */
    public interface OnFragmentInteractionListener {
        // TODO: Update argument type and name
        void onFragmentInteraction(Uri uri);
    }

    /**
     * 사용자에게 권한 묻기
     */
    private boolean checkPermissions() {
        int result;
        List<String> permissionList = new ArrayList<>();
        for (String pm : permissions) {
            result = ContextCompat.checkSelfPermission(getContext(), pm);
            if (result != PackageManager.PERMISSION_GRANTED) {  //사용자가 해당 권한을 가지고 있지 않을 경우 리스트에 해당 권한명 추가
                permissionList.add(pm);
            }
        }
        if (!permissionList.isEmpty()) {    //권한이 추가되었으면 해당 리스트가 empty가 아니므로 권한을 request(요청)함
            ActivityCompat.requestPermissions(getActivity(), permissionList.toArray(new String[permissionList.size()]), MULTIPLE_PERMISSIONS);
            return false;
        }
        return true;
    }

    private void selectImage(View view) {
        DialogInterface.OnClickListener cancelListener = new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
                //dialog.cancel();
            }
        };

        final CharSequence[] mArrayItem = {"사진 촬영", "앨범에서 선택"};

        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setTitle("업로드 할 이미지 선택");
        builder.setSingleChoiceItems(mArrayItem, 0, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int whichButton) {
                mChoicedArrayItem = whichButton;
            }
        });
        builder.setPositiveButton("선택", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int whichButton) {
                if(checkPermissions()) {
                    if (mChoicedArrayItem == 0) {
                        doTakePhotoAction();
                    } else if (mChoicedArrayItem == 1) {
                        doTakeAlbumAction();
                    }
                } else {
                    Toast.makeText(getContext(), "권한을 허용해주세요.", Toast.LENGTH_SHORT).show();
                    dialog.cancel();
                }
            }
        });
        builder.setNegativeButton("취소", cancelListener);

        AlertDialog dialog = builder.create();  //알림창 객체 생성
        dialog.show();  //알림창 띄우기
    }

    /**
     * 카메라에서 사진 촬영
     * */
    public void doTakePhotoAction() {
        Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);

        try {
            photoFile = createImageFile();
        } catch (IOException e) {
            Toast.makeText(getContext(), "이미지 처리 오류! 다시 시도해주세요.", Toast.LENGTH_SHORT).show();
            getActivity().finish();
            e.printStackTrace();
        }
        if (photoFile != null) {
            mImageCaptureUri = FileProvider.getUriForFile(getContext(),
                    "com.example.seongjun.biocube.provider", photoFile);
            intent.putExtra(MediaStore.EXTRA_OUTPUT, mImageCaptureUri); //사진을 찍어 해당 Content uri를 mImageCaptureUri에 적용시키기 위함
            startActivityForResult(intent, PICK_FROM_CAMERA);
        }
    }

    /**
     * Android M에서는 Uri.fromFile 함수를 사용하였으나, 7.0부터는 이 함수를 사용할 시 FileUriExposedException이 발생하므로 아래와 같이 함수를 작성함.
     */
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

    /**
     * 앨범에서 이미지 가져오기
     */
    public void doTakeAlbumAction() {
        Intent intent = new Intent(Intent.ACTION_PICK);
        intent.setType(MediaStore.Images.Media.CONTENT_TYPE);
        startActivityForResult(intent, PICK_FROM_ALBUM);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (resultCode != RESULT_OK) {
            Toast.makeText(getContext(), "취소 되었습니다.", Toast.LENGTH_SHORT).show();
            return;
        }

        if (requestCode == PICK_FROM_ALBUM) {
            if (data == null) {
                return;
            }
            mImageCaptureUri = data.getData();
            cropImage();
        } else if (requestCode == PICK_FROM_CAMERA) {
            cropImage();


            // 갤러리에 나타나게
            MediaScannerConnection.scanFile(getContext(),
                    new String[]{mImageCaptureUri.getPath()}, null,
                    new MediaScannerConnection.OnScanCompletedListener() {
                        public void onScanCompleted(String path, Uri uri) {
                        }
                    });

        } else if (requestCode == CROP_FROM_CAMERA) {
            iv_UserPhoto.setImageURI(null);
            iv_UserPhoto.setImageURI(mImageCaptureUri);

            if(photoFile != null) {
                photoFile.delete(); // 임시 파일 삭제
            }
        }
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
                        } else if (permissions[i].equals(this.permissions[2])) {
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
        Toast.makeText(getContext(), "권한 요청에 동의 해주셔야 이용 가능합니다. 설정에서 권한 허용 하시기 바랍니다.", Toast.LENGTH_SHORT).show();
        getActivity().finish();
    }

    //Android N crop image
    public void cropImage() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {   // 마쉬멜로우 이상 버전일 때
            getActivity().grantUriPermission("com.android.camera", mImageCaptureUri,
                    Intent.FLAG_GRANT_WRITE_URI_PERMISSION | Intent.FLAG_GRANT_READ_URI_PERMISSION);
        }
        Intent intent = new Intent("com.android.camera.action.CROP");
        intent.setDataAndType(mImageCaptureUri, "image/*");

        List<ResolveInfo> list = getActivity().getPackageManager().queryIntentActivities(intent, 0);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            getActivity().grantUriPermission(list.get(0).activityInfo.packageName, mImageCaptureUri,
                    Intent.FLAG_GRANT_WRITE_URI_PERMISSION | Intent.FLAG_GRANT_READ_URI_PERMISSION);
        }
        int size = list.size();
        if (size == 0) {
            Toast.makeText(getContext(), "취소 되었습니다.", Toast.LENGTH_SHORT).show();
            return;
        } else {
            Toast.makeText(getContext(), "용량이 큰 사진의 경우 시간이 오래 걸릴 수 있습니다.", Toast.LENGTH_SHORT).show();

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
            mImageCaptureUri = FileProvider.getUriForFile(getContext(),
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

                getActivity().grantUriPermission(res.activityInfo.packageName, mImageCaptureUri,
                        Intent.FLAG_GRANT_WRITE_URI_PERMISSION | Intent.FLAG_GRANT_READ_URI_PERMISSION);
            }
            i.setComponent(new ComponentName(res.activityInfo.packageName, res.activityInfo.name));
            startActivityForResult(i, CROP_FROM_CAMERA);
        }
    }

    /* 다이어리 작성 버튼 클릭리스너 */
    View.OnClickListener diaryRegisterListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            String url = "http://fungdu0624.phps.kr/biocube/uploadImageForDiary.php";
            String attachmentName = "uploadfile_for_diary";
            String attachmentFileName = croppedFile.getName();
            String uploadImgPath = "users/" + ((UserMainActivity)getActivity()).userID + "/";
            try {
                if(new ImageUploadToServer().execute(url, attachmentName, attachmentFileName, uploadImgPath, mCurrentPhotoPath).get()) {
                    Toast.makeText(getContext(), "업로드 성공", Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(getContext(), "업로드 실패", Toast.LENGTH_SHORT).show();
                }
            } catch (InterruptedException e) {
                e.printStackTrace();
            } catch (ExecutionException e) {
                e.printStackTrace();
            }

        }
    };

    /* 날짜 설정 관련 메소드들 */
    private String getTodayDate() {
        Date todayDate = new Date();

        SimpleDateFormat todayDateFormat = new SimpleDateFormat("yyyy년MM월dd일");
        return todayDateFormat.format(todayDate);
    }

    DatePickerDialog.OnDateSetListener dateSetListener = new DatePickerDialog.OnDateSetListener() {
        @Override
        public void onDateSet(DatePicker view, int year, int monthOfYear, int dayOfMonth) {
            iYear = year;
            iMonth = monthOfYear;
            iDay = dayOfMonth;
            updateDate();
        }
    };

    private void updateDate() {
        StringBuffer sb = new StringBuffer();
        dateBtn.setText(sb.append(iYear+"년").append((iMonth+1) + "월").append(iDay+"일"));
    }

}
