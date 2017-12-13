package com.eulersbridge.isegoria.login;

import android.app.ProgressDialog;
import android.content.Context;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

import com.eulersbridge.isegoria.Isegoria;
import com.eulersbridge.isegoria.MainActivity;
import com.eulersbridge.isegoria.utilities.TitledFragment;
import com.eulersbridge.isegoria.network.IgnoredCallback;
import com.eulersbridge.isegoria.R;

public class EmailVerificationFragment extends Fragment implements TitledFragment {
    private MainActivity mainActivity;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.email_verification, container, false);

        mainActivity = (MainActivity) getActivity();

        Isegoria isegoria = mainActivity.getIsegoriaApplication();

        final Button verifiedButton = rootView.findViewById(R.id.verifiedButton);
        verifiedButton.setOnClickListener(view -> {
            isegoria.login(null, null);
            mainActivity.dialog = ProgressDialog.show(mainActivity, "", "Loading. Please wait...", true);
        });

        final Button resendVerificationButton = rootView.findViewById(R.id.resendVerificationButton);
        resendVerificationButton.setOnClickListener(view -> {
            String userEmail = isegoria.getLoggedInUser().email;

            isegoria.getAPI().sendVerificationEmail(userEmail).enqueue(new IgnoredCallback<>());
        });

        return rootView;
    }

    @Override
    public String getTitle(Context context) {
        return null;
    }
}