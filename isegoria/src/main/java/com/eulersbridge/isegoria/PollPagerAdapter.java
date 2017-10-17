package com.eulersbridge.isegoria;

import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;

import java.util.List;

class PollPagerAdapter extends FragmentPagerAdapter {
	private final List<Fragment> fragments;
	
	public PollPagerAdapter(FragmentManager fm, List<Fragment> fragments) {
		super(fm);
	    this.fragments = fragments;
	}
	
	 @Override
	 public Fragment getItem(int position) {
		 return this.fragments.get(position);
	 }
	 
	 @Override
	 public CharSequence getPageTitle(int position) {
	      return "Poll" + String.valueOf(position+1);
	    }
	 
	 @Override
	 public int getCount() {
		 return this.fragments.size();
	 }
}
