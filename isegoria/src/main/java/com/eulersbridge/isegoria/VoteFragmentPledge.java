package com.eulersbridge.isegoria;

import android.app.ActionBar;
import android.graphics.Color;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewGroup.LayoutParams;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.TimePicker;

import java.util.Calendar;
import java.util.GregorianCalendar;

public class VoteFragmentPledge extends Fragment {
    private View rootView;
    private ArrayAdapter<String> voteLocationArrayAdapter;
    private NonSwipeableViewPager mPager;
    private VoteFragment voteFragment;
    private Network network;

    public void setViewPager(NonSwipeableViewPager mPager) {
        this.mPager = mPager;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        rootView = inflater.inflate(R.layout.vote_fragment_pledge, container, false);

        //TODO: No Tabs

        MainActivity mainActivity = (MainActivity) getActivity();
        network = mainActivity.getIsegoriaApplication().getNetwork();

        LinearLayout mContent = rootView.findViewById(R.id.signPad);
        Signature mSignature = new Signature(mainActivity, null);
        mSignature.setBackgroundColor(Color.WHITE);
        mContent.addView(mSignature, LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT);

        Button voteNextButton = rootView.findViewById(R.id.voteNextButton);
        voteNextButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mPager.setCurrentItem(2);

                Spinner voteSpinner = voteFragment.getSpinnerLocation();
                DatePicker datePicker = voteFragment.getDatePicker();
                TimePicker timePicker = voteFragment.getTimePicker();

                String location = voteSpinner.getSelectedItem().toString();
                Calendar calendar = new GregorianCalendar(datePicker.getYear(), datePicker.getMonth(), datePicker.getDayOfMonth());
                calendar.set(Calendar.HOUR_OF_DAY, timePicker.getHour());
                calendar.set(Calendar.MINUTE, timePicker.getMinute());
                long date = calendar.getTimeInMillis();

                network.addVoteReminder(date, location);
            }
        });

        return rootView;
    }

    public VoteFragment getVoteFragment() {
        return voteFragment;
    }

    public void setVoteFragment(VoteFragment voteFragment) {
        this.voteFragment = voteFragment;
    }
}