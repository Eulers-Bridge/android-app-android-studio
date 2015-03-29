package com.eulersbridge.isegoria;


import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.DisplayMetrics;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

public class TaskDetailProgressFragment extends Fragment {
    private View rootView;

    private float dpWidth;
    private float dpHeight;

    private Isegoria isegoria;
    private Network network;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        DisplayMetrics displayMetrics = getActivity().getResources().getDisplayMetrics();
        this.isegoria = (Isegoria) getActivity().getApplication();
        rootView = inflater.inflate(R.layout.task_detail_fragment, container, false);

        dpWidth = displayMetrics.widthPixels / displayMetrics.density;
        dpHeight = displayMetrics.heightPixels / displayMetrics.density;

        MainActivity mainActivity = (MainActivity) getActivity();
        network = mainActivity.getIsegoriaApplication().getNetwork();


        return rootView;
    }
}