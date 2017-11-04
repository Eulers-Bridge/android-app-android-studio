package com.eulersbridge.isegoria.login;

import android.app.ProgressDialog;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

import com.eulersbridge.isegoria.MainActivity;
import com.eulersbridge.isegoria.Network;
import com.eulersbridge.isegoria.R;

public class EmailVerificationFragment extends Fragment {
    private MainActivity mainActivity;
    private Network network;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.email_verification, container, false);

        mainActivity = (MainActivity) getActivity();
        network = mainActivity.getIsegoriaApplication().getNetwork();

        final Button verifiedButton = rootView.findViewById(R.id.verifiedButton);
        verifiedButton.setOnClickListener(view -> {
            network.login();
            mainActivity.getIsegoriaApplication().login();
            mainActivity.dialog = ProgressDialog.show(mainActivity, "", "Loading. Please wait...", true);
        });

        final Button resendVerificationButton = rootView.findViewById(R.id.resendVerificationButton);
        resendVerificationButton.setOnClickListener(view -> network.verifyEmail());

        return rootView;
    }
}