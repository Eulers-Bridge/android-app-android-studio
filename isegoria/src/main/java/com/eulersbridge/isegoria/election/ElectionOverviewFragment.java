package com.eulersbridge.isegoria.election;


import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.eulersbridge.isegoria.Isegoria;
import com.eulersbridge.isegoria.R;
import com.eulersbridge.isegoria.models.Election;
import com.eulersbridge.isegoria.network.SimpleCallback;
import com.eulersbridge.isegoria.common.Utils;

import java.util.List;

import retrofit2.Response;

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

        Isegoria isegoria = (Isegoria)getActivity().getApplication();

        long institutionId = isegoria.getLoggedInUser().institutionId;

        isegoria.getAPI().getElections(institutionId).enqueue(new SimpleCallback<List<Election>>() {
            @Override
            public void handleResponse(Response<List<Election>> response) {
                List<Election> elections = response.body();
                if (elections != null && elections.size() > 0) {
                    populateElectionText(elections.get(0));
                }
            }
        });
		
		return rootView;
	}

    private void populateElectionText(Election election) {
        if (getActivity() != null) {
            getActivity().runOnUiThread(() -> {
                electionTitle.setText(election.title);
                electionIntroduction.setText(election.introduction);
                //TODO: Format election date
                electionDate.setText(Utils.convertTimestampToString(getContext(), election.startTimestamp));
                electionProcess.setText(election.process);
            });

        }
    }
}
