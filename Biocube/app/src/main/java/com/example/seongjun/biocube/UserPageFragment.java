package com.example.seongjun.biocube;

import android.content.Intent;
import android.database.sqlite.SQLiteDatabase;
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
import android.widget.TextView;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.concurrent.ExecutionException;

/**
 * A simple {@link Fragment} subclass.
 * Activities that contain this fragment must implement the
 * {@link UserPageFragment.OnFragmentInteractionListener} interface
 * to handle interaction events.
 * Use the {@link UserPageFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class UserPageFragment extends Fragment {
    private OnFragmentInteractionListener mListener;

    private TokenDBHelper helper;

    TextView text_cubeNum;
    TextView text_diaryNum;
    TextView text_user_nickname;
    String nickname;


    public UserPageFragment() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @return A new instance of fragment UserPageFragment.
     */
    // TODO: Rename and change types and number of parameters
    public static UserPageFragment newInstance() {
        UserPageFragment fragment = new UserPageFragment();
        Bundle args = new Bundle();
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        helper = new TokenDBHelper(getActivity());
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_user_page, container, false);

        text_user_nickname = (TextView)view.findViewById(R.id.text_user_nickname);
        text_cubeNum = (TextView)view.findViewById(R.id.text_cubeNum);
        text_diaryNum = (TextView)view.findViewById(R.id.text_diaryNum);

        /* 기본 정보 설정 */
        settingNickname();

        try {
            String[] getList = new ReturnCubeList().execute(((UserMainActivity)getActivity()).userID).get();
            text_cubeNum.setText(getList[0]);

        } catch (InterruptedException e) {
            e.printStackTrace();
        } catch (ExecutionException e) {
            e.printStackTrace();
        }

        new getDiaryNum().execute();

        /* Toolbar 설정 */
        Toolbar toolbar = (Toolbar) view.findViewById(R.id.toolbar_user_page);
        ((AppCompatActivity)getActivity()).setSupportActionBar(toolbar);
        ActionBar actionBar = ((AppCompatActivity)getActivity()).getSupportActionBar();
        actionBar.setDisplayShowCustomEnabled(true);
        actionBar.setDisplayShowTitleEnabled(false);


        /* OnClickListener 설정 */
        view.findViewById(R.id.img_user_myCube).setOnClickListener(myCubeClickListener);
        view.findViewById(R.id.btn_user_myCube).setOnClickListener(myCubeClickListener);

        view.findViewById(R.id.img_user_changeInfo).setOnClickListener(changeInfoClickListener);
        view.findViewById(R.id.btn_user_changeInfo).setOnClickListener(changeInfoClickListener);

        view.findViewById(R.id.img_user_question).setOnClickListener(questionClickListener);
        view.findViewById(R.id.btn_user_question).setOnClickListener(questionClickListener);

        view.findViewById(R.id.img_user_logout).setOnClickListener(logoutClickListener);
        view.findViewById(R.id.btn_user_logout).setOnClickListener(logoutClickListener);

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

    public void mOnPopupClick(){
        //데이터 담아서 팝업(액티비티) 호출
        Intent intent = new Intent(getActivity(), PopQuestion.class);
        startActivity(intent);
    }

    public class getDiaryNum extends AsyncTask<Object,Object,Integer> {
        // 실제 params 부분에는 execute 함수에서 넣은 인자 값이 들어 있다.
        @Override
        public Integer doInBackground(Object... params) {
            try {
             /* URL 설정하고 접속 */
                URL url = new URL("http://fungdu0624.phps.kr/biocube/getuserdiarynum.php");
                HttpURLConnection http = (HttpURLConnection) url.openConnection();

            /* 전송모드 설정 */
                http.setDefaultUseCaches(false);
                http.setDoInput(true);  //서버에서 읽기 모드로 지정
                http.setDoOutput(true);    //서버에서 쓰기 모드로 지정
                http.setRequestMethod("POST");
                http.setRequestProperty("content-type", "application/x-www-form-urlencoded");   //서버에게 웹에서 <Form>으로 값이 넘어온 것과 같은 방식으로 처리하라는 걸 알려준다

            /* 서버로 값 전송 */
                StringBuffer buffer = new StringBuffer();
                buffer.append("userID").append("=").append(((UserMainActivity)getActivity()).userID);
                OutputStreamWriter outStream = new OutputStreamWriter(http.getOutputStream(), "EUC-KR");
                PrintWriter writer = new PrintWriter(outStream);
                writer.write(buffer.toString());
                writer.flush();
                writer.close();

            /* 서버에서 전송 받기 */
                InputStream inStream = http.getInputStream();
                BufferedReader reader = new BufferedReader(new InputStreamReader(inStream, "UTF-8"));
                String result = reader.readLine();

                if(result != null) {
                    return Integer.parseInt(result);
                }


            } catch(MalformedURLException e) {
                e.printStackTrace();
            } catch(IOException e) {
                e.printStackTrace();
            }
            //publishProgress(params);    // 중간 중간에 진행 상태 UI 를 업데이트 하기 위해 사용..
            return -1;
        }

        @Override
        public void onPostExecute(Integer result) {
            super.onPostExecute(result);
            // Todo: doInBackground() 메소드 작업 끝난 후 처리해야할 작업..
            text_diaryNum.setText(result.toString());
        }
    }


    /* OnClickListener 정의 */
    View.OnClickListener myCubeClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            Intent intent = new Intent(getContext(), CubeListActivity.class);
            intent.putExtra("id", ((UserMainActivity)getActivity()).userID);
            startActivity(intent);
        }
    };

    View.OnClickListener changeInfoClickListener = new View.OnClickListener(){
        @Override
        public void onClick(View v) {
            Intent intent;
            intent = new Intent(v.getContext(), ChangeInfoActivity.class);
            intent.putExtra("id", ((UserMainActivity)getActivity()).userID);
            startActivity(intent);
        }
    };

    View.OnClickListener questionClickListener = new View.OnClickListener(){
        @Override
        public void onClick(View v) {
            //데이터 담아서 팝업(액티비티) 호출
            mOnPopupClick();
        }
    };

    View.OnClickListener logoutClickListener = new View.OnClickListener() {
        public void onClick(View v) {
            SQLiteDatabase db = helper.getWritableDatabase();
            db.delete("TOKEN", "token is not null", null);

            Intent intent = new Intent(getActivity(), LoginActivity.class);
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK);
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            startActivity(intent);
        }
    };

    public void settingNickname() {
        try {
            String[] userInfo = new GetUserInfo().execute(helper).get();
            nickname = userInfo[0];
            text_user_nickname.setText(nickname);

        } catch (InterruptedException e) {
            e.printStackTrace();
        } catch (ExecutionException e) {
            e.printStackTrace();
        }
    }
    public void settingCubeNum(){
        String[] getList = new String[0];
        try {
            getList = new ReturnCubeList().execute(((UserMainActivity)getActivity()).userID).get();
        } catch (InterruptedException e) {
            e.printStackTrace();
        } catch (ExecutionException e) {
            e.printStackTrace();
        }
        text_cubeNum.setText(getList[0]);
    }
    public void settingDiaryNum(){
        new getDiaryNum().execute();
    }
}