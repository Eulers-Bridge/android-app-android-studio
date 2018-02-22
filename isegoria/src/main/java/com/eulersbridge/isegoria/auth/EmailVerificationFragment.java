package com.eulersbridge.isegoria.auth;

import android.arch.lifecycle.ViewModelProviders;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

import com.eulersbridge.isegoria.R;

public class EmailVerificationFragment extends Fragment {

    private EmailVerificationViewModel viewModel;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.email_verification, container, false);

        viewModel = ViewModelProviders.of(this).get(EmailVerificationViewModel.class);

        //noinspection ConstantConditions
        AuthViewModel authViewModel = ViewModelProviders.of(getActivity()).get(AuthViewModel.class);

        final Button verifiedButton = rootView.findViewById(R.id.verifiedButton);
        verifiedButton.setOnClickListener(view -> {
            verifiedButton.setEnabled(false);

            authViewModel.verificationComplete.setValue(true);

            viewModel.userVerified().observe(this, success -> {
                if (success != null && !success)
                    verifiedButton.setEnabled(true);
            });
        });

        final Button resendVerificationButton = rootView.findViewById(R.id.resendVerificationButton);
        resendVerificationButton.setOnClickListener(view -> {
            view.setEnabled(false);

            viewModel.resendVerification().observe(this,
                    success -> view.setEnabled(true));
        });

        return rootView;
    }

    @Override
    public void onDestroy() {
        if (viewModel != null)
            viewModel.onExit();

        super.onDestroy();
    }
}