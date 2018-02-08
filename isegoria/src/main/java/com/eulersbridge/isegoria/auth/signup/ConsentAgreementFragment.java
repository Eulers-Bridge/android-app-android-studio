package com.eulersbridge.isegoria.auth.signup;

import android.arch.lifecycle.ViewModelProviders;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ScrollView;

import com.eulersbridge.isegoria.R;
import com.eulersbridge.isegoria.auth.AuthViewModel;

public class ConsentAgreementFragment extends Fragment {

	@Override
	public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		View rootView = inflater.inflate(R.layout.consent_agreement_fragment, container, false);

		final ScrollView scrollContainer = rootView.findViewById(R.id.consent_agreement_container);

		boolean hasTranslucentStatusBar = Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP;
		if (hasTranslucentStatusBar) {
			int additionalTopPadding = Math.round(22 * getResources().getDisplayMetrics().density);

			scrollContainer.setPadding(
					scrollContainer.getPaddingLeft(),
					scrollContainer.getPaddingTop() + additionalTopPadding,
					scrollContainer.getPaddingRight(),
					scrollContainer.getPaddingBottom());
		}

		//noinspection ConstantConditions
		AuthViewModel authViewModel = ViewModelProviders.of(getActivity()).get(AuthViewModel.class);

		final Button agreeButton = rootView.findViewById(R.id.user_consent_next_button);
		agreeButton.setOnClickListener(view -> {
			agreeButton.setEnabled(false);

            authViewModel.setSignUpConsentGiven();
		});

		return rootView;
	}
}
