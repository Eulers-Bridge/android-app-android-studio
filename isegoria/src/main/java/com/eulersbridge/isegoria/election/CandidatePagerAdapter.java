package com.eulersbridge.isegoria.election;

import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;

import java.util.List;

class CandidatePagerAdapter extends FragmentStatePagerAdapter  {
	private final List<Fragment> fragments;
	
	CandidatePagerAdapter(FragmentManager fm, List<Fragment> fragments) {
		super(fm);
	    this.fragments = fragments;
	}
	
	 @Override
	 public Fragment getItem(int position) {
		 return this.fragments.get(position);
	 }
	 
	 @Override
	    public CharSequence getPageTitle(int position) {
			switch(position) {
				case 0:
					return "Position";
				case 1:
					return "Ticket";
				case 2:
					return "All";
				default:
					return "";
			}
	    }
	 
	 @Override
	 public int getCount() {
		 return this.fragments.size();
	 }
}
