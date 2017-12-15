package com.eulersbridge.isegoria.vote;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
import android.support.v4.view.ViewPager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.TimePicker;

import com.eulersbridge.isegoria.Isegoria;
import com.eulersbridge.isegoria.R;
import com.eulersbridge.isegoria.models.Election;
import com.eulersbridge.isegoria.models.VoteLocation;
import com.eulersbridge.isegoria.network.SimpleCallback;

import java.util.ArrayList;
import java.util.List;

import retrofit2.Response;

public class VoteFragment extends Fragment implements OnItemSelectedListener {
    private ArrayAdapter<String> voteLocationArrayAdapter;
    @SuppressWarnings("MismatchedQueryAndUpdateOfCollection")
    private ArrayList<VoteLocation> voteLocationArray;

    private Spinner spinnerLocation;
    private TextView voteFragmentTitle1;
    private View voteDivider1;
    private View voteDivider2;
    private TextView voteText;
    private DatePicker datePicker;
    private TimePicker timePicker;

    private ViewPager mPager;

    @Override
	public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.vote_fragment, container, false);
		
        spinnerLocation = rootView.findViewById(R.id.voteLocation);
        spinnerLocation.setOnItemSelectedListener(this);
        voteLocationArrayAdapter = new ArrayAdapter<>(getActivity(), R.layout.spinner_layout);
        voteLocationArrayAdapter.setDropDownViewResource(R.layout.spinner_layout);
        spinnerLocation.setAdapter(voteLocationArrayAdapter);
        voteLocationArray = new ArrayList<>();

        datePicker = rootView.findViewById(R.id.datePicker1);
        timePicker = rootView.findViewById(R.id.timePicker1);
        voteDivider1 = rootView.findViewById(R.id.voteDivider1);
        voteDivider2 = rootView.findViewById(R.id.voteDivider2);
        voteFragmentTitle1 = rootView.findViewById(R.id.voteFragmentTitle1);
        voteText = rootView.findViewById(R.id.voteText);

        Button voteOkButton = rootView.findViewById(R.id.voteOkButton);
        voteOkButton.setOnClickListener(view -> mPager.setCurrentItem(1));

        Isegoria isegoria = (Isegoria) getActivity().getApplication();

        long institutionId = isegoria.getLoggedInUser().institutionId;

        isegoria.getAPI().getVoteLocations(institutionId).enqueue(new SimpleCallback<List<VoteLocation>>() {
            @Override
            protected void handleResponse(Response<List<VoteLocation>> response) {
                List<VoteLocation> locations = response.body();
                if (locations != null) {
                    for (VoteLocation voteLocation : locations) {
                        voteLocationArray.add(voteLocation);
                        voteLocationArrayAdapter.add(voteLocation.name);
                    }

                    showAll();
                }
            }
        });

        isegoria.getAPI().getElections(institutionId).enqueue(new SimpleCallback<List<Election>>() {
            @Override
            protected void handleResponse(Response<List<Election>> response) {
                List<Election> elections = response.body();
                if (elections != null && elections.size() > 0) {
                    Election election = elections.get(0);
                    updateDatePicker(election);
                }
            }
        });

        // TODO: Fix
        /*if (isegoria.getNetwork().isReminderSet()) {
            mPager.setCurrentItem(2);
        }*/

		return rootView;
	}

	private void updateDatePicker(Election election) {
        if (getActivity() != null) {
            getActivity().runOnUiThread(() -> {
                if (election.startVotingTimestamp < election.endVotingTimestamp) {
                    datePicker.setMinDate(election.startVotingTimestamp);
                    datePicker.setMaxDate(election.endVotingTimestamp);
                }
            });
        }
    }

    public void setViewPager(ViewPager mPager) {
        this.mPager = mPager;
    }
	
    public void onItemSelected(AdapterView<?> parent, View view, 
            int pos, long id) {
       // VoteLocation voteLocation = voteLocationArray.get(pos);
        //network.getVoteLocation(this, voteLocation.getVotingLocationId());
    }
    
    public void onNothingSelected(AdapterView<?> parent) {

    }

    public DatePicker getDatePicker() {
        return datePicker;
    }

    public TimePicker getTimePicker() {
        return timePicker;
    }

    public Spinner getSpinnerLocation() {
        return spinnerLocation;
    }

    private void showAll() {
        datePicker.setVisibility(ViewGroup.VISIBLE);
        timePicker.setVisibility(ViewGroup.VISIBLE);
        voteDivider1.setVisibility(ViewGroup.VISIBLE);
        voteDivider2.setVisibility(ViewGroup.VISIBLE);
        voteFragmentTitle1.setVisibility(ViewGroup.VISIBLE);
        voteText.setVisibility(ViewGroup.VISIBLE);
    }
}