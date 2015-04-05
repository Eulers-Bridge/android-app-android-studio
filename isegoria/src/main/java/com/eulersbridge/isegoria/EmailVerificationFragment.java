package com.eulersbridge.isegoria;

import android.app.ActionBar;
import android.app.ProgressDialog;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

import com.actionbarsherlock.app.SherlockFragment;
import com.actionbarsherlock.app.SherlockFragmentActivity;

public class EmailVerificationFragment extends SherlockFragment {
    private View rootView;
    private MainActivity mainActivity;
    private EmailVerificationFragment emailVerificationFragment;
    private Network network;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        rootView = inflater.inflate(R.layout.email_verification, container, false);
        ((SherlockFragmentActivity) getActivity()).getSupportActionBar().setNavigationMode(ActionBar.NAVIGATION_MODE_STANDARD);
        getActivity().getActionBar().removeAllTabs();
        emailVerificationFragment = this;

        mainActivity = (MainActivity) getActivity();
        network = mainActivity.getIsegoriaApplication().getNetwork();

        final Button verifiedButton = (Button) rootView.findViewById(R.id.verifiedButton);
        verifiedButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                network.login();
                mainActivity.getIsegoriaApplication().login();
                mainActivity.dialog = ProgressDialog.show(mainActivity, "", "Loading. Please wait...", true);
            }
        });

        final Button resendVerificationButton = (Button) rootView.findViewById(R.id.resendVerificationButton);
        resendVerificationButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                network.verifyEmail(emailVerificationFragment);
            }
        });

        return rootView;
    }
}