package com.eulersbridge.isegoria.login;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
import android.support.v4.view.ViewPager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

import com.eulersbridge.isegoria.MainActivity;
import com.eulersbridge.isegoria.R;

public class PersonalityScreen1Fragment extends Fragment {

    private ViewPager mPager;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.personality_screen1_fragment, container, false);

        final MainActivity mainActivity = (MainActivity) getActivity();

        final Button takePersonalityButton = rootView.findViewById(R.id.takePersonalityButton);
        takePersonalityButton.setOnClickListener(view -> mPager.setCurrentItem(1));

        Button skipPersonalityQuestions = rootView.findViewById(R.id.skipPersonality);
        skipPersonalityQuestions.setOnClickListener(v -> mainActivity.getIsegoriaApplication().setFeedFragment());


        return rootView;
    }

    public void setViewPager(ViewPager mPager) {
        this.mPager = mPager;
    }
}
