package com.eulersbridge.isegoria.feed;

import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;

class FeedViewPagerAdapter extends FragmentPagerAdapter {

    private final NewsFragment newsFragment;
    private final PhotosFragment photosFragment;
    private final EventsFragment eventsFragment;

    FeedViewPagerAdapter(FragmentManager fm) {
        super(fm);

        newsFragment = new NewsFragment();
        photosFragment = new PhotosFragment();
        eventsFragment = new EventsFragment();
    }

    @Override
    public Fragment getItem(int arg0) {
        switch (arg0) {
            case 0:
                return newsFragment;
            case 1:
                return photosFragment;
            case 2:
                return eventsFragment;
        }
        return null;
    }

    @Override
    public int getCount() {
        return 3;
    }

    @Override
    public CharSequence getPageTitle(int position) {
        switch (position) {
            case 0:
                return "News";
            case 1:
                return "Photos";
            case 2:
                return "Events";
        }
        return null;
    }
}