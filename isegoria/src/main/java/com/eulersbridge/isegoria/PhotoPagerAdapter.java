package com.eulersbridge.isegoria;

import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;

import com.actionbarsherlock.app.SherlockFragment;

import java.util.List;

/**
 * Created by Anthony on 03/04/2015.
 */
public class PhotoPagerAdapter extends FragmentPagerAdapter {
    private List<SherlockFragment> fragments;

    public PhotoPagerAdapter(FragmentManager fm, List<SherlockFragment> fragments) {
        super(fm);
        this.fragments = fragments;
    }

    public void addFragment(SherlockFragment fragment) {
        fragments.add(fragment);
    }

    @Override
    public SherlockFragment getItem(int position) {
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
