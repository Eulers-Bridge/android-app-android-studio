package com.eulersbridge.isegoria.feed;

import android.content.Context;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;

import com.eulersbridge.isegoria.util.ui.TitledFragment;
import com.eulersbridge.isegoria.feed.events.EventsFragment;
import com.eulersbridge.isegoria.feed.news.NewsFragment;
import com.eulersbridge.isegoria.feed.photos.PhotosFragment;

import java.util.ArrayList;

class FeedViewPagerAdapter extends FragmentPagerAdapter {

    final private ArrayList<Fragment> fragments;
    final private int fragmentCount = 3;

    FeedViewPagerAdapter(FragmentManager fragmentManager) {
        super(fragmentManager);

        fragments = new ArrayList<>(fragmentCount);
    }

    @Override
    public Fragment getItem(int position) {
        Fragment fragment;

        try {
            fragment = fragments.get(position);

        } catch (Exception e) {
            if (position == 1) {
                fragment = new PhotosFragment();
            } else if (position == 2) {
                fragment = new EventsFragment();
            } else {
                // 0, etc.
                fragment = new NewsFragment();
            }

            fragments.add(position, fragment);
        }

        return fragment;
    }

    @Override
    public int getCount() {
        return fragmentCount;
    }

    @Override
    public CharSequence getPageTitle(int position) {

        Fragment fragment = getItem(position);
        if (fragment instanceof TitledFragment) {
            Context context = fragment.getContext();
            return ((TitledFragment) fragment).getTitle(context);
        }

        return null;
    }
}