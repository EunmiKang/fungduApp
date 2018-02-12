package com.example.seongjun.biocube;

import android.Manifest;
import android.app.AlertDialog;
import android.app.DatePickerDialog;
import android.app.Dialog;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.ComponentName;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.media.MediaScannerConnection;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.provider.MediaStore;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.Fragment;
import android.support.v4.content.ContextCompat;
import android.support.v4.content.FileProvider;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.Spinner;
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
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Set;
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
    private File photoFile = null, croppedFile= null;
    private String[] permissions = {Manifest.permission.READ_EXTERNAL_STORAGE, Manifest.permission.WRITE_EXTERNAL_STORAGE, Manifest.permission.CAMERA}; //권한 설정 변수
    private static final int MULTIPLE_PERMISSIONS = 101; //권한 동의 여부 문의 후 CallBack 함수에 쓰일 변수

    private String mCurrentPhotoPath;

    private OnFragmentInteractionListener mListener;
    int mChoicedArrayItem;

    private Spinner cubeSpinner, filterSpinner;

    private Button dateBtn;
    private int iYear, iMonth, iDay;

    private EditText contentText;
    private Button btn_sensor;

    private int readBufferPosition;
    byte[] readBuffer;
    Thread mWorkerThread = null;
    InputStream mInputStream = null;
    char mCharDelimiter =  '\n';

    Set<BluetoothDevice> mDevices;
    BluetoothAdapter mBluetoothAdapter;
    int mPariedDeviceCount = 0;
    String id;

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

        try {//로그인된 아이디 불러옴
            id = new GetId().execute(getActivity()).get();
        } catch (InterruptedException e) {
            e.printStackTrace();
        } catch (ExecutionException e) {
            e.printStackTrace();
        }

        /* Toolbar 설정 */
        Toolbar toolbar = (Toolbar) view.findViewById(R.id.toolbar_diary);
        ((AppCompatActivity)getActivity()).setSupportActionBar(toolbar);
        ActionBar actionBar = ((AppCompatActivity)getActivity()).getSupportActionBar();
        actionBar.setDisplayShowCustomEnabled(true);
        actionBar.setDisplayShowTitleEnabled(false);
        btn_sensor = (Button) view.findViewById(R.id.btn_sensor);
        mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();

        /* 큐브, 필터 spinner 설정 */
        String[] filterList;
        cubeSpinner = view.findViewById(R.id.spinner_cube);
        setSpinner();

        filterSpinner = view.findViewById(R.id.spinner_filter);
        try {
            String[] getList = new ReturnFilterList().execute(id).get();
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

        btn_sensor.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v) {
                String selectedCube = cubeSpinner.getSelectedItem().toString();
                String deviceNum = "";
                try{
                    selectedCube = URLEncoder.encode(selectedCube,"UTF-8");
                    deviceNum = new GetDevice().execute(selectedCube).get();
                    ((UserMainActivity) getActivity()).mBluetooth.mSocket.close();
                } catch(Exception e) {
                    e.printStackTrace();
                }
                switch (((UserMainActivity)getActivity()).mBluetooth.checkBluetooth(getContext())){
                    case 0: Toast.makeText(getContext(), "기기가 블루투스를 지원하지 않습니다.", Toast.LENGTH_LONG).show();
                        break;
                    case 1: Toast.makeText(getContext(), "현재 블루투스가 비활성 상태입니다.", Toast.LENGTH_LONG).show();
                        Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
                        startActivityForResult(enableBtIntent, ((UserMainActivity)getActivity()).mBluetooth.REQUEST_ENABLE_BT);
                        break;
                    case 2: mDevices = mBluetoothAdapter.getBondedDevices();//기기를 지원하고 활성상태일때
                        mPariedDeviceCount = mDevices.size();
                }
                if(mDevices != null) {
                    BluetoothDevice device = mBluetoothAdapter.getRemoteDevice(deviceNum);
                    if (((UserMainActivity) getActivity()).mBluetooth.connectToSelectedDevice(deviceNum, mDevices, device)) {
                        beginListenForData();
                        ((UserMainActivity) getActivity()).mBluetooth.sendData("read_data");
                    } else {
                        Toast.makeText(getContext(), "블루투스 연결 중 오류가 발생했습니다.", Toast.LENGTH_LONG).show();
                    }
                }
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

        /* 내용 view */
        contentText = view.findViewById(R.id.text_diary_content);

        /* 작성 완료 버튼 리스너 설정 */
        Button registerBtn = (Button) view.findViewById(R.id.btn_diary_register);
        registerBtn.setOnClickListener(diaryRegisterListener);

        return view;
    }

    public void setSpinner(){
        String[] cubeList;
        try {
            String[] getList = new ReturnCubeList().execute(id).get();
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

    private void selectImage(View view) {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setTitle("업로드 할 이미지 선택");
        final CharSequence[] mArrayItem = {"사진 촬영", "앨범에서 선택"};
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

        DialogInterface.OnClickListener cancelListener = new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
                //dialog.cancel();
            }
        };

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
            intent.putExtra("outputX", 900);
            intent.putExtra("outputY", 900);
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
            if(cubeSpinner.getAdapter().getCount() > 0) {
                if (!((croppedFile == null) && contentText.getText().toString().equals(""))) {

            /* 서버에 이미지 올리기 */
                    if (croppedFile != null) {
                        String url = "http://fungdu0624.phps.kr/biocube/uploadImageForDiary.php";
                        String attachmentName = "uploadfile_for_diary";
                        String attachmentFileName = croppedFile.getName();
                        String uploadImgPath = "users/" + ((UserMainActivity) getActivity()).userID + "/";

                        // 용량 줄이기
                    /*
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
                    */

                        // 서버에 이미지 업로드
                        try {
                            if (new ImageUploadToServer().execute(url, attachmentName, attachmentFileName, uploadImgPath, mCurrentPhotoPath).get()) {
                                //Toast.makeText(getContext(), "업로드 성공", Toast.LENGTH_SHORT).show();
                            } else {
                                Toast.makeText(getContext(), "서버에 사진 업로드 실패", Toast.LENGTH_SHORT).show();
                            }
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        } catch (ExecutionException e) {
                            e.printStackTrace();
                        }
                    }

            /* 다이어리 작성 내용 디비에 저장하기 */
                    String cube_name = cubeSpinner.getSelectedItem().toString();
                    String image_path = "NULL";
                    if (croppedFile != null) {
                        image_path = croppedFile.getName();
                    }
                    String date = dateBtn.getText().toString();
                    String filter = filterSpinner.getSelectedItem().toString();
                    String content = contentText.getText().toString();
                    try {
                        cube_name = URLEncoder.encode(cube_name, "UTF-8");
                        date = URLEncoder.encode(date, "UTF-8");
                        filter = URLEncoder.encode(filter, "UTF-8");
                        content = URLEncoder.encode(content, "UTF-8");
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                    // 디비에 업로드
                    try {
                        new UploadDiary().execute(cube_name, image_path, date, filter, content).get();
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    } catch (ExecutionException e) {
                        e.printStackTrace();
                    }
                } else {
                    Toast.makeText(getContext(), "이미지를 선택해주시거나 내용을 작성해주세요.", Toast.LENGTH_SHORT).show();
                }
            } else {
                Toast.makeText(getContext(), "큐브를 등록 해주세요.", Toast.LENGTH_SHORT).show();
            }
        }
    };

    /* 다이어리 업로드용 쓰레드 */
    public class UploadDiary extends AsyncTask<String,Object,Boolean>{

        @Override
        protected Boolean doInBackground(String... params) {
            boolean result = false;
            try {
             /* URL 설정하고 접속 */
                URL url = new URL("http://fungdu0624.phps.kr/biocube/uploadDiary.php");
                HttpURLConnection http = (HttpURLConnection) url.openConnection();

            /* 전송모드 설정 */
                http.setDefaultUseCaches(false);
                http.setDoInput(true);  //서버에서 읽기 모드로 지정
                http.setDoOutput(true);    //서버에서 쓰기 모드로 지정
                http.setRequestMethod("POST");
                http.setRequestProperty("content-type", "application/x-www-form-urlencoded");   //서버에게 웹에서 <Form>으로 값이 넘어온 것과 같은 방식으로 처리하라는 걸 알려준다

            /* 서버로 값 전송 */
                StringBuffer buffer = new StringBuffer();
                buffer.append("userID").append("=").append(((UserMainActivity)getActivity()).userID).append("&");
                buffer.append("cube_name").append("=").append(params[0]).append("&");
                buffer.append("img_name").append("=").append(params[1]).append("&");
                buffer.append("date").append("=").append(params[2]).append("&");
                buffer.append("filter").append("=").append(params[3]).append("&");
                buffer.append("content").append("=").append(params[4]);

                OutputStreamWriter outStream = new OutputStreamWriter(http.getOutputStream(), "EUC-KR");
                PrintWriter writer = new PrintWriter(outStream);
                writer.write(buffer.toString());
                writer.flush();
                writer.close();

            /* 서버에서 전송 받기 */
                InputStream inStream = http.getInputStream();
                BufferedReader reader = new BufferedReader(new InputStreamReader(inStream, "UTF-8"));
                String str = reader.readLine();

                if(str.equals("success")){  // 업로드 성공
                    result = true;
                }
            } catch(MalformedURLException e) {
                e.printStackTrace();
            } catch(IOException e) {
                e.printStackTrace();
            }
            return result;
        }

        @Override
        public void onPostExecute(Boolean result) {
            super.onPostExecute(result);
            // Todo: doInBackground() 메소드 작업 끝난 후 처리해야할 작업..
            Intent intent;
            if(result) {   // 업로드 성공
                Toast.makeText(getContext(), "일지 작성이 완료되었습니다.", Toast.LENGTH_SHORT).show();
                /* 뷰들 초기화 */
                cubeSpinner.setSelection(0);
                croppedFile.delete();
                croppedFile = null;
                iv_UserPhoto.setImageDrawable(null);
                dateBtn.setText(getTodayDate());
                filterSpinner.setSelection(0);
                contentText.setText("");
                ((NewspeedFragment)(((UserMainActivity)UserMainActivity.context).mUserPagerAdapter.getItem(1))).new GetDataJSON().execute("http://fungdu0624.phps.kr/biocube/getnewspeed.php");
            } else {    // 업로드 실패
                Toast.makeText(getContext(), "일지 작성 실패", Toast.LENGTH_SHORT).show();
            }
        }
    }


    /* 날짜 설정 관련 메소드들 */
    private String getTodayDate() {
        Date todayDate = new Date();

        String month = "M", day = "d";
        if(todayDate.getMonth() > 9) {
            month += "M";
        }
        if(todayDate.getDate() > 9) {
            day += "d";
        }
        SimpleDateFormat todayDateFormat = new SimpleDateFormat("yyyy년 " + month + "월 " + day + "일");
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
        dateBtn.setText(sb.append(iYear+"년 ").append((iMonth+1) + "월 ").append(iDay+"일"));
    }

    class GetDevice extends AsyncTask<String, Void, String> {

        @Override
        protected String doInBackground(String... params) {

            String device= "";
            try {
         /* URL 설정하고 접속 */
                URL url = new URL("http://fungdu0624.phps.kr/biocube/getdevice.php");
                HttpURLConnection http = (HttpURLConnection) url.openConnection();

        /* 전송모드 설정 */
                http.setDefaultUseCaches(false);
                http.setDoInput(true);  //서버에서 읽기 모드로 지정
                http.setDoOutput(true);    //서버에서 쓰기 모드로 지정
                http.setRequestMethod("POST");
                http.setRequestProperty("content-type", "application/x-www-form-urlencoded");   //서버에게 웹에서 <Form>으로 값이 넘어온 것과 같은 방식으로 처리하라는 걸 알려준다

        /* 서버로 값 전송 */
                StringBuffer buffer = new StringBuffer();
                buffer.append("user_id").append("=").append(((UserMainActivity) getActivity()).userID).append("&");
                buffer.append("cubename").append("=").append(params[0].toString());


                OutputStreamWriter outStream = new OutputStreamWriter(http.getOutputStream(), "EUC-KR");
                PrintWriter writer = new PrintWriter(outStream);
                writer.write(buffer.toString());
                writer.flush();
                writer.close();

        /* 서버에서 전송 받기 */
                InputStream inStream = http.getInputStream();
                BufferedReader reader = new BufferedReader(new InputStreamReader(inStream, "UTF-8"));
                device = reader.readLine();


            } catch(MalformedURLException e) {
                e.printStackTrace();
            } catch(IOException e) {
                e.printStackTrace();
            }
            return device;
        }
    }

    //데이터 수신
    void beginListenForData() {
        final Handler handler = new Handler();

        readBufferPosition = 0;                 // 버퍼 내 수신 문자 저장 위치.
        readBuffer = new byte[1024];            // 수신 버퍼.

        // 문자열 수신 쓰레드.
        mWorkerThread = new Thread(new Runnable()
        {
            @Override
            public void run() {
                // interrupt() 메소드를 이용 스레드를 종료시키는 예제이다.
                // interrupt() 메소드는 하던 일을 멈추는 메소드이다.
                // isInterrupted() 메소드를 사용하여 멈추었을 경우 반복문을 나가서 스레드가 종료하게 된다.
                while(!Thread.currentThread().isInterrupted()) {
                    try {
                        // InputStream.available() : 다른 스레드에서 blocking 하기 전까지 읽은 수 있는 문자열 개수를 반환함.
                        int byteAvailable = ((UserMainActivity)getActivity()).mBluetooth.mInputStream.available();   // 수신 데이터 확인
                        if(byteAvailable > 0) {                        // 데이터가 수신된 경우.
                            byte[] packetBytes = new byte[byteAvailable];
                            // read(buf[]) : 입력스트림에서 buf[] 크기만큼 읽어서 저장 없을 경우에 -1 리턴.
                            ((UserMainActivity)getActivity()).mBluetooth.mInputStream.read(packetBytes);
                            for(int i=0; i<byteAvailable; i++) {
                                byte b = packetBytes[i];
                                if(b == mCharDelimiter) {
                                    byte[] encodedBytes = new byte[readBufferPosition];
                                    //  System.arraycopy(복사할 배열, 복사시작점, 복사된 배열, 붙이기 시작점, 복사할 개수)
                                    //  readBuffer 배열을 처음 부터 끝까지 encodedBytes 배열로 복사.
                                    System.arraycopy(readBuffer, 0, encodedBytes, 0, encodedBytes.length);

                                    final String data = new String(encodedBytes, "US-ASCII");
                                    readBufferPosition = 0;

                                    handler.post(new Runnable(){
                                        // 수신된 문자열 데이터에 대한 처리.
                                        @Override
                                        public void run() {
                                            // mStrDelimiter = '\n';
//                                            mEditReceive.setText(mEditReceive.getText().toString() + data+ mStrDelimiter);
                                            String[] datas = data.split(",");
                                            contentText.setText("대기온도 : "+ datas[1]+", 대기습도 : "+datas[2] +"\n" +"토양습도 : "+datas[3] + "\n" +contentText.getText().toString());
                                            try{
                                                ((UserMainActivity)getActivity()).mBluetooth.mSocket.close();
                                            }catch (Exception e){
                                                e.printStackTrace();
                                            }
                                            mWorkerThread.interrupt();
                                        }

                                    });
                                }
                                else {
                                    readBuffer[readBufferPosition++] = b;
                                }
                            }
                        }

                    } catch (Exception e) {    // 데이터 수신 중 오류 발생.
                        e.printStackTrace();
                    }
                }
            }

        });
        mWorkerThread.start();
    }

}
