package com.example.seongjun.biocube;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
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
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

/**
 * A simple {@link Fragment} subclass.
 * Activities that contain this fragment must implement the
 * {@link AdminNewspeedFragment.OnFragmentInteractionListener} interface
 * to handle interaction events.
 * Use the {@link AdminNewspeedFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class AdminNewspeedFragment extends Fragment {
    private OnFragmentInteractionListener mListener;

    public AdminNewspeedFragment() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @return A new instance of fragment AdminNewspeedFragment.
     */
    // TODO: Rename and change types and number of parameters
    public static AdminNewspeedFragment newInstance() {
        AdminNewspeedFragment fragment = new AdminNewspeedFragment();
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
        View view = inflater.inflate(R.layout.fragment_admin_newspeed, container, false);

        /* Toolbar 설정 */
        Toolbar toolbar = (Toolbar) view.findViewById(R.id.toolbar_admin_newspeed);
        ((AppCompatActivity)getActivity()).setSupportActionBar(toolbar);
        ActionBar actionBar = ((AppCompatActivity)getActivity()).getSupportActionBar();
        actionBar.setDisplayShowCustomEnabled(true);
        actionBar.setDisplayShowTitleEnabled(false);

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

    public class DiaryItem {
        private String writerNickname;
        private Bitmap diaryImg;
        private String diaryContent;

        public DiaryItem(String nickname, Bitmap diaryImg, String diaryContent) {
            this.writerNickname = nickname;
            this.diaryImg = diaryImg;
            this.diaryContent = diaryContent;
        }

        public String getWriterNickname() {
            return writerNickname;
        }
        public void setWriterNickname(String writerNickname) {
            this.writerNickname = writerNickname;
        }

        public Bitmap getDiaryImg() {
            return diaryImg;
        }
        public void setDiaryImg(Bitmap diaryImg) {
            this.diaryImg = diaryImg;
        }

        public String getDiaryContent() {
            return diaryContent;
        }
        public void setDiaryContent(String diaryContent) {
            this.diaryContent = diaryContent;
        }
    }

    public class GetDiaryList extends AsyncTask<Object, Object, List<DiaryItem>> {

        @Override
        protected List<DiaryItem> doInBackground(Object[] objects) {
            List<DiaryItem> returnList = new ArrayList<DiaryItem>();

            try {
                /* URL 설정하고 접속 */
                URL url = new URL("http://fungdu0624.phps.kr/biocube/returnDiaryList.php");
                HttpURLConnection http = (HttpURLConnection) url.openConnection();

                /* 전송모드 설정 */
                http.setDefaultUseCaches(false);
                http.setDoInput(true);  //서버에서 읽기 모드로 지정
                http.setDoOutput(true);    //서버에서 쓰기 모드로 지정
                http.setRequestMethod("POST");
                http.setRequestProperty("content-type", "application/x-www-form-urlencoded");   //서버에게 웹에서 <Form>으로 값이 넘어온 것과 같은 방식으로 처리하라는 걸 알려준다

                /* 서버에서 전송 받기 */
                String[] diaryList;
                InputStream inStream = http.getInputStream();
                BufferedReader reader = new BufferedReader(new InputStreamReader(inStream, "UTF-8"));
                String readResult = reader.readLine();
                if(readResult != null) {
                    diaryList = readResult.split("|");
                } else {
                    diaryList = new String[0];
                }
                inStream.close();
                http.disconnect();

                /* 리턴할 리스트에 읽어들인 것으로 처리 */


            } catch(MalformedURLException e) {
                e.printStackTrace();
            } catch(IOException e) {
                e.printStackTrace();
            }

            return returnList;
        }
    }
}
