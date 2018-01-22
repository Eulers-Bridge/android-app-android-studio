package com.eulersbridge.isegoria.personality;

import android.arch.lifecycle.ViewModelProviders;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

import com.eulersbridge.isegoria.R;

public class PersonalityPermissionFragment extends Fragment {

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.personality_screen1_fragment, container, false);

        //noinspection ConstantConditions
        PersonalityViewModel viewModel = ViewModelProviders.of(getActivity()).get(PersonalityViewModel.class);

        final Button takePersonalityButton = rootView.findViewById(R.id.takePersonalityButton);
        takePersonalityButton.setOnClickListener(view -> viewModel.setUserContinuedQuestions());

        Button skipPersonalityQuestions = rootView.findViewById(R.id.skipPersonality);
        skipPersonalityQuestions.setOnClickListener(v -> viewModel.setUserSkippedQuestions());

        return rootView;
    }
}
