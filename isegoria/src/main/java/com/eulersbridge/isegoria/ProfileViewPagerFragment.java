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

public class ProfileViewPagerFragment extends SherlockFragment  {
    private View rootView;
    private ViewPager mPager;
    private ProfilePagerAdapter mPagerAdapter;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        rootView = inflater.inflate(R.layout.profile_viewpager_fragment, container, false);
        ((SherlockFragmentActivity) getActivity()).getSupportActionBar().setNavigationMode(ActionBar.NAVIGATION_MODE_TABS);
        ((SherlockFragmentActivity) getActivity()).getSupportActionBar().show();

        FragmentManager fm = getChildFragmentManager();

        ViewPager.SimpleOnPageChangeListener ViewPagerListener = new ViewPager.SimpleOnPageChangeListener() {
            @Override
            public void onPageSelected(int position) {
                super.onPageSelected(position);
            }
        };

        ArrayList<SherlockFragment> fragmentList = new ArrayList<SherlockFragment>();

        mPager = (ViewPager) rootView.findViewById(R.id.profileViewPagerFragment);
        mPager.setOnPageChangeListener(ViewPagerListener);

        ProfileFragment profileFragment = new ProfileFragment();
        profileFragment.setViewPager(mPager);
        fragmentList.add(profileFragment);
        fragmentList.add(new TaskDetailProgressFragment());
        fragmentList.add(new ProfileBadgesFragment());

        mPagerAdapter = new ProfilePagerAdapter(fm, fragmentList);
        mPager.setAdapter(mPagerAdapter);

        return rootView;
    }
}