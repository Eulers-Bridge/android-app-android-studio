package com.eulersbridge.isegoria.feed;

import android.content.Context;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.TabLayout;
import android.support.v4.app.Fragment;
import android.support.v4.content.ContextCompat;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;

import com.eulersbridge.isegoria.MainActivity;
import com.eulersbridge.isegoria.util.Utils;
import com.eulersbridge.isegoria.util.ui.TitledFragment;
import com.eulersbridge.isegoria.R;


public class FeedFragment extends Fragment implements TitledFragment, MainActivity.TabbedFragment {

    private TabLayout tabLayout;
    private ViewPager viewPager;

    @Override
	public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.feed_fragment, container, false);

        AppCompatActivity activity = (AppCompatActivity)getActivity();

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP && activity != null) {
            int colour = ContextCompat.getColor(activity, R.color.darkBlue);
            activity.getWindow().setStatusBarColor(colour);

            Utils.setMultitaskColour(activity, colour);
            Utils.setStatusBarColour(activity, colour);
        }

        if (activity != null) {
            // Ensure options menu from another fragment is not carried over
            activity.invalidateOptionsMenu();

            Utils.hideKeyboard(activity);
        }

        setHasOptionsMenu(true);

        setupViewPager(rootView);

		return rootView;
	}

	public String getTitle(Context context) {
	    return context.getString(R.string.section_title_feed);
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.feed, menu);
        super.onCreateOptionsMenu(menu, inflater);

        MenuItem friendsItem = menu.findItem(R.id.feed_menu_item_friends);
        Drawable drawable = friendsItem.getIcon();
        if (drawable != null) {
            drawable.mutate();
            drawable.setColorFilter(Color.WHITE, PorterDuff.Mode.SRC_ATOP);
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.feed_menu_item_friends:
                if (getActivity() != null)
                    ((MainActivity)getActivity()).showFriends();

                return true;

            default:
                return super.onOptionsItemSelected(item);
        }
    }

    private void setupViewPager(View rootView) {
        viewPager = rootView.findViewById(R.id.feedViewPagerFragment);
        viewPager.setOffscreenPageLimit(3);

        FeedViewPagerAdapter viewPagerAdapter = new FeedViewPagerAdapter(getChildFragmentManager());
        viewPager.setAdapter(viewPagerAdapter);

        viewPager.setCurrentItem(0);

        if (tabLayout != null) tabLayout.setupWithViewPager(viewPager);
    }

    @Override
	public void setupTabLayout(TabLayout tabLayout) {
        this.tabLayout = tabLayout;

        tabLayout.removeAllTabs();
        tabLayout.setVisibility(View.VISIBLE);
        tabLayout.addOnTabSelectedListener(onTabSelectedListener);

        if (viewPager != null) tabLayout.setupWithViewPager(viewPager);
    }

    @Override
    public void onPause() {
        super.onPause();

        if (tabLayout != null) tabLayout.removeOnTabSelectedListener(onTabSelectedListener);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();

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