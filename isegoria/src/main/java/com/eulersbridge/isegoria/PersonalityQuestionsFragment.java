package com.eulersbridge.isegoria;

import android.os.Bundle;
import android.support.v4.app.FragmentManager;
import android.support.v4.view.ViewPager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.actionbarsherlock.app.ActionBar;
import com.actionbarsherlock.app.SherlockFragment;
import com.actionbarsherlock.app.SherlockFragmentActivity;

import java.util.ArrayList;

/**
 * Created by Anthony on 01/04/2015.
 */
public class PersonalityQuestionsFragment extends SherlockFragment {
    private View rootView;
    private ViewPager mPager;
    private ProfilePagerAdapter mPagerAdapter;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        rootView = inflater.inflate(R.layout.personality_questions_fragment, container, false);
        ((SherlockFragmentActivity) getActivity()).getSupportActionBar().setNavigationMode(ActionBar.NAVIGATION_MODE_STANDARD);
        ((SherlockFragmentActivity) getActivity()).getSupportActionBar().show();

        FragmentManager fm = ((SherlockFragmentActivity) getActivity()).getSupportFragmentManager();

        ViewPager.SimpleOnPageChangeListener ViewPagerListener = new ViewPager.SimpleOnPageChangeListener() {
            @Override
            public void onPageSelected(int position) {
                super.onPageSelected(position);
            }
        };

        mPager = (ViewPager) rootView.findViewById(R.id.personalityViewPagerFragment);
        mPager.setOnPageChangeListener(ViewPagerListener);

        PersonalityScreen1Fragment personalityScreen1Fragment = new PersonalityScreen1Fragment();
        personalityScreen1Fragment.setViewPager(mPager);
        ArrayList<SherlockFragment> fragmentList = new ArrayList<SherlockFragment>();
        fragmentList.add(personalityScreen1Fragment);
        fragmentList.add(new PersonalityScreen2Fragment());

        mPagerAdapter = new ProfilePagerAdapter(fm, fragmentList);
        mPager.setAdapter(mPagerAdapter);

        return rootView;
    }
}