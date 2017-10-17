package com.eulersbridge.isegoria;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.view.ViewPager;
import android.support.v7.app.ActionBar;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import java.util.ArrayList;

/**
 * Created by Anthony on 30/03/2015.
 */
public class ContactProfileViewPagerFragment extends Fragment {
    private View rootView;
    private ViewPager mPager;
    private ProfilePagerAdapter mPagerAdapter;

    private String profileId;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        rootView = inflater.inflate(R.layout.contact_profile_viewpager_fragment, container, false);
        ((MainActivity) getActivity()).getSupportActionBar().setNavigationMode(ActionBar.NAVIGATION_MODE_TABS);
        ((MainActivity) getActivity()).getSupportActionBar().show();

        Bundle bundle = this.getArguments();
        profileId = bundle.getString("ProfileId");

        FragmentManager fm = getActivity().getSupportFragmentManager();

        ViewPager.SimpleOnPageChangeListener ViewPagerListener = new ViewPager.SimpleOnPageChangeListener() {
            @Override
            public void onPageSelected(int position) {
                super.onPageSelected(position);
            }
        };

        ArrayList<Fragment> fragmentList = new ArrayList<>();

        mPager = rootView.findViewById(R.id.profileViewPagerFragment);
        mPager.setOnPageChangeListener(ViewPagerListener);

        ContactProfileFragment profileFragment = new ContactProfileFragment();
        Bundle args = new Bundle();
        args.putInt("ProfileId", Integer.parseInt(profileId));
        profileFragment.setArguments(args);
        profileFragment.setViewPager(mPager);
        fragmentList.add(profileFragment);
        fragmentList.add(new TaskDetailProgressFragment());

        mPagerAdapter = new ProfilePagerAdapter(fm, fragmentList);
        mPager.setAdapter(mPagerAdapter);

        return rootView;
    }
}