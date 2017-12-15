package com.eulersbridge.isegoria.auth;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

import com.eulersbridge.isegoria.R;

public class PersonalityScreen1Fragment extends Fragment {

    private ViewPager viewPager;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.personality_screen1_fragment, container, false);

        final AppCompatActivity activity = (AppCompatActivity) getActivity();

        final Button takePersonalityButton = rootView.findViewById(R.id.takePersonalityButton);
        takePersonalityButton.setOnClickListener(view -> viewPager.setCurrentItem(1));

        Button skipPersonalityQuestions = rootView.findViewById(R.id.skipPersonality);
        skipPersonalityQuestions.setOnClickListener(v -> {
            if (activity != null) activity.finish();
        });

        return rootView;
    }

    public void setViewPager(ViewPager viewPager) {
        this.viewPager = viewPager;
    }
}
