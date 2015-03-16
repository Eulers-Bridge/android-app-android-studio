package com.eulersbridge.isegoria;

import com.actionbarsherlock.app.SherlockFragment;
import com.actionbarsherlock.app.SherlockFragmentActivity;

import android.app.ActionBar;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Spinner;
import android.widget.AdapterView.OnItemSelectedListener;

public class VoteFragment extends SherlockFragment implements OnItemSelectedListener {
	private View rootView;
	private ArrayAdapter<String> voteLocationArrayAdapter;
	
	public VoteFragment() {
	
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {   
		rootView = inflater.inflate(R.layout.vote_fragment, container, false);
		((SherlockFragmentActivity) getActivity()).getSupportActionBar().setNavigationMode(ActionBar.NAVIGATION_MODE_STANDARD);
		getActivity().getActionBar().removeAllTabs();
		
        Spinner spinnerLocation = (Spinner) rootView.findViewById(R.id.voteLocation);
        spinnerLocation.setOnItemSelectedListener(this);
        voteLocationArrayAdapter = new ArrayAdapter<String>(getActivity(), android.R.layout.simple_spinner_item);
        voteLocationArrayAdapter.add("Test");
        voteLocationArrayAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerLocation.setAdapter(voteLocationArrayAdapter);
        
        MainActivity mainActivity = (MainActivity) getActivity();
        Network network = mainActivity.getIsegoriaApplication().getNetwork();
        network.getVoteRecords(this);
		
		return rootView;
	}
	
	public void addVoteLocations(String location) {
		voteLocationArrayAdapter.add(location);
	}
	
    public void onItemSelected(AdapterView<?> parent, View view, 
            int pos, long id) {


    }
    
    public void onNothingSelected(AdapterView<?> parent) {

    }
}