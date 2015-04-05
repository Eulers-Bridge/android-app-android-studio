package com.eulersbridge.isegoria;

import android.app.ActionBar;
import android.graphics.Color;
import android.os.Bundle;
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

import com.actionbarsherlock.app.SherlockFragment;
import com.actionbarsherlock.app.SherlockFragmentActivity;

import java.util.Calendar;
import java.util.GregorianCalendar;

public class VoteFragmentPledge extends SherlockFragment  {
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
        ((SherlockFragmentActivity) getActivity()).getSupportActionBar().setNavigationMode(ActionBar.NAVIGATION_MODE_STANDARD);
        getActivity().getActionBar().removeAllTabs();

        MainActivity mainActivity = (MainActivity) getActivity();
        network = mainActivity.getIsegoriaApplication().getNetwork();

        LinearLayout mContent = (LinearLayout) rootView.findViewById(R.id.signPad);
        Signature mSignature = new Signature(mainActivity, null);
        mSignature.setBackgroundColor(Color.WHITE);
        mContent.addView(mSignature, LayoutParams.FILL_PARENT, LayoutParams.FILL_PARENT);

        Button voteNextButton = (Button) rootView.findViewById(R.id.voteNextButton);
        voteNextButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mPager.setCurrentItem(2);

                Spinner voteSpinner = voteFragment.getSpinnerLocation();
                DatePicker datePicker = voteFragment.getDatePicker();
                TimePicker timePicker = voteFragment.getTimePicker();

                String location = voteSpinner.getSelectedItem().toString();
                Calendar calendar = new GregorianCalendar(datePicker.getYear(), datePicker.getMonth(), datePicker.getDayOfMonth());
                calendar.set(Calendar.HOUR_OF_DAY, timePicker.getCurrentHour());
                calendar.set(Calendar.MINUTE, timePicker.getCurrentMinute());
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