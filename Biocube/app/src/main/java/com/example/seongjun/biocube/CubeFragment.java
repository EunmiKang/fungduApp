package com.example.seongjun.biocube;

import android.content.Intent;
import android.media.Image;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
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
            mCubeRegister.sendData("hello");
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
            mCubeRegister.checkBluetooth();
            mCubeRegister.connectToSelectedDevice(deviceNum, 1);
        }
    }
}
