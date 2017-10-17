package com.eulersbridge.isegoria;

import java.util.List;

import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;

class ElectionPagerAdapter extends FragmentStatePagerAdapter  {
	private final List<Fragment> fragments;
	
	ElectionPagerAdapter(FragmentManager fm, List<Fragment> fragments) {
		super(fm);
	    this.fragments = fragments;
	}
	
	 @Override
	 public Fragment getItem(int position) {
		 return this.fragments.get(position);
	 }
	 
	 @Override
	    public CharSequence getPageTitle(int position) {
		 	if(position == 0) {
		 		return "Overview";
		 	}
		 	else if(position == 1) {
		 		return "Process";
		 	}
		 	else if(position == 2) {
		 		return "Positions";
		 	}
		 	
		 	return "";
	    }
	 
	 @Override
	 public int getCount() {
		 return this.fragments.size();
	 }
}
