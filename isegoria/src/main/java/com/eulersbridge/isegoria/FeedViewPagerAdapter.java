package com.eulersbridge.isegoria;

import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;

public class FeedViewPagerAdapter extends FragmentPagerAdapter {
    final int PAGE_COUNT = 3;

    public FeedViewPagerAdapter(FragmentManager fm) {
        super(fm);
    }

    @Override
    public Fragment getItem(int arg0) {
        switch (arg0) {
            case 0:
                NewsFragment fragmenttab1 = new NewsFragment();
                return fragmenttab1;
            case 1:
                PhotosFragment fragmenttab2 = new PhotosFragment();
                return fragmenttab2;
            case 2:
                EventsFragment fragmenttab3 = new EventsFragment();
                return fragmenttab3;
        }
        return null;
    }

    @Override
    public int getCount() {
        return PAGE_COUNT;
    }
}