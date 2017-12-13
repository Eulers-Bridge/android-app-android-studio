package com.eulersbridge.isegoria.profile;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.TabLayout;
import android.support.v4.app.Fragment;
import android.support.v4.view.ViewPager;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;

import com.eulersbridge.isegoria.Isegoria;
import com.eulersbridge.isegoria.MainActivity;
import com.eulersbridge.isegoria.R;
import com.eulersbridge.isegoria.utilities.TitledFragment;
import com.eulersbridge.isegoria.utilities.SimpleFragmentPagerAdapter;

import java.util.ArrayList;

public class ProfileViewPagerFragment extends Fragment implements TitledFragment {

    private TabLayout tabLayout;
    private ViewPager viewPager;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.profile_viewpager_fragment, container, false);

        setHasOptionsMenu(true);

        setupViewPager(rootView);
        setupTabLayout();

        return rootView;
    }

    @Override
    public String getTitle(Context context) {
        return context.getString(R.string.section_title_profile);
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.profile, menu);
        super.onCreateOptionsMenu(menu, inflater);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {

            case R.id.profile_settings:
                startActivity(new Intent(getContext(), SettingsActivity.class));
                return true;

            case R.id.profile_logout:
                if (getActivity() != null) {
                    ((Isegoria)getActivity().getApplication()).logOut();
                }
                return true;

            default:
                return super.onOptionsItemSelected(item);
        }
    }

    private void setupViewPager(View rootView) {
        if (rootView == null) rootView = getView();

        if (viewPager == null && rootView != null) {
            viewPager = rootView.findViewById(R.id.profileViewPagerFragment);

            final ArrayList<Fragment> fragmentList = new ArrayList<>();

            ProfileFragment profileFragment = new ProfileFragment();
            profileFragment.setViewPager(viewPager);

            fragmentList.add(profileFragment);
            fragmentList.add(new TaskDetailProgressFragment());
            fragmentList.add(new ProfileBadgesFragment());

            final SimpleFragmentPagerAdapter pagerAdapter = new SimpleFragmentPagerAdapter(getChildFragmentManager(), fragmentList) {
                @Override
                public CharSequence getPageTitle(int position) {
                    TitledFragment fragment = (TitledFragment)fragmentList.get(position);
                    if (fragment != null) {
                        return fragment.getTitle(getContext());

                    } else {
                        return null;
                    }
                }
            };

            viewPager.setAdapter(pagerAdapter);
            viewPager.setOffscreenPageLimit(3);
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