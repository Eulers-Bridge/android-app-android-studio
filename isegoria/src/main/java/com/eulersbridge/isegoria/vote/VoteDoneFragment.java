package com.eulersbridge.isegoria.vote;

import android.arch.lifecycle.ViewModelProviders;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

import com.eulersbridge.isegoria.R;
import com.eulersbridge.isegoria.util.ui.TitledFragment;

public class VoteDoneFragment extends Fragment implements TitledFragment {

    private Button addToCalendarButton;
    private VoteViewModel viewModel;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.vote_fragment_done, container, false);

        addToCalendarButton = rootView.findViewById(R.id.vote_done_button_add_to_calendar);
        addToCalendarButton.setEnabled(false);
        addToCalendarButton.setOnClickListener(view -> addToCalendar());

        if (getParentFragment() != null) {
            viewModel = ViewModelProviders.of(getParentFragment()).get(VoteViewModel.class);

            viewModel.getLatestVoteReminder().observe(this, success -> {
                if (success != null && success)
                    addToCalendarButton.setEnabled(true);
            });
        }

        return rootView;
    }


    private void addToCalendar() {
        if (viewModel != null)
            return;

        addToCalendarButton.setEnabled(false);

        Intent addToCalendarIntent = viewModel.getAddVoteReminderToCalendarIntent();

        if (addToCalendarIntent != null
                && getActivity() != null
                && addToCalendarIntent.resolveActivity(getActivity().getPackageManager()) != null)
            getActivity().startActivity(addToCalendarIntent);

        addToCalendarButton.setEnabled(true);
    }

    @Override
    public String getTitle(Context context) {
        return context.getString(R.string.vote_tab_3);
    }
}