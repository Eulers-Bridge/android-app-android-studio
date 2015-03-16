package com.eulersbridge.isegoria;

import java.util.List;

import com.actionbarsherlock.app.SherlockFragment;

import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;

public class PollPagerAdapter extends FragmentPagerAdapter {
	private List<SherlockFragment> fragments;
	
	public PollPagerAdapter(FragmentManager fm, List<SherlockFragment> fragments) {
		super(fm);
	    this.fragments = fragments;
	}
	
	 @Override
	 public SherlockFragment getItem(int position) {
		 return this.fragments.get(position);
	 }
	 
	 @Override
	    public CharSequence getPageTitle(int position) {
	      return "Test";
	    }
	 
	 @Override
	 public int getCount() {
		 return this.fragments.size();
	 }
}
