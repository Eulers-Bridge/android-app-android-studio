package com.eulersbridge.isegoria;

import com.actionbarsherlock.app.SherlockFragment;

import android.app.ActionBar;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

public class UserConsentAgreementFragment extends SherlockFragment {
	private View rootView;
	private Isegoria isegoria;
	
	public UserConsentAgreementFragment() {
		
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {   
		rootView = inflater.inflate(R.layout.user_consent_agreement_fragment, container, false);
		this.isegoria = (Isegoria) getActivity().getApplication();
		getActivity().getActionBar().setNavigationMode(ActionBar.NAVIGATION_MODE_STANDARD);
		
		return rootView;
	}
}
