package com.eulersbridge.isegoria;

import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;

import java.util.List;

/**
 * Created by Anthony on 30/03/2015.
 */
class ContactProfilePagerAdapter extends FragmentPagerAdapter {
    private final List<Fragment> fragments;

    public ContactProfilePagerAdapter(FragmentManager fm, List<Fragment> fragments) {
        super(fm);
        this.fragments = fragments;
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
