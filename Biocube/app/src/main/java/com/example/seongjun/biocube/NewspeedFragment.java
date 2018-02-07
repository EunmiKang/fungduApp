package com.example.seongjun.biocube;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.Toast;

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
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutionException;


/**
 * A simple {@link Fragment} subclass.
 * Activities that contain this fragment must implement the
 * {@link NewspeedFragment.OnFragmentInteractionListener} interface
 * to handle interaction events.
 * Use the {@link NewspeedFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class NewspeedFragment extends Fragment {
    private OnFragmentInteractionListener mListener;

    private static final String TAG_DIARY="diaryinfo";
    private static final String TAG_NICKNAME = "nickname";
    private static final String TAG_IMG = "img";
    private static final String TAG_CONTENT ="content";
    private static final String TAG_ID ="id";
    private static final String TAG_DIARYNO = "diaryNo";
    private static final String TAG_LASTCOMMENT = "lastComment";
    private static final String TAG_COUNTCOMMENT = "countComment";
    private static final String TAG_LASTCOMMENTNICK = "lastCmtNick";
    private static final String TAG_DAY = "day";

    JSONArray diary = null;
    int authority;
    ListView list_newspeed;
    private TokenDBHelper helper;
    String nickname;
    ImageButton btn_filter;
    List filterItems;
    String id;

    SwipeRefreshLayout mSwipeRefreshLayout;

    public NewspeedFragment() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @return A new instance of fragment NewspeedFragment.
     */
    // TODO: Rename and change types and number of parameters
    public static NewspeedFragment newInstance() {
        NewspeedFragment fragment = new NewspeedFragment();
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
        mSwipeRefreshLayout = (SwipeRefreshLayout) view.findViewById(R.id.swipe);
        mSwipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                try {
                    new GetDataJSON().execute("http://fungdu0624.phps.kr/biocube/getnewspeed.php").get();
                } catch (InterruptedException e) {
                    e.printStackTrace();
                } catch (ExecutionException e) {
                    e.printStackTrace();
                }
                // 새로고침 완료
                mSwipeRefreshLayout.setRefreshing(false);
            }
        });

        /* Toolbar 설정 */
        Toolbar toolbar = (Toolbar) view.findViewById(R.id.toolbar_newspeed);
        ((AppCompatActivity)getActivity()).setSupportActionBar(toolbar);
        ActionBar actionBar = ((AppCompatActivity)getActivity()).getSupportActionBar();
        actionBar.setDisplayShowCustomEnabled(true);
        actionBar.setDisplayShowTitleEnabled(false);

        list_newspeed = (ListView) view.findViewById(R.id.list_newspeed);
        btn_filter = (ImageButton) view.findViewById(R.id.btn_filter);

        try {
            String[] userInfo = new GetUserInfo().execute(helper).get();
            nickname = userInfo[0];
            if (userInfo[1].equals("1")) {
                authority = 1;
                id = ((UserMainActivity)getActivity()).userID;
            } else if (userInfo[1].equals("2")) {
                authority = 2;
                id = ((ExpertMainActivity)getActivity()).expertID;
            } else {
                authority = 0;
                id = ((AdminMainActivity)getActivity()).adminID;
            }

        } catch (InterruptedException e) {
            e.printStackTrace();
        } catch (ExecutionException e) {
            e.printStackTrace();
        }

        btn_filter.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                try {
                    filterItems = new GetFilter().execute("http://fungdu0624.phps.kr/biocube/getFilterItems.php", id).get();
                    AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(NewspeedFragment.this.getContext());
                    final String[] filter = new String[filterItems.size()];
                    for(int i = 0; i<filterItems.size(); i++){
                        filter[i] = filterItems.get(i).toString();
                    }
                    alertDialogBuilder.setTitle("필터 선택");
                    alertDialogBuilder.setItems(filter, new DialogInterface.OnClickListener(){
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            new GetDataJSON().execute("http://fungdu0624.phps.kr/biocube/getNewspeedAsFilter.php", filter[which]);
                            Toast.makeText(NewspeedFragment.this.getContext(), filter[which] + "이 선택되었습니다.", Toast.LENGTH_SHORT).show();
                        }
                    });
                    AlertDialog alertDialog = alertDialogBuilder.create();
                    alertDialog.show();
                } catch (InterruptedException e) {
                    e.printStackTrace();
                } catch (ExecutionException e) {
                    e.printStackTrace();
                }
            }
        });
        new GetDataJSON().execute("http://fungdu0624.phps.kr/biocube/getnewspeed.php");
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


    class GetDataJSON extends AsyncTask<String, Void, List<DiaryItem>> {

        ProgressDialog asyncDialog = new ProgressDialog(getContext());
        @Override
        protected List<DiaryItem> doInBackground(String... params) {

            String uri = params[0];
            List<DiaryItem> diarylist = new ArrayList<DiaryItem>();

            try {
                URL url = new URL(uri);
                HttpURLConnection con = (HttpURLConnection) url.openConnection();
                con.setDefaultUseCaches(false);
                con.setDoInput(true);  //서버에서 읽기 모드로 지정
                con.setDoOutput(true);    //서버에서 쓰기 모드로 지정
                con.setRequestMethod("POST");
                con.setRequestProperty("content-type", "application/x-www-form-urlencoded");   //서버에게 웹에서 <Form>으로 값이 넘어온 것과 같은 방식으로 처리하라는 걸 알려준다


                if(params.length == 2){//filter를 선택했을때
                    StringBuffer buffer = new StringBuffer();
                    params[1] = URLEncoder.encode(params[1],"UTF-8");
                    buffer.append("filter").append("=").append(params[1].toString());

                    /* 서버로 값 전송 */
                    OutputStreamWriter outStream = new OutputStreamWriter(con.getOutputStream(), "EUC-KR");
                    PrintWriter writer = new PrintWriter(outStream);
                    writer.write(buffer.toString());
                    writer.flush();
                    writer.close();
                }
                /* 서버에서 전송 받음 */
                InputStream inStream = con.getInputStream();
                BufferedReader reader = new BufferedReader(new InputStreamReader(inStream, "UTF-8"));
                StringBuilder sb = new StringBuilder();

                String json;
                while((json = reader.readLine())!= null){
                    sb.append(json+"\n");
                }

                try {
                    JSONObject jsonObj = new JSONObject(sb.toString().trim());
                    diary = jsonObj.getJSONArray(TAG_DIARY);
                    Bitmap plantImg;

                    for (int i = 0; i < diary.length(); i++) {
                        JSONObject c = diary.getJSONObject(i);
                        String id = c.getString(TAG_ID);
                        if(!id.equals("")) {
                            String nickname = c.getString(TAG_NICKNAME);
                            String img = c.getString(TAG_IMG);
                            String content = c.getString(TAG_CONTENT);
                            int diaryNo = c.getInt(TAG_DIARYNO);
                            String lastComment = c.getString(TAG_LASTCOMMENT);
                            int countComment = c.getInt(TAG_COUNTCOMMENT);
                            String lastCmtNick = c.getString(TAG_LASTCOMMENTNICK);
                            String day = c.getString(TAG_DAY);
                            if (lastComment.equals("null")) {
                                lastComment = "댓글이 없습니다.";
                                lastCmtNick = "";
                            }
                            if (!img.equals("null") || (img.equals(""))) {
                                String readURL = "http://fungdu0624.phps.kr/biocube/users/" + id + "/" + img;
                                HttpURLConnection http = (HttpURLConnection) url.openConnection();
                                url = new URL(readURL);
                                http = (HttpURLConnection) url.openConnection();
                                http.connect();
                                //스트림생성
                                InputStream inStream2 = http.getInputStream();
                                //스트림에서 받은 데이터를 비트맵 변환
                                BitmapFactory.Options option = new BitmapFactory.Options();
                                option.inSampleSize = 2;
                                plantImg = BitmapFactory.decodeStream(inStream2, null, option);
                                diarylist.add(new DiaryItem(diaryNo, nickname, plantImg, content, lastComment, countComment, lastCmtNick, day));
                            } else {
                                plantImg = null;
                                diarylist.add(new DiaryItem(diaryNo, nickname, plantImg, content, lastComment, countComment, lastCmtNick, day));
                            }
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
        protected void onPreExecute() {
            asyncDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
            asyncDialog.setMessage("로딩중입니다..");

            // show dialog
            asyncDialog.show();
            super.onPreExecute();
        }
        @Override
        protected void onPostExecute(List<DiaryItem> result){
            list_newspeed.setAdapter(new DiaryManageAdapter(getContext(), nickname, result, authority, id));
            asyncDialog.dismiss();
        }
    }
    public void reset(){
        try {
            new GetDataJSON().execute("http://fungdu0624.phps.kr/biocube/getnewspeed.php").get();
        } catch (InterruptedException e) {
            e.printStackTrace();
        } catch (ExecutionException e) {
            e.printStackTrace();
        }
    }
}
