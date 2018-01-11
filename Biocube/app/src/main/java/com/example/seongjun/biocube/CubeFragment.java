package com.example.seongjun.biocube;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.Intent;
import android.media.Image;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.app.Fragment;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ExecutionException;



/**
 * A simple {@link Fragment} subclass.
 * Activities that contain this fragment must implement the
 * {@link CubeFragment.OnFragmentInteractionListener} interface
 * to handle interaction events.
 * Use the {@link CubeFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class CubeFragment extends Fragment {

    private OnFragmentInteractionListener mListener;
    String id;
    Spinner spinner_cubeName;
    CubeRegister mCubeRegister = new CubeRegister();
    Bluetooth mBluetooth = new Bluetooth();
    Set<BluetoothDevice> mDevices;
    BluetoothAdapter mBluetoothAdapter;
    int mPariedDeviceCount = 0;

    TextView text_temper;
    TextView text_humi_air;
    TextView text_humi_soil;
    TextView text_motor;
    TextView text_led;
    int state_motor = 0;

    private int readBufferPosition;
    byte[] readBuffer;
    Thread mWorkerThread = null;
    InputStream mInputStream = null;
    char mCharDelimiter =  '\n';
//    int state_led = 0;
    String stateMotor = "";

    public CubeFragment() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @return A new instance of fragment CubeFragment.
     */
    // TODO: Rename and change types and number of parameters
    public static CubeFragment newInstance() {
        CubeFragment fragment = new CubeFragment();
        Bundle args = new Bundle();
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);


    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_cube, container, false);
        spinner_cubeName = (Spinner) view.findViewById(R.id.spinner_cube_cubeselect);
        view.findViewById(R.id.btn_connect).setOnClickListener(connectClickListener);
        view.findViewById(R.id.btn_led).setOnClickListener(setLedClickListener);
        view.findViewById(R.id.btn_pump).setOnClickListener(setPumpClickListener);
        text_temper = (TextView) view.findViewById(R.id.text_temp);
        text_humi_air = (TextView) view.findViewById(R.id.text_humi_air);
        text_humi_soil = (TextView) view.findViewById(R.id.text_humi_soil);
        text_motor = (TextView) view.findViewById(R.id.text_motor);
        text_led = (TextView) view.findViewById(R.id.text_led);
        mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();

        try {
            id = new GetId().execute(getActivity()).get();
        } catch (InterruptedException e) {
            e.printStackTrace();
        } catch (ExecutionException e) {
            e.printStackTrace();
        }

        /* Toolbar 설정 */
        Toolbar toolbar = (Toolbar) view.findViewById(R.id.toolbar_cube);
        ((AppCompatActivity)getActivity()).setSupportActionBar(toolbar);
        ActionBar actionBar = ((AppCompatActivity)getActivity()).getSupportActionBar();
        actionBar.setDisplayShowCustomEnabled(true);
        actionBar.setDisplayShowTitleEnabled(false);

        Button registCube = (Button) view.findViewById(R.id.btn_cube_regist);

        registCube.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent;
                intent = new Intent(v.getContext(), CubeRegister.class);
                startActivity(intent);
            }
        });

        String[] cubeList;
        try {
            String[] getList = new ReturnCubeList().execute(id).get();
            cubeList = new String[getList.length-1];
            for(int i=0; i<getList.length-1; i++) {
                cubeList[i] = getList[i+1].toString();
            }
            ArrayAdapter<String> dataAdapter = new ArrayAdapter<String>(getContext(), android.R.layout.simple_spinner_item, cubeList);
            dataAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
            spinner_cubeName.setAdapter(dataAdapter);
        } catch (InterruptedException e) {
            e.printStackTrace();
        } catch (ExecutionException e) {
            e.printStackTrace();
        }

        return view;
    }

    ImageButton.OnClickListener setLedClickListener = new ImageButton.OnClickListener(){//LED 버튼 눌렀을 때
        @Override
        public void onClick(View v) {
            mBluetooth.sendData("hello");

        }
    };

    ImageButton.OnClickListener setPumpClickListener = new ImageButton.OnClickListener(){

        @Override
        public void onClick(View v) {
            mBluetooth.sendData("pump");
//            text_motor.setText(mCubeRegister.getStateMotor());
//                text_motor.setText("MOTOR ON");
        }
    };


    Button.OnClickListener connectClickListener= new Button.OnClickListener(){
        @Override
        public void onClick(View v) {
            String selectedCube = spinner_cubeName.getSelectedItem().toString();
            try{
                selectedCube = URLEncoder.encode(selectedCube,"UTF-8");
            } catch(Exception e) {
                e.printStackTrace();
            }
            new GetDevice().execute(selectedCube);
        }
    };


    // TODO: Rename method, update argument and hook method into UI event
    public void onButtonPressed(Uri uri) {
        if (mListener != null) {
            mListener.onFragmentInteraction(uri);
        }
    }

    @Override
    public void onDetach() {
        try{
            mBluetooth.mSocket.close();
        }catch (Exception e){
            e.printStackTrace();
        }
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
                buffer.append("user_id").append("=").append(id).append("&");
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

        @Override
        protected void onPostExecute(String result){
            String deviceNum = result;
            switch (mBluetooth.checkBluetooth(getContext())){
                case 0: Toast.makeText(getContext(), "기기가 블루투스를 지원하지 않습니다.", Toast.LENGTH_LONG).show();
                    break;
                case 1: Toast.makeText(getContext(), "현재 블루투스가 비활성 상태입니다.", Toast.LENGTH_LONG).show();
                    Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
                    startActivityForResult(enableBtIntent, mBluetooth.REQUEST_ENABLE_BT);
                    break;
                case 2: mDevices = mBluetoothAdapter.getBondedDevices();
                mPariedDeviceCount = mDevices.size();
            }
            BluetoothDevice device = mBluetoothAdapter.getRemoteDevice(deviceNum);
            if(mBluetooth.connectToSelectedDevice(deviceNum, mDevices, device)){
                beginListenForData();
                mBluetooth.sendData("connect");
            }else{
                Toast.makeText(getContext(), "블루투스 연결 중 오류가 발생했습니다.", Toast.LENGTH_LONG).show();
            }

//            Intent intent = new Intent();
//            if(mCubeRegister.connectToSelectedDevice(deviceNum, 1)){
//                intent.setAction(BluetoothDevice.ACTION_ACL_CONNECTED);
//            }
//            else{
//                intent.setAction("connectfail");
//            }
//            mCubeRegister.mBluetoothStateReceiver.onReceive(getContext(),intent);
//            mCubeRegister.beginListenForData();
//            mCubeRegister.mWorkerThread.start();
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
                        int byteAvailable = mBluetooth.mInputStream.available();   // 수신 데이터 확인
                        if(byteAvailable > 0) {                        // 데이터가 수신된 경우.
                            byte[] packetBytes = new byte[byteAvailable];
                            // read(buf[]) : 입력스트림에서 buf[] 크기만큼 읽어서 저장 없을 경우에 -1 리턴.
                            mBluetooth.mInputStream.read(packetBytes);
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
                                            if(datas[0].equals("PUMPON")){
                                                text_motor.setText("ON");
                                            }
                                            else if(datas[0].equals("PUMPOFF")){
                                                text_motor.setText("OFF");
                                            }
                                            else if(datas[0].equals("TEMPER")){
                                                text_temper.setText(datas[1]);
                                                text_humi_air.setText(datas[2]);
                                            }
                                        }

                                    });
                                }
                                else {
                                    readBuffer[readBufferPosition++] = b;
                                }
                            }
                        }

                    } catch (Exception e) {    // 데이터 수신 중 오류 발생.
//                        Toast.makeText(getContext(), "데이터 수신 중 오류가 발생 했습니다.", Toast.LENGTH_LONG).show();
//                        getActivity().finish();            // App 종료.
                    }
                }
            }

        });
        mWorkerThread.start();
    }

    @Override
    public void onDestroy() {
        try{
            mWorkerThread.interrupt(); // 데이터 수신 쓰레드 종료
            mInputStream.close();
            mBluetooth.mSocket.close();
        }catch(Exception e){}
        super.onDestroy();
    }

}
