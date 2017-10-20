package com.eulersbridge.isegoria;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.view.ViewPager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

/**
 * Created by Anthony on 01/04/2015.
 */
public class PersonalityScreen1Fragment extends Fragment {

    private ViewPager mPager;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.personality_screen1_fragment, container, false);

        final MainActivity mainActivity = (MainActivity) getActivity();

        final Button takePersonalityButton = rootView.findViewById(R.id.takePersonalityButton);
        takePersonalityButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mPager.setCurrentItem(1);
            }
        });

        Button skipPersonalityQuestions = rootView.findViewById(R.id.skipPersonality);
        skipPersonalityQuestions.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                mainActivity.getIsegoriaApplication().setFeedFragment();
            }
        });


        return rootView;
    }

    public void setViewPager(ViewPager mPager) {
        this.mPager = mPager;
    }
}
