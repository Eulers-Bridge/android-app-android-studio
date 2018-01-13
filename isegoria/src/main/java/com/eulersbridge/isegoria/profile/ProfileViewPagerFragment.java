package com.eulersbridge.isegoria.profile;

import android.arch.lifecycle.ViewModelProviders;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.TabLayout;
import android.support.v4.app.Fragment;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AlertDialog;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;

import com.eulersbridge.isegoria.MainActivity;
import com.eulersbridge.isegoria.R;
import com.eulersbridge.isegoria.profile.badges.ProfileBadgesFragment;
import com.eulersbridge.isegoria.profile.settings.SettingsActivity;
import com.eulersbridge.isegoria.util.ui.SimpleFragmentPagerAdapter;
import com.eulersbridge.isegoria.util.ui.TitledFragment;

import java.util.ArrayList;

public class ProfileViewPagerFragment extends Fragment implements TitledFragment, MainActivity.TabbedFragment {

    private TabLayout tabLayout;
    private ViewPager viewPager;

    private ProfileViewModel viewModel;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.profile_viewpager_fragment, container, false);

        setHasOptionsMenu(true);

        viewModel = ViewModelProviders.of(this).get(ProfileViewModel.class);

        setupViewPager(rootView);

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
                if (getContext() != null)
                     new AlertDialog.Builder(getContext())
                             .setTitle(R.string.log_out_confirmation_title)
                             .setPositiveButton(android.R.string.yes,
                                     (dialogInterface, choice) -> viewModel.logOut())
                             .setNegativeButton(android.R.string.no, null)
                             .show();

                return true;

            default:
                return super.onOptionsItemSelected(item);
        }
    }

    private void setupViewPager(@NonNull View rootView) {
        if (viewPager == null) {
            viewPager = rootView.findViewById(R.id.profileViewPagerFragment);

            final ArrayList<Fragment> fragmentList = new ArrayList<>();

            ProfileOverviewFragment profileOverviewFragment = new ProfileOverviewFragment();

            fragmentList.add(profileOverviewFragment);
            fragmentList.add(new ProfileTaskProgressFragment());
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

    @Override
    public void setupTabLayout(TabLayout tabLayout) {
        this.tabLayout = tabLayout;

        tabLayout.removeAllTabs();
        tabLayout.setVisibility(View.VISIBLE);

        tabLayout.setupWithViewPager(viewPager);
        tabLayout.addOnTabSelectedListener(onTabSelectedListener);
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
}