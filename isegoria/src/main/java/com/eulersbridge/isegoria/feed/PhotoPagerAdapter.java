package com.eulersbridge.isegoria.feed;

import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;

import java.util.List;

/**
 * Created by Anthony on 03/04/2015.
 */
class PhotoPagerAdapter extends FragmentPagerAdapter {
    private final List<Fragment> fragments;

    PhotoPagerAdapter(FragmentManager fm, List<Fragment> fragments) {
        super(fm);
        this.fragments = fragments;
    }

    public void addFragment(Fragment fragment) {
        fragments.add(fragment);
    }

    @Override
    public Fragment getItem(int position) {
        return this.fragments.get(position);
    }

    @Override
    public CharSequence getPageTitle(int position) {
        return "Profile" + String.valueOf(position+1);
    }

    @Override
    public int getCount() {
        return this.fragments.size();
    }
}
