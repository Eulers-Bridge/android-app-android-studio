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

public class VoteViewPagerFragment extends SherlockFragment  {
    private View rootView;
    private NonSwipeableViewPager mPager;
    private ProfilePagerAdapter mPagerAdapter;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        rootView = inflater.inflate(R.layout.vote_view_pager_fragment, container, false);
        ((SherlockFragmentActivity) getActivity()).getSupportActionBar().setNavigationMode(ActionBar.NAVIGATION_MODE_TABS);

        FragmentManager fm = getChildFragmentManager();

        ViewPager.SimpleOnPageChangeListener ViewPagerListener = new ViewPager.SimpleOnPageChangeListener() {
            @Override
            public void onPageSelected(int position) {
                super.onPageSelected(position);
            }
        };

        ArrayList<SherlockFragment> fragmentList = new ArrayList<SherlockFragment>();

        mPager = (NonSwipeableViewPager) rootView.findViewById(R.id.voteViewPagerFragment);
        mPager.setOnPageChangeListener(ViewPagerListener);

        VoteFragment voteFragment = new VoteFragment();
        VoteFragmentPledge voteFragmentPledge = new VoteFragmentPledge();
        VoteFragmentDone voteFragmentDone = new VoteFragmentDone();

        voteFragment.setViewPager(mPager);
        voteFragmentPledge.setViewPager(mPager);
        voteFragmentDone.setViewPager(mPager);

        ProfileFragment profileFragment = new ProfileFragment();
        profileFragment.setViewPager(mPager);
        fragmentList.add(voteFragment);
        fragmentList.add(voteFragmentPledge);
        fragmentList.add(voteFragmentDone);

        voteFragmentPledge.setVoteFragment(voteFragment);

        mPagerAdapter = new ProfilePagerAdapter(fm, fragmentList);
        mPager.setAdapter(mPagerAdapter);

        return rootView;
    }
}