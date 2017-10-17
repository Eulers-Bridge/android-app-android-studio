package com.eulersbridge.isegoria;

import android.app.ActionBar;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.Button;

import java.util.Calendar;

public class VoteFragmentDone extends Fragment implements OnItemSelectedListener {
    private View rootView;
    private NonSwipeableViewPager mPager;
    private Network network;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        rootView = inflater.inflate(R.layout.vote_fragment_done, container, false);
        ((MainActivity) getActivity()).getSupportActionBar().setNavigationMode(ActionBar.NAVIGATION_MODE_STANDARD);
        getActivity().getActionBar().removeAllTabs();

        final MainActivity mainActivity = (MainActivity) getActivity();
        network = mainActivity.getIsegoriaApplication().getNetwork();

        Button addToCalButton = rootView.findViewById(R.id.addToCalButton);
        addToCalButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Calendar cal = Calendar.getInstance();
                Intent intent = new Intent(Intent.ACTION_EDIT);
                intent.setType("vnd.android.cursor.item/event");
                intent.putExtra("beginTime", network.getVoteReminderDate());
                intent.putExtra("allDay", false);
                intent.putExtra("endTime", network.getVoteReminderDate()+60*60*1000);
                intent.putExtra("title", "Voting for Candidate");
                intent.putExtra("description", network.getVoteReminderLocation());
                mainActivity.startActivity(intent);
            }
        });

        return rootView;
    }

    public void setViewPager(NonSwipeableViewPager mPager) {
        this.mPager = mPager;
    }

    public void onItemSelected(AdapterView<?> parent, View view,
                               int pos, long id) {


    }

    public void onNothingSelected(AdapterView<?> parent) {

    }
}