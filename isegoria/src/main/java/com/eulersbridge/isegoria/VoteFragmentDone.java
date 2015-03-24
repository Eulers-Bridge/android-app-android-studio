package com.eulersbridge.isegoria;

import android.app.ActionBar;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemSelectedListener;

import com.actionbarsherlock.app.SherlockFragment;
import com.actionbarsherlock.app.SherlockFragmentActivity;

public class VoteFragmentDone extends SherlockFragment implements OnItemSelectedListener {
    private View rootView;

    public VoteFragmentDone() {

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        rootView = inflater.inflate(R.layout.vote_fragment_done, container, false);
        ((SherlockFragmentActivity) getActivity()).getSupportActionBar().setNavigationMode(ActionBar.NAVIGATION_MODE_STANDARD);
        getActivity().getActionBar().removeAllTabs();

        return rootView;
    }

    public void onItemSelected(AdapterView<?> parent, View view,
                               int pos, long id) {


    }

    public void onNothingSelected(AdapterView<?> parent) {

    }
}