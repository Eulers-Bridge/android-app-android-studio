package com.eulersbridge.isegoria;

import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;

import java.util.List;

class CandidatePagerAdapter extends FragmentStatePagerAdapter  {
	private final List<Fragment> fragments;
	
	public CandidatePagerAdapter(FragmentManager fm, List<Fragment> fragments) {
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
		 		return "By Type";
		 	}
		 	else if(position == 1) {
		 		return "By Ticket";
		 	}
		 	else if(position == 2) {
		 		return "List";
		 	}
		 	
		 	return "";
	    }
	 
	 @Override
	 public int getCount() {
		 return this.fragments.size();
	 }
}
