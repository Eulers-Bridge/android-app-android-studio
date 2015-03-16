package com.eulersbridge.isegoria;

import java.util.List;

import com.actionbarsherlock.app.SherlockFragment;


import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.app.FragmentStatePagerAdapter;

public class ElectionPagerAdapter extends FragmentStatePagerAdapter  {
	private List<SherlockFragment> fragments;
	
	public ElectionPagerAdapter(FragmentManager fm, List<SherlockFragment> fragments) {
		super(fm);
	    this.fragments = fragments;
	}
	
	 @Override
	 public SherlockFragment getItem(int position) {
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
