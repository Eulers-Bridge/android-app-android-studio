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
 * Created by Anthony on 30/03/2015.
 */
public class ContactProfileViewPagerFragment extends SherlockFragment {
    private View rootView;
    private ViewPager mPager;
    private ProfilePagerAdapter mPagerAdapter;

    private String profileId;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        rootView = inflater.inflate(R.layout.contact_profile_viewpager_fragment, container, false);
        ((SherlockFragmentActivity) getActivity()).getSupportActionBar().setNavigationMode(ActionBar.NAVIGATION_MODE_TABS);
        ((SherlockFragmentActivity) getActivity()).getSupportActionBar().show();

        Bundle bundle = this.getArguments();
        profileId = (String) bundle.getString("ProfileId");

        FragmentManager fm = ((SherlockFragmentActivity) getActivity()).getSupportFragmentManager();

        ViewPager.SimpleOnPageChangeListener ViewPagerListener = new ViewPager.SimpleOnPageChangeListener() {
            @Override
            public void onPageSelected(int position) {
                super.onPageSelected(position);
            }
        };

        ArrayList<SherlockFragment> fragmentList = new ArrayList<SherlockFragment>();

        mPager = (ViewPager) rootView.findViewById(R.id.profileViewPagerFragment);
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