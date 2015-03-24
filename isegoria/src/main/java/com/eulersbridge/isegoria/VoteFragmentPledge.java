package com.eulersbridge.isegoria;

import android.app.ActionBar;
import android.graphics.Color;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewGroup.LayoutParams;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.ArrayAdapter;
import android.widget.LinearLayout;

import com.actionbarsherlock.app.SherlockFragment;
import com.actionbarsherlock.app.SherlockFragmentActivity;

public class VoteFragmentPledge extends SherlockFragment implements OnItemSelectedListener {
    private View rootView;
    private ArrayAdapter<String> voteLocationArrayAdapter;

    public VoteFragmentPledge() {

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        rootView = inflater.inflate(R.layout.vote_fragment_pledge, container, false);
        ((SherlockFragmentActivity) getActivity()).getSupportActionBar().setNavigationMode(ActionBar.NAVIGATION_MODE_STANDARD);
        getActivity().getActionBar().removeAllTabs();

        MainActivity mainActivity = (MainActivity) getActivity();
        Network network = mainActivity.getIsegoriaApplication().getNetwork();

        LinearLayout mContent = (LinearLayout) rootView.findViewById(R.id.signPad);
        Signature mSignature = new Signature(mainActivity, null);
        mSignature.setBackgroundColor(Color.WHITE);
        mContent.addView(mSignature, LayoutParams.FILL_PARENT, LayoutParams.FILL_PARENT);

        return rootView;
    }

    public void addVoteLocations(String location) {
        voteLocationArrayAdapter.add(location);
    }

    public void onItemSelected(AdapterView<?> parent, View view,
                               int pos, long id) {


    }

    public void onNothingSelected(AdapterView<?> parent) {

    }
}