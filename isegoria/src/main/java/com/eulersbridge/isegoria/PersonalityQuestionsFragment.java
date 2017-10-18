package com.eulersbridge.isegoria;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import java.util.ArrayList;

/**
 * Created by Anthony on 01/04/2015.
 */
public class PersonalityQuestionsFragment extends Fragment {

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.personality_questions_fragment, container, false);

        //TODO: No Tabs

        FragmentManager fm = getActivity().getSupportFragmentManager();

        NonSwipeableViewPager mPager = rootView.findViewById(R.id.personalityViewPagerFragment);

        PersonalityScreen1Fragment personalityScreen1Fragment = new PersonalityScreen1Fragment();
        personalityScreen1Fragment.setViewPager(mPager);
        ArrayList<Fragment> fragmentList = new ArrayList<>();
        fragmentList.add(personalityScreen1Fragment);
        fragmentList.add(new PersonalityScreen2Fragment());

        ProfilePagerAdapter mPagerAdapter = new ProfilePagerAdapter(fm, fragmentList);
        mPager.setAdapter(mPagerAdapter);

        return rootView;
    }
}