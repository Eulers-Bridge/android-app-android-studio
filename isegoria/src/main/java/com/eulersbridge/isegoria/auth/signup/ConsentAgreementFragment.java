package com.eulersbridge.isegoria.auth.signup;

import android.content.Context;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.eulersbridge.isegoria.MainActivity;
import com.eulersbridge.isegoria.util.ui.TitledFragment;
import com.eulersbridge.isegoria.R;

public class ConsentAgreementFragment extends Fragment implements TitledFragment {

	@Override
	public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		View rootView = inflater.inflate(R.layout.user_consent_agreement_fragment, container, false);

        rootView.findViewById(R.id.user_consent_next_button).setOnClickListener(view -> {
            MainActivity mainActivity = (MainActivity)getActivity();

            if (mainActivity != null) mainActivity.userConsentNext();
        });

		return rootView;
	}

	@Override
	public String getTitle(Context context) {
		return null;
	}
}
