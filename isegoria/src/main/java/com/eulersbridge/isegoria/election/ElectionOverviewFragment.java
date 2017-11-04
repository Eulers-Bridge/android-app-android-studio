package com.eulersbridge.isegoria.election;


import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.eulersbridge.isegoria.MainActivity;
import com.eulersbridge.isegoria.Network;
import com.eulersbridge.isegoria.R;
import com.eulersbridge.isegoria.models.Election;

public class ElectionOverviewFragment extends Fragment {

    private TextView electionIntroduction;
    private TextView electionTitle;
    private TextView electionProcess;
    private TextView electionDate;

    @Override
	public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.election_overview_fragment, container, false);

        electionIntroduction = rootView.findViewById(R.id.electionIntroduction);
        electionTitle = rootView.findViewById(R.id.electionTitle);
        electionDate = rootView.findViewById(R.id.electionDate);
        electionProcess = rootView.findViewById(R.id.electionProcess);

        MainActivity mainActivity = (MainActivity) getActivity();
        Network network = mainActivity.getIsegoriaApplication().getNetwork();
        network.getLatestElection(new Network.ElectionListener() {
            @Override
            public void onFetchSuccess(Election election) {
                populateElectionText(election);
            }

            @Override
            public void onFetchFailure(Exception e) {}
        });
		
		return rootView;
	}

    private void populateElectionText(Election election) {
        if (getActivity() != null) {
            getActivity().runOnUiThread(() -> {
                electionTitle.setText(election.getTitle());
                electionIntroduction.setText(election.getIntroduction());
                //TODO: Format election date
                electionDate.setText("");
                electionProcess.setText(election.getProcess());
            });

        }
    }
}
