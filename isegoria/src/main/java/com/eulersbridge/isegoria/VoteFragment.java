package com.eulersbridge.isegoria;

import android.app.ActionBar;
import android.os.Bundle;
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

import com.actionbarsherlock.app.SherlockFragment;
import com.actionbarsherlock.app.SherlockFragmentActivity;

import java.util.ArrayList;

public class VoteFragment extends SherlockFragment implements OnItemSelectedListener {
	private View rootView;
	private ArrayAdapter<String> voteLocationArrayAdapter;
    private ArrayList<VoteLocation> voteLocationArray;

    public Spinner spinnerLocation;
    private TextView voteFragmentTitle1;
    private View voteDivider1;
    private View voteDivider2;
    private TextView voteText;
    public DatePicker datePicker;
    public TimePicker timePicker;

    private ViewPager mPager;
    private Network network;

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {   
		rootView = inflater.inflate(R.layout.vote_fragment, container, false);
		((SherlockFragmentActivity) getActivity()).getSupportActionBar().setNavigationMode(ActionBar.NAVIGATION_MODE_STANDARD);
		getActivity().getActionBar().removeAllTabs();
		
        spinnerLocation = (Spinner) rootView.findViewById(R.id.voteLocation);
        spinnerLocation.setOnItemSelectedListener(this);
        voteLocationArrayAdapter = new ArrayAdapter<String>(getActivity(), R.layout.spinner_layout);
        voteLocationArrayAdapter.setDropDownViewResource(R.layout.spinner_layout);
        spinnerLocation.setAdapter(voteLocationArrayAdapter);
        voteLocationArray = new ArrayList<VoteLocation>();

        datePicker = (DatePicker) rootView.findViewById(R.id.datePicker1);
        timePicker = (TimePicker) rootView.findViewById(R.id.timePicker1);
        voteDivider1 = (View) rootView.findViewById(R.id.voteDivider1);
        voteDivider2 = (View) rootView.findViewById(R.id.voteDivider1);
        voteFragmentTitle1 = (TextView) rootView.findViewById(R.id.voteFragmentTitle1);
        voteText = (TextView) rootView.findViewById(R.id.voteText);

        Button voteOkButton = (Button) rootView.findViewById(R.id.voteOkButton);
        voteOkButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mPager.setCurrentItem(1);
            }
        });
        
        MainActivity mainActivity = (MainActivity) getActivity();
        network = mainActivity.getIsegoriaApplication().getNetwork();
        network.getVoteLocations(this);
        network.getLatestElection(this);

        if(network.isReminderSet()) {
            mPager.setCurrentItem(2);
        }

		return rootView;
	}

    public void setViewPager(ViewPager mPager) {
        this.mPager = mPager;
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

    public DatePicker getDatePicker() {
        return datePicker;
    }

    public void setDatePicker(DatePicker datePicker) {
        this.datePicker = datePicker;
    }

    public TimePicker getTimePicker() {
        return timePicker;
    }

    public void setTimePicker(TimePicker timePicker) {
        this.timePicker = timePicker;
    }

    public Spinner getSpinnerLocation() {
        return spinnerLocation;
    }

    public void setSpinnerLocation(Spinner spinnerLocation) {
        this.spinnerLocation = spinnerLocation;
    }

    public void showAll() {
        datePicker.setVisibility(ViewGroup.VISIBLE);
        timePicker.setVisibility(ViewGroup.VISIBLE);
        voteDivider1.setVisibility(ViewGroup.VISIBLE);
        voteDivider2.setVisibility(ViewGroup.VISIBLE);
        voteFragmentTitle1.setVisibility(ViewGroup.VISIBLE);
        voteText.setVisibility(ViewGroup.VISIBLE);
    }
}