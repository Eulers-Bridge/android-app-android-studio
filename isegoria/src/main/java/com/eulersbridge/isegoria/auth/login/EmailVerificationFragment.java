package com.eulersbridge.isegoria.auth.login;

import android.arch.lifecycle.ViewModelProviders;
import android.content.Context;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

import com.eulersbridge.isegoria.R;
import com.eulersbridge.isegoria.util.ui.TitledFragment;

public class EmailVerificationFragment extends Fragment implements TitledFragment {

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.email_verification, container, false);

        EmailVerificationViewModel viewModel = ViewModelProviders.of(this).get(EmailVerificationViewModel.class);

        final Button verifiedButton = rootView.findViewById(R.id.verifiedButton);
        verifiedButton.setOnClickListener(view -> viewModel.userVerified());

        final Button resendVerificationButton = rootView.findViewById(R.id.resendVerificationButton);
        resendVerificationButton.setOnClickListener(view -> {

            view.setEnabled(false);

            viewModel.resendVerification().observe(this, success -> view.setEnabled(true));
        });

        return rootView;
    }

    @Override
    public String getTitle(Context context) {
        return null;
    }
}