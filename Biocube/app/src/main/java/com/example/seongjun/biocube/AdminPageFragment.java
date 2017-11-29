package com.example.seongjun.biocube;

import android.content.Context;
import android.content.Intent;
import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;
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
import android.widget.Toast;

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

        /* 버튼들 리스너 설정 */
        Button manualManageBtn = (Button) view.findViewById(R.id.btn_admin_manualManage);
        manualManageBtn.setOnClickListener(manaualManageClickListener);

        Button myCommentBtn = (Button) view.findViewById(R.id.btn_admin_mycomment);
        myCommentBtn.setOnClickListener(myCommentClickListener);

        Button myCubeBtn = (Button) view.findViewById(R.id.btn_admin_mycube);
        myCubeBtn.setOnClickListener(myCubeClickListener);

        Button logoutBtn = (Button) view.findViewById(R.id.btn_admin_logout);
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
    Button.OnClickListener manaualManageClickListener = new Button.OnClickListener() {
        public void onClick(View v) {

        }
    };

    Button.OnClickListener myCommentClickListener = new Button.OnClickListener() {
        public void onClick(View v) {
            Intent intent = new Intent(getContext(), CommentListActivity.class);
            intent.putExtra("id", ((AdminMainActivity)getActivity()).adminID);
            startActivity(intent);
        }
    };

    Button.OnClickListener myCubeClickListener = new Button.OnClickListener() {
        public void onClick(View v) {
            Intent intent = new Intent(getContext(), CubeListActivity.class);
            intent.putExtra("id", ((AdminMainActivity)getActivity()).adminID);
            startActivity(intent);
        }
    };

    Button.OnClickListener logoutClickListener = new Button.OnClickListener() {
        public void onClick(View v) {
            SQLiteDatabase db = helper.getWritableDatabase();
            db.delete("TOKEN", "token is not null", null);

            Intent intent = new Intent(getContext(), LoginActivity.class);
            startActivity(intent);
        }
    };
}
