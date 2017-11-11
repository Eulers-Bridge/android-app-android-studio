package com.eulersbridge.isegoria.login;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.eulersbridge.isegoria.R;
import com.eulersbridge.isegoria.utilities.TitledFragment;

public class UserConsentAgreementFragment extends Fragment implements TitledFragment {

	@Override
	public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		return inflater.inflate(R.layout.user_consent_agreement_fragment, container, false);
	}

	@Override
	public String getTitle() {
		return null;
	}
}
