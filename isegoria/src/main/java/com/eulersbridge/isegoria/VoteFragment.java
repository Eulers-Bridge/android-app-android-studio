package com.eulersbridge.isegoria;

import android.app.ActionBar;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.ArrayAdapter;
import android.widget.Spinner;

import com.actionbarsherlock.app.SherlockFragment;
import com.actionbarsherlock.app.SherlockFragmentActivity;

import java.util.ArrayList;

public class VoteFragment extends SherlockFragment implements OnItemSelectedListener {
	private View rootView;
	private ArrayAdapter<String> voteLocationArrayAdapter;
    private ArrayList<VoteLocation> voteLocationArray;
    private Network network;
	
	public VoteFragment() {
	
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {   
		rootView = inflater.inflate(R.layout.vote_fragment, container, false);
		((SherlockFragmentActivity) getActivity()).getSupportActionBar().setNavigationMode(ActionBar.NAVIGATION_MODE_STANDARD);
		getActivity().getActionBar().removeAllTabs();
		
        Spinner spinnerLocation = (Spinner) rootView.findViewById(R.id.voteLocation);
        spinnerLocation.setOnItemSelectedListener(this);
        voteLocationArrayAdapter = new ArrayAdapter<String>(getActivity(), R.layout.spinner_layout);
        voteLocationArrayAdapter.setDropDownViewResource(R.layout.spinner_layout);
        spinnerLocation.setAdapter(voteLocationArrayAdapter);
        voteLocationArray = new ArrayList<VoteLocation>();
        
        MainActivity mainActivity = (MainActivity) getActivity();
        network = mainActivity.getIsegoriaApplication().getNetwork();
        network.getVoteLocations(this);
		
		return rootView;
	}

	public void addVoteLocations(String ownerId, String votingLocationId,
                                 String name, String information) {
        VoteLocation voteLocation = new VoteLocation(ownerId, votingLocationId, name, information);
        voteLocationArray.add(voteLocation);
        voteLocationArrayAdapter.add(name);
	}
	
    public void onItemSelected(AdapterView<?> parent, View view, 
            int pos, long id) {
       // VoteLocation voteLocation = voteLocationArray.get(pos);
        //network.getVoteLocation(this, voteLocation.getVotingLocationId());
    }
    
    public void onNothingSelected(AdapterView<?> parent) {

    }
}