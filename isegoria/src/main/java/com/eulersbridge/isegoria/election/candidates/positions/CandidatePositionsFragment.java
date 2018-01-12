package com.eulersbridge.isegoria.election.candidates.positions;


import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.eulersbridge.isegoria.IsegoriaApp;
import com.eulersbridge.isegoria.network.api.API;
import com.eulersbridge.isegoria.network.api.models.Election;
import com.eulersbridge.isegoria.network.api.models.Position;
import com.eulersbridge.isegoria.network.api.models.User;
import com.eulersbridge.isegoria.util.network.SimpleCallback;
import com.eulersbridge.isegoria.R;

import java.util.List;

import retrofit2.Response;

public class CandidatePositionsFragment extends Fragment {
    private API api;

    private PositionAdapter adapter;

	@Override
	public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		View rootView = inflater.inflate(R.layout.election_positions_fragment, container, false);

        IsegoriaApp isegoriaApp = (getActivity() != null)? (IsegoriaApp)getActivity().getApplication() : null;

        if (isegoriaApp != null)
            api = isegoriaApp.getAPI();

        adapter = new PositionAdapter(this, api);

        RecyclerView positionsGridView = rootView.findViewById(R.id.election_positions_grid_view);
        positionsGridView.setAdapter(adapter);

        if (isegoriaApp != null) {
            User user = isegoriaApp.getLoggedInUser();

            if (user != null) {
                Long institutionId = user.institutionId;

                if (institutionId != null)
                    isegoriaApp.getAPI().getElections(institutionId).enqueue(electionsCallback);
            }
        }
        
		return rootView;
	}

	private final SimpleCallback<List<Election>> electionsCallback = new SimpleCallback<List<Election>>() {
		@Override
        protected void handleResponse(Response<List<Election>> response) {
			List<Election> elections = response.body();
			if (elections != null && elections.size() > 0) {
				Election election = elections.get(0);

				api.getElectionPositions(election.id).enqueue(positionsCallback);
			}
		}
	};

	private final SimpleCallback<List<Position>> positionsCallback = new SimpleCallback<List<Position>>() {
        @Override
        protected void handleResponse(Response<List<Position>> response) {
            List<Position> positions = response.body();

            if (positions != null)
                setPositions(positions);
        }
	};

	private void setPositions(@NonNull List<Position> positions) {
	    adapter.setLoading(false);
        adapter.replaceItems(positions);
    }
}
