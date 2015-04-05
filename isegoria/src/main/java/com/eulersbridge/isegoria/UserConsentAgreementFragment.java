package com.eulersbridge.isegoria;

import android.app.ActionBar;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

import com.actionbarsherlock.app.SherlockFragment;

public class UserConsentAgreementFragment extends SherlockFragment {
	private View rootView;
	private Isegoria isegoria;

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {   
		rootView = inflater.inflate(R.layout.user_consent_agreement_fragment, container, false);
		this.isegoria = (Isegoria) getActivity().getApplication();
		getActivity().getActionBar().setNavigationMode(ActionBar.NAVIGATION_MODE_STANDARD);


		return rootView;
	}
}
