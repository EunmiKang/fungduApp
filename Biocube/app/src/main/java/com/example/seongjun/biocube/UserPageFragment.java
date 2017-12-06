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
import android.widget.ArrayAdapter;
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
import java.util.ArrayList;
import java.util.List;

import static android.app.Activity.RESULT_OK;


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

        text_cubeNum = (TextView)view.findViewById(R.id.text_cubeNum);
        text_diaryNum = (TextView)view.findViewById(R.id.text_diaryNum);

        /* Toolbar 설정 */
        Toolbar toolbar = (Toolbar) view.findViewById(R.id.toolbar_user_page);
        ((AppCompatActivity)getActivity()).setSupportActionBar(toolbar);
        ActionBar actionBar = ((AppCompatActivity)getActivity()).getSupportActionBar();
        actionBar.setDisplayShowCustomEnabled(true);
        actionBar.setDisplayShowTitleEnabled(false);

        Button logoutBtn = (Button) view.findViewById(R.id.btn_user_logout);
        logoutBtn.setOnClickListener(logoutClickListener);

        Button myCube = (Button) view.findViewById(R.id.btn_user_myCube);
        myCube.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getContext(), CubeListActivity.class);
                intent.putExtra("id", ((UserMainActivity)getActivity()).userID);
                startActivity(intent);
            }
        });

        Button changeInfo = (Button) view.findViewById(R.id.btn_user_changeInfo);
        changeInfo.setOnClickListener(new View.OnClickListener(){

            @Override
            public void onClick(View v) {
                Intent intent;
                intent = new Intent(v.getContext(), ChangeInfoActivity.class);
                intent.putExtra("id", ((UserMainActivity)getActivity()).userID);
                startActivity(intent);
            }
        });

        Button question = (Button) view.findViewById(R.id.btn_user_question);
        question.setOnClickListener(questionClickListener);

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

    Button.OnClickListener logoutClickListener = new Button.OnClickListener() {
        public void onClick(View v) {
            SQLiteDatabase db = helper.getWritableDatabase();
            db.delete("TOKEN", "token is not null", null);

            Intent intent = new Intent(getActivity(), LoginActivity.class);
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK);
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            startActivity(intent);
        }
    };

    Button.OnClickListener questionClickListener = new Button.OnClickListener(){
        @Override
        public void onClick(View v) {
            //데이터 담아서 팝업(액티비티) 호출
            mOnPopupClick();
        }
    };

    public void mOnPopupClick(){
        //데이터 담아서 팝업(액티비티) 호출
        Intent intent = new Intent(getActivity(), PopQuestion.class);
        intent.putExtra("data", "Test Popup");
        startActivityForResult(intent, 1);
    }

}

