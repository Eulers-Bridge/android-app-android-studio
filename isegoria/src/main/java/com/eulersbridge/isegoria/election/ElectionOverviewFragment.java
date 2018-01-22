package com.eulersbridge.isegoria.election;


import android.animation.LayoutTransition;
import android.arch.lifecycle.ViewModelProviders;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.eulersbridge.isegoria.R;
import com.eulersbridge.isegoria.network.api.models.Election;
import com.eulersbridge.isegoria.util.Strings;

public class ElectionOverviewFragment extends Fragment {

    private TextView electionTitle;
    private TextView electionDate;

    private TextView electionIntroductionHeading;
    private TextView electionIntroduction;

    private TextView electionProcessHeading;
    private TextView electionProcess;

    @Override
	public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.election_overview_fragment, container, false);

        electionTitle = rootView.findViewById(R.id.election_overview_title);
        electionDate = rootView.findViewById(R.id.election_overview_date);
        electionIntroductionHeading = rootView.findViewById(R.id.election_overview_introduction_heading);
        electionIntroduction = rootView.findViewById(R.id.election_overview_introduction);
        electionProcessHeading = rootView.findViewById(R.id.election_overview_process_heading);
        electionProcess = rootView.findViewById(R.id.election_overview_process);

        /* By default, animateLayoutChanges="true" will not work when children change size,
          so enable animation when a child changes its size */
        ((ViewGroup)rootView.findViewById(R.id.election_overview_content_container))
                .getLayoutTransition()
                .enableTransitionType(LayoutTransition.CHANGING);

        ElectionViewModel viewModel = ViewModelProviders.of(this).get(ElectionViewModel.class);
        viewModel.getElection().observe(this, election -> {
            if (election != null)
                populateElectionText(election);
        });

		return rootView;
	}

    private void populateElectionText(@NonNull Election election) {
        electionTitle.setText(election.title);
        electionDate.setText(Strings.fromTimestamp(getContext(), election.startTimestamp));

        if (!TextUtils.isEmpty(election.introduction)) {
            electionIntroductionHeading.setVisibility(View.VISIBLE);
            electionIntroduction.setVisibility(View.VISIBLE);
            electionIntroduction.setText(election.introduction);

        } else {
            electionIntroductionHeading.setVisibility(View.GONE);
            electionIntroduction.setVisibility(View.GONE);
        }

        if (!TextUtils.isEmpty(election.process)) {
            electionProcessHeading.setVisibility(View.VISIBLE);
            electionProcess.setVisibility(View.VISIBLE);
            electionProcess.setText(election.process);

        } else {
            electionProcessHeading.setVisibility(View.GONE);
            electionProcess.setVisibility(View.GONE);
        }
    }
}
