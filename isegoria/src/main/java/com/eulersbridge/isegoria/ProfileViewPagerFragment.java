package com.eulersbridge.isegoria;

import android.os.Bundle;
import android.support.design.widget.TabLayout;
import android.support.v4.app.Fragment;
import android.support.v4.view.ViewPager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import java.util.ArrayList;

public class ProfileViewPagerFragment extends Fragment {

    private TabLayout tabLayout;
    private ViewPager viewPager;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        final View rootView = inflater.inflate(R.layout.profile_viewpager_fragment, container, false);

        setupViewPager(rootView);
        setupTabLayout();

        return rootView;
    }

    private void setupViewPager(View rootView) {
        if (rootView == null) rootView = getView();

        if (viewPager == null && rootView != null) {
            viewPager = rootView.findViewById(R.id.profileViewPagerFragment);

            final ArrayList<Fragment> fragmentList = new ArrayList<>();

            final ProfileFragment profileFragment = new ProfileFragment();
            profileFragment.setViewPager(viewPager);
            fragmentList.add(profileFragment);
            fragmentList.add(new TaskDetailProgressFragment());
            fragmentList.add(new ProfileBadgesFragment());

            final ProfilePagerAdapter pagerAdapter = new ProfilePagerAdapter(getChildFragmentManager(), fragmentList) {
                @Override
                public CharSequence getPageTitle(int position) {
                    switch (position) {
                        case 0:
                            return "Profile";
                        case 1:
                            return "Progress";
                        case 2:
                            return "Badges";
                    }
                    return null;
                }
            };
            viewPager.setAdapter(pagerAdapter);

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