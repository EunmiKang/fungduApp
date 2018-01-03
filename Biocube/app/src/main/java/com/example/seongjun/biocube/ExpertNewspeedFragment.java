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
import android.widget.ListView;
import android.widget.TextView;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutionException;

/**
 * A simple {@link Fragment} subclass.
 * Activities that contain this fragment must implement the
 * {@link ExpertNewspeedFragment.OnFragmentInteractionListener} interface
 * to handle interaction events.
 * Use the {@link ExpertNewspeedFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class ExpertNewspeedFragment extends Fragment {
    private OnFragmentInteractionListener mListener;

    private static final String TAG_DIARY="diaryinfo";
    private static final String TAG_NICKNAME = "nickname";
    private static final String TAG_IMG = "img";
    private static final String TAG_CONTENT ="content";
    private static final String TAG_ID ="id";

    JSONArray diary = null;
    int authority;
    ListView list_newspeed;
    private TokenDBHelper helper;

    public ExpertNewspeedFragment() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @return A new instance of fragment ExpertNewspeedFragment.
     */
    // TODO: Rename and change types and number of parameters
    public static ExpertNewspeedFragment newInstance() {
        ExpertNewspeedFragment fragment = new ExpertNewspeedFragment();
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
        View view = inflater.inflate(R.layout.fragment_newspeed, container, false);

        /* Toolbar 설정 */
        Toolbar toolbar = (Toolbar) view.findViewById(R.id.toolbar_newspeed);
        ((AppCompatActivity)getActivity()).setSupportActionBar(toolbar);
        ActionBar actionBar = ((AppCompatActivity)getActivity()).getSupportActionBar();
        actionBar.setDisplayShowCustomEnabled(true);
        actionBar.setDisplayShowTitleEnabled(false);

        list_newspeed = (ListView) view.findViewById(R.id.list_newspeed);

        try {

            String[] userInfo = new GetUserInfo().execute(helper).get();
            if (userInfo[1].equals("1")) {
                authority = 1;
            } else if (userInfo[1].equals("2")) {
                authority = 2;
            } else {
                authority = 0;
            }

        } catch (InterruptedException e) {
            e.printStackTrace();
        } catch (ExecutionException e) {
            e.printStackTrace();
        }


        getData("http://fungdu0624.phps.kr/biocube/getnewspeed.php");

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

    public void getData(String url){
        class GetDataJSON extends AsyncTask<String, Void, List<DiaryItem>> {

            @Override
            protected List<DiaryItem> doInBackground(String... params) {

                String uri = params[0];
                List<DiaryItem> diarylist = new ArrayList<DiaryItem>();

                BufferedReader bufferedReader = null;
                try {
                    URL url = new URL(uri);
                    HttpURLConnection con = (HttpURLConnection) url.openConnection();
                    con.setDefaultUseCaches(false);
                    con.setDoInput(true);  //서버에서 읽기 모드로 지정
                    con.setDoOutput(true);    //서버에서 쓰기 모드로 지정

            /* 서버로 값 전송 */
                    bufferedReader = new BufferedReader(new InputStreamReader(con.getInputStream()));
                    StringBuilder sb = new StringBuilder();

                    String json;
                    while((json = bufferedReader.readLine())!= null){
                        sb.append(json+"\n");
                    }

                    try {
                        JSONObject jsonObj = new JSONObject(sb.toString().trim());
                        diary = jsonObj.getJSONArray(TAG_DIARY);
                        Bitmap plantImg;

                        for (int i = 0; i < diary.length(); i++) {
                            JSONObject c = diary.getJSONObject(i);
                            String nickname = c.getString(TAG_NICKNAME);
                            String img = c.getString(TAG_IMG);
                            String content = c.getString(TAG_CONTENT);
                            String id = c.getString(TAG_ID);

                            if(!img.equals("null")) {
                                String readURL = "http://fungdu0624.phps.kr/biocube/users/" + id + "/" + img;
                                HttpURLConnection http = (HttpURLConnection) url.openConnection();
                                url = new URL(readURL);
                                http = (HttpURLConnection) url.openConnection();
                                http.connect();
                                //스트림생성
                                InputStream inStream = http.getInputStream();
                                //스트림에서 받은 데이터를 비트맵 변환
                                plantImg = BitmapFactory.decodeStream(inStream);
                                diarylist.add(new DiaryItem(nickname,plantImg,content));
                            }
                            else{
                                plantImg = null;
                                diarylist.add(new DiaryItem(nickname,plantImg,content));
                            }

                        }
                    }catch (JSONException e) {
                        e.printStackTrace();
                    }
                    return diarylist;
                }catch(Exception e){
                    return null;
                }

            }

            @Override
            protected void onPostExecute(List<DiaryItem> result){
                list_newspeed.setAdapter(new DiaryManageAdapter(getContext(), result, authority));
            }
        }
        GetDataJSON g = new GetDataJSON();
        g.execute(url);
    }
}
