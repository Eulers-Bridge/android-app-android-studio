package com.eulersbridge.isegoria.feed;

import android.os.Build;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.TabLayout;
import android.support.v4.app.Fragment;
import android.support.v4.content.ContextCompat;
import android.support.v4.view.ViewPager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.eulersbridge.isegoria.MainActivity;
import com.eulersbridge.isegoria.R;
import com.eulersbridge.isegoria.utilities.Utils;


public class FeedFragment extends Fragment {
    private TabLayout tabLayout;
    private ViewPager viewPager;

	@Override
	public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.feed_fragment, container, false);

        MainActivity mainActivity = (MainActivity)getActivity();

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            int colour = ContextCompat.getColor(mainActivity, R.color.darkBlue);
            mainActivity.getWindow().setStatusBarColor(colour);

            Utils.setMultitaskColour(mainActivity, colour);
            Utils.setStatusBarColour(mainActivity, colour);
        }

        // Ensure options menu from another fragment is not carried over
        mainActivity.invalidateOptionsMenu();

        mainActivity.setToolbarTitle(getString(R.string.section_title_feed));

        setupViewPager(rootView);
        setupTabLayout();

        Utils.hideKeyboard(mainActivity);

		return rootView;
	}

	private void setupViewPager(View rootView) {
        if (rootView == null) rootView = getView();

        if (viewPager == null && rootView != null) {
            viewPager = rootView.findViewById(R.id.feedViewPagerFragment);

            FeedViewPagerAdapter viewPagerAdapter = new FeedViewPagerAdapter(getChildFragmentManager());
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