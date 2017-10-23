package com.eulersbridge.isegoria;


import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

public class ElectionOverviewFragment extends Fragment {

    private TextView electionIntroduction;
    private TextView electionTitle;
    private TextView electionProcess;
    private TextView electionDate;
    private TextView overviewTextField;
    private TextView processTextField;
    private int electionId;

    private Network network;

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.election_overview_fragment, container, false);

        electionIntroduction = rootView.findViewById(R.id.electionIntroduction);
        electionTitle = rootView.findViewById(R.id.electionTitle);
        electionDate = rootView.findViewById(R.id.electionDate);
        electionProcess = rootView.findViewById(R.id.electionProcess);
        overviewTextField = rootView.findViewById(R.id.overviewTextField);
        processTextField = rootView.findViewById(R.id.processTextField);

        MainActivity mainActivity = (MainActivity) getActivity();
        network = mainActivity.getIsegoriaApplication().getNetwork();
        network.getLatestElection(this);
		
		return rootView;
	}

    public void updateEntities(int electionId, String title, String introduction, String date,
                               String process) {
        this.electionId = electionId;

        overviewTextField.setText("Overview");
        processTextField.setText("Process");
        electionTitle.setText(title);
        electionIntroduction.setText(introduction);
        electionDate.setText(date);
        electionProcess.setText(process);
    }
}
