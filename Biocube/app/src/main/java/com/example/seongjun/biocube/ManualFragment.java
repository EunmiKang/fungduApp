package com.example.seongjun.biocube;

import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.view.ViewPager;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import me.relex.circleindicator.CircleIndicator;

/**
 * A simple {@link Fragment} subclass.
 * Activities that contain this fragment must implement the
 * {@link ManualFragment.OnFragmentInteractionListener} interface
 * to handle interaction events.
 * Use the {@link ManualFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class ManualFragment extends Fragment  {
    private ViewPager pager;
    private ManualsAdapter adapter;

    private OnFragmentInteractionListener mListener;

    public ManualFragment() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @return A new instance of fragment ManualFragment.
     */
    // TODO: Rename and change types and number of parameters
    public static ManualFragment newInstance() {
        ManualFragment fragment = new ManualFragment();
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
        View view = inflater.inflate(R.layout.fragment_manual, container, false);

        /* Toolbar 설정 */
        Toolbar toolbar = (Toolbar) view.findViewById(R.id.toolbar_manual);
        ((AppCompatActivity)getActivity()).setSupportActionBar(toolbar);
        ActionBar actionBar = ((AppCompatActivity)getActivity()).getSupportActionBar();
        actionBar.setDisplayShowCustomEnabled(true);
        //actionBar.setDisplayShowTitleEnabled(false);

        pager = (ViewPager) view.findViewById(R.id.pager_manual);
        adapter = new ManualsAdapter(inflater); // 처음에 매뉴얼들 띄우기 위해 필요한 어댑터 설정
        CircleIndicator indicator = (CircleIndicator) view.findViewById(R.id.indicator);

        new SettingManuals().execute(pager, adapter, indicator);

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
}
