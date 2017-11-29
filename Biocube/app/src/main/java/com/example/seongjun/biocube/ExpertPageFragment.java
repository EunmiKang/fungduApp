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

        /* Toolbar 설정 */
        Toolbar toolbar = (Toolbar) view.findViewById(R.id.toolbar_expert_page);
        ((AppCompatActivity)getActivity()).setSupportActionBar(toolbar);
        ActionBar actionBar = ((AppCompatActivity)getActivity()).getSupportActionBar();
        actionBar.setDisplayShowCustomEnabled(true);
        actionBar.setDisplayShowTitleEnabled(false);

        Button changeInfo = (Button) view.findViewById(R.id.btn_expert_changeInfo);
        changeInfo.setOnClickListener(new View.OnClickListener(){

            @Override
            public void onClick(View v) {
                Intent intent;
                intent = new Intent(v.getContext(), ChangeInfoActivity.class);
                startActivity(intent);
            }
        });

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

    Button.OnClickListener logoutClickListener = new Button.OnClickListener() {
        public void onClick(View v) {
            SQLiteDatabase db = helper.getWritableDatabase();
            db.delete("TOKEN", "token is not null", null);

            Intent intent = new Intent(getActivity(), LoginActivity.class);
            startActivity(intent);
        }
    };
}
