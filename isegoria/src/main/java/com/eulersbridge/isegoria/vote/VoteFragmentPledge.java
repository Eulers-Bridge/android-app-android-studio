package com.eulersbridge.isegoria.vote;

import android.os.Build;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.TabLayout;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.Spinner;
import android.widget.TimePicker;

import com.eulersbridge.isegoria.MainActivity;
import com.eulersbridge.isegoria.Network;
import com.eulersbridge.isegoria.election.SelfEfficacyQuestionsFragment;
import com.eulersbridge.isegoria.utilities.NonSwipeableViewPager;
import com.eulersbridge.isegoria.R;

import java.util.Calendar;
import java.util.GregorianCalendar;

public class VoteFragmentPledge extends Fragment {
    private ArrayAdapter<String> voteLocationArrayAdapter;
    private NonSwipeableViewPager mPager;
    private VoteFragment voteFragment;
    private Network network;

    public void setViewPager(NonSwipeableViewPager mPager) {
        this.mPager = mPager;
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.vote_fragment_pledge, container, false);

        //TODO: No Tabs

        MainActivity mainActivity = (MainActivity) getActivity();
        network = mainActivity.getIsegoriaApplication().getNetwork();

        Button voteNextButton = rootView.findViewById(R.id.voteNextButton);
        voteNextButton.setOnClickListener(view -> {
            mPager.setCurrentItem(2);

            Spinner voteSpinner = voteFragment.getSpinnerLocation();
            DatePicker datePicker = voteFragment.getDatePicker();
            TimePicker timePicker = voteFragment.getTimePicker();

            String location = voteSpinner.getSelectedItem().toString();
            Calendar calendar = new GregorianCalendar(datePicker.getYear(), datePicker.getMonth(), datePicker.getDayOfMonth());

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                calendar.set(Calendar.HOUR_OF_DAY, timePicker.getHour());
                calendar.set(Calendar.MINUTE, timePicker.getMinute());
            } else {
                calendar.set(Calendar.HOUR_OF_DAY, timePicker.getCurrentHour());
                calendar.set(Calendar.MINUTE, timePicker.getCurrentMinute());
            }

            long date = calendar.getTimeInMillis();

            network.addVoteReminder(location, date);
        });

        Button selfEfficacyStartButton = rootView.findViewById(R.id.selfEfficacyStartButton);
        selfEfficacyStartButton.setOnClickListener(view -> {
            SelfEfficacyQuestionsFragment selfEfficacyQuestionsFragment = new SelfEfficacyQuestionsFragment();

            getActivity().getSupportFragmentManager()
                    .beginTransaction()
                    .addToBackStack(null)
                    .add(R.id.container, selfEfficacyQuestionsFragment)
                    .commit();
        });

        return rootView;
    }

    public void setTabLayout(TabLayout tabLayout) {
        tabLayout.setVisibility(View.GONE);
    }

    public VoteFragment getVoteFragment() {
        return voteFragment;
    }

    public void setVoteFragment(VoteFragment voteFragment) {
        this.voteFragment = voteFragment;
    }
}