package com.eulersbridge.isegoria.vote;

import android.os.Bundle;
import android.support.design.widget.TabLayout;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.eulersbridge.isegoria.MainActivity;
import com.eulersbridge.isegoria.utilities.NonSwipeableViewPager;
import com.eulersbridge.isegoria.R;
import com.eulersbridge.isegoria.utilities.SimpleFragmentPagerAdapter;

import java.util.ArrayList;

public class VoteViewPagerFragment extends Fragment {
    private TabLayout tabLayout;
    private NonSwipeableViewPager viewPager;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.vote_view_pager_fragment, container, false);

        ((MainActivity)getActivity()).setToolbarTitle(getString(R.string.section_title_vote));

        setupViewPager(rootView);
        setupTabLayout();

        return rootView;
    }

    private void setupViewPager(View rootView) {
        if (rootView == null) rootView = getView();

        if (viewPager == null && rootView != null) {
            viewPager = rootView.findViewById(R.id.voteViewPagerFragment);

            ArrayList<Fragment> fragments = new ArrayList<>();

            VoteFragment voteFragment = new VoteFragment();
            VoteFragmentPledge voteFragmentPledge = new VoteFragmentPledge();
            VoteFragmentDone voteFragmentDone = new VoteFragmentDone();

            voteFragment.setViewPager(viewPager);
            voteFragmentPledge.setViewPager(viewPager);

            fragments.add(voteFragment);
            fragments.add(voteFragmentPledge);
            fragments.add(voteFragmentDone);

            voteFragmentPledge.setVoteFragment(voteFragment);

            SimpleFragmentPagerAdapter viewPagerAdapter = new SimpleFragmentPagerAdapter(getChildFragmentManager(), fragments) {
                @Override
                public CharSequence getPageTitle(int position) {
                    switch (position) {
                        case 0:
                            return "Vote";
                        case 1:
                            return "Pledge";
                        case 2:
                            return "Done";
                    }
                    return null;
                }
            };
            viewPager.setAdapter(viewPagerAdapter);

            viewPager.setCurrentItem(0);
        }
    }

    public void setTabLayout(TabLayout tabLayout) {
        this.tabLayout = tabLayout;
    }

    @Override
    public void onPause() {
        super.onPause();

        if (tabLayout != null) tabLayout.removeOnTabSelectedListener(onTabSelectedListener);
    }

    private final TabLayout.OnTabSelectedListener onTabSelectedListener = new TabLayout.OnTabSelectedListener() {
        @Override
        public void onTabSelected(TabLayout.Tab tab) {
            viewPager.setCurrentItem(tab.getPosition());
        }

        @Override
        public void onTabUnselected(TabLayout.Tab tab) { }

        @Override
        public void onTabReselected(TabLayout.Tab tab) { }
    };

    private void setupTabLayout() {
        if (tabLayout == null) return;

        tabLayout.removeAllTabs();
        tabLayout.setupWithViewPager(viewPager);
        tabLayout.addOnTabSelectedListener(onTabSelectedListener);
        tabLayout.setVisibility(View.VISIBLE);
    }
}