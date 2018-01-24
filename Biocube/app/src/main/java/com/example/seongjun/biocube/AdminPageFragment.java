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
import android.widget.Button;
import android.widget.TextView;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;

/**
 * A simple {@link Fragment} subclass.
 * Activities that contain this fragment must implement the
 * {@link AdminPageFragment.OnFragmentInteractionListener} interface
 * to handle interaction events.
 * Use the {@link AdminPageFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class AdminPageFragment extends Fragment {
    private OnFragmentInteractionListener mListener;

    private TokenDBHelper helper;

    TextView countCubeView, countExpertView, countUserView;

    public AdminPageFragment() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @return A new instance of fragment AdminPageFragment.
     */
    // TODO: Rename and change types and number of parameters
    public static AdminPageFragment newInstance() {
        AdminPageFragment fragment = new AdminPageFragment();
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
        View view = inflater.inflate(R.layout.fragment_admin_page, container, false);

        /* Toolbar 설정 */
        Toolbar toolbar = (Toolbar) view.findViewById(R.id.toolbar_admin_page);
        ((AppCompatActivity)getActivity()).setSupportActionBar(toolbar);
        ActionBar actionBar = ((AppCompatActivity)getActivity()).getSupportActionBar();
        actionBar.setDisplayShowCustomEnabled(true);
        actionBar.setDisplayShowTitleEnabled(false);

        /* 정보창 설정 */
        countCubeView = (TextView) view.findViewById(R.id.text_admin_cubeNum);
        countExpertView = (TextView) view.findViewById(R.id.text_admin_expertNum);
        countUserView = (TextView) view.findViewById(R.id.text_admin_userNum);
        new SettingAdminPage().execute();

        /* OnClickListener 설정 */
        view.findViewById(R.id.img_admin_mycomment).setOnClickListener(myCommentClickListener);
        view.findViewById(R.id.btn_admin_mycomment).setOnClickListener(myCommentClickListener);

        view.findViewById(R.id.img_admin_mycube).setOnClickListener(myCubeClickListener);
        view.findViewById(R.id.btn_admin_mycube).setOnClickListener(myCubeClickListener);

        view.findViewById(R.id.img_admin_manualManage).setOnClickListener(manaualManageClickListener);
        view.findViewById(R.id.btn_admin_manualManage).setOnClickListener(manaualManageClickListener);

        view.findViewById(R.id.img_admin_logout).setOnClickListener(logoutClickListener);
        view.findViewById(R.id.btn_admin_logout).setOnClickListener(logoutClickListener);

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


    /* OnClickListener 정의 */
    View.OnClickListener manaualManageClickListener = new View.OnClickListener() {
        public void onClick(View v) {
            Intent intent = new Intent(getContext(), ManualManageActivity.class);
            startActivity(intent);
        }
    };

    View.OnClickListener myCommentClickListener = new View.OnClickListener() {
        public void onClick(View v) {
            Intent intent = new Intent(getContext(), CommentListActivity.class);
            intent.putExtra("id", ((AdminMainActivity)getActivity()).adminID);
            startActivity(intent);
        }
    };

    View.OnClickListener myCubeClickListener = new View.OnClickListener() {
        public void onClick(View v) {
            Intent intent = new Intent(getContext(), CubeListActivity.class);
            intent.putExtra("id", ((AdminMainActivity)getActivity()).adminID);
            startActivity(intent);
        }
    };

    View.OnClickListener logoutClickListener = new View.OnClickListener() {
        public void onClick(View v) {
            SQLiteDatabase db = helper.getWritableDatabase();
            db.delete("TOKEN", "token is not null", null);

            Intent intent = new Intent(getContext(), LoginActivity.class);
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK);
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            startActivity(intent);
        }
    };


    /* 쓰레드 */
    public class SettingAdminPage extends AsyncTask<String,Object,String[]> {
        @Override
        protected String[] doInBackground(String... params) {
            String[] resultList = new String[3];
            try {
                /* URL 설정하고 접속 */
                URL url = new URL("http://fungdu0624.phps.kr/biocube/settingAdminPage.php");
                HttpURLConnection http = (HttpURLConnection) url.openConnection();

                /* 전송모드 설정 */
                http.setDefaultUseCaches(false);
                http.setDoInput(true);  //서버에서 읽기 모드로 지정
                http.setDoOutput(true);    //서버에서 쓰기 모드로 지정
                http.setRequestMethod("POST");
                http.setRequestProperty("content-type", "application/x-www-form-urlencoded");   //서버에게 웹에서 <Form>으로 값이 넘어온 것과 같은 방식으로 처리하라는 걸 알려준다

                /* 서버에서 전송 받기 */
                InputStream inStream = http.getInputStream();
                BufferedReader reader = new BufferedReader(new InputStreamReader(inStream, "UTF-8"));
                String result = reader.readLine();
                resultList = result.split(",");

                inStream.close();
                http.disconnect();

            } catch (MalformedURLException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            }

            return resultList;
        }

        @Override
        public void onPostExecute(String[] result) {
            super.onPostExecute(result);
            // Todo: doInBackground() 메소드 작업 끝난 후 처리해야할 작업..
            countCubeView.setText(result[0]);
            countExpertView.setText(result[2]);
            countUserView.setText(result[1]);
        }
    }
}
