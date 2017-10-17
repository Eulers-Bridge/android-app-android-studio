package com.eulersbridge.isegoria;

import android.app.ActionBar;
import android.app.ProgressDialog;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

public class EmailVerificationFragment extends Fragment {
    private View rootView;
    private MainActivity mainActivity;
    private EmailVerificationFragment emailVerificationFragment;
    private Network network;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        rootView = inflater.inflate(R.layout.email_verification, container, false);

        //TODO: No Tabs

        emailVerificationFragment = this;

        mainActivity = (MainActivity) getActivity();
        network = mainActivity.getIsegoriaApplication().getNetwork();

        final Button verifiedButton = rootView.findViewById(R.id.verifiedButton);
        verifiedButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                network.login();
                mainActivity.getIsegoriaApplication().login();
                mainActivity.dialog = ProgressDialog.show(mainActivity, "", "Loading. Please wait...", true);
            }
        });

        final Button resendVerificationButton = rootView.findViewById(R.id.resendVerificationButton);
        resendVerificationButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                network.verifyEmail(emailVerificationFragment);
            }
        });

        return rootView;
    }
}