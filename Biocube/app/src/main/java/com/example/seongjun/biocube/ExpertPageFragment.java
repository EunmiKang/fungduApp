package com.example.seongjun.biocube;

import android.content.Context;
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
import android.widget.Button;
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
 * {@link ExpertPageFragment.OnFragmentInteractionListener} interface
 * to handle interaction events.
 * Use the {@link ExpertPageFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class ExpertPageFragment extends Fragment {
    private OnFragmentInteractionListener mListener;

    private TokenDBHelper helper;
    TextView text_expert_nickname;
    TextView text_commentNum;
    String nickname;

    //id :((ExpertMainActivity)getActivity()).expertID

    public ExpertPageFragment() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @return A new instance of fragment ExpertPageFragment.
     */
    // TODO: Rename and change types and number of parameters
    public static ExpertPageFragment newInstance() {
        ExpertPageFragment fragment = new ExpertPageFragment();
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
        View view = inflater.inflate(R.layout.fragment_expert_page, container, false);

        text_expert_nickname = (TextView)view.findViewById(R.id.text_expert_nickname);
        text_commentNum = (TextView) view.findViewById(R.id.text_commentNum);

        try {
            String[] userInfo = new GetUserInfo().execute(helper).get();
            nickname = userInfo[0];
            text_expert_nickname.setText(nickname);

        } catch (InterruptedException e) {
            e.printStackTrace();
        } catch (ExecutionException e) {
            e.printStackTrace();
        }

        new getCommentNum().execute();
        /* Toolbar 설정 */
        Toolbar toolbar = (Toolbar) view.findViewById(R.id.toolbar_expert_page);
        ((AppCompatActivity)getActivity()).setSupportActionBar(toolbar);
        ActionBar actionBar = ((AppCompatActivity)getActivity()).getSupportActionBar();
        actionBar.setDisplayShowCustomEnabled(true);
        actionBar.setDisplayShowTitleEnabled(false);

        /* 버튼들 리스너 설정 */
        Button myCommentBtn = (Button) view.findViewById(R.id.btn_expert_myComment);
        myCommentBtn.setOnClickListener(myCommentClickListener);

        Button changeInfoBtn = (Button) view.findViewById(R.id.btn_expert_changeInfo);
        changeInfoBtn.setOnClickListener(new View.OnClickListener(){

            @Override
            public void onClick(View v) {
                Intent intent;
                intent = new Intent(v.getContext(), ChangeInfoActivity.class);
                intent.putExtra("id", ((ExpertMainActivity)getActivity()).expertID);
                startActivity(intent);
            }
        });

        Button questionBtn = (Button) view.findViewById(R.id.btn_expert_question);
        questionBtn.setOnClickListener(questionClickListener);

        Button logoutBtn = (Button) view.findViewById(R.id.btn_expert_logout);
        logoutBtn.setOnClickListener(logoutClickListener);

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

    /* 버튼 리스너들 */
    Button.OnClickListener myCommentClickListener = new Button.OnClickListener() {
        public void onClick(View v) {
            Intent intent = new Intent(getContext(), CommentListActivity.class);
            intent.putExtra("id", ((ExpertMainActivity)getActivity()).expertID);
            startActivity(intent);
        }
    };

    Button.OnClickListener questionClickListener = new Button.OnClickListener(){
        @Override
        public void onClick(View v) {
            //데이터 담아서 팝업(액티비티) 호출
            Intent intent = new Intent(getActivity(), PopQuestion.class);
            intent.putExtra("data", "Test Popup");
            startActivityForResult(intent, 1);
        }
    };

    Button.OnClickListener logoutClickListener = new Button.OnClickListener() {
        public void onClick(View v) {
            SQLiteDatabase db = helper.getWritableDatabase();
            db.delete("TOKEN", "token is not null", null);

            Intent intent = new Intent(getActivity(), LoginActivity.class);
            startActivity(intent);
        }
    };


    public class getCommentNum extends AsyncTask<Object,Object,Integer> {
        // 실제 params 부분에는 execute 함수에서 넣은 인자 값이 들어 있다.
        @Override
        public Integer doInBackground(Object... params) {
            try {
             /* URL 설정하고 접속 */
                URL url = new URL("http://fungdu0624.phps.kr/biocube/getcommentnum.php");
                HttpURLConnection http = (HttpURLConnection) url.openConnection();

            /* 전송모드 설정 */
                http.setDefaultUseCaches(false);
                http.setDoInput(true);  //서버에서 읽기 모드로 지정
                http.setDoOutput(true);    //서버에서 쓰기 모드로 지정
                http.setRequestMethod("POST");
                http.setRequestProperty("content-type", "application/x-www-form-urlencoded");   //서버에게 웹에서 <Form>으로 값이 넘어온 것과 같은 방식으로 처리하라는 걸 알려준다

            /* 서버로 값 전송 */
                StringBuffer buffer = new StringBuffer();
                buffer.append("nickname").append("=").append(nickname);
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
            text_commentNum.setText(result.toString());
        }
    }
}
