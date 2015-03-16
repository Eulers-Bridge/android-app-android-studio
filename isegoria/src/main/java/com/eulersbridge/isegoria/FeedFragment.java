package com.eulersbridge.isegoria;

import java.text.DateFormat.Field;

import com.actionbarsherlock.app.ActionBar;
import com.actionbarsherlock.app.ActionBar.Tab;
import com.actionbarsherlock.app.SherlockFragment;
import com.actionbarsherlock.app.SherlockFragmentActivity;
import com.actionbarsherlock.app.ActionBar.TabListener;


import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.support.v4.app.FragmentTransaction;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RelativeLayout;


public class FeedFragment extends SherlockFragment implements TabListener {
	private View rootView;
	private NewsFragment newsFragment = null;
	private PhotosFragment photosFragment = null;
	private EventsFragment eventsFragment = null;
	private ViewGroup container = null;
	
	private boolean complete = false;
	
	public FeedFragment () {
		newsFragment = new NewsFragment();
		photosFragment = new PhotosFragment();
		eventsFragment = new EventsFragment();
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {   
		rootView = inflater.inflate(R.layout.feed_fragment, container, false);
		((SherlockFragmentActivity) getActivity()).getSupportActionBar().setNavigationMode(ActionBar.NAVIGATION_MODE_TABS);
		((SherlockFragmentActivity) getActivity()).getSupportActionBar().show();

		((SherlockFragmentActivity) getActivity()).getSupportActionBar().removeAllTabs();
		((SherlockFragmentActivity) getActivity()).getSupportActionBar().addTab(
				((SherlockFragmentActivity) getActivity()).getSupportActionBar().newTab()
	            .setText("News")
	            .setTabListener(this));
		((SherlockFragmentActivity) getActivity()).getSupportActionBar().addTab(
				((SherlockFragmentActivity) getActivity()).getSupportActionBar().newTab()
	            .setText("Photos")
	            .setTabListener(this));
		((SherlockFragmentActivity) getActivity()).getSupportActionBar().addTab(
				((SherlockFragmentActivity) getActivity()).getSupportActionBar().newTab()
	            .setText("Events")
	            .setTabListener(this));
	    
		ActionBar bar = ((SherlockFragmentActivity) getActivity()).getSupportActionBar();
		bar.setBackgroundDrawable(new ColorDrawable(Color.parseColor("#3C7EC9")));
		bar.setStackedBackgroundDrawable(new ColorDrawable(Color.parseColor("#313E4D")));
		bar.setSplitBackgroundDrawable(new ColorDrawable(Color.parseColor("#313E4D")));
		
		complete = true;
		return rootView;
	}
	

    @Override
    public void onTabSelected(Tab tab, FragmentTransaction ft) {    	
    	try {

	    	if(tab.getText().equals("News")) {
	    		((SherlockFragmentActivity) getActivity()).getSupportFragmentManager().popBackStack();
	    		((SherlockFragmentActivity) getActivity()).getSupportFragmentManager().beginTransaction().replace(R.id.content_feed_frame, newsFragment).commit();
	    		//ft.replace(R.id.content_feed_frame, newsFragment);
	    	}
	    	else if(tab.getText().equals("Photos")) {
	    		((SherlockFragmentActivity) getActivity()).getSupportFragmentManager().popBackStack();
	    		((SherlockFragmentActivity) getActivity()).getSupportFragmentManager().beginTransaction().replace(R.id.content_feed_frame, photosFragment).commit();
	    		//ft.replace(R.id.content_feed_frame, photosFragment);
	    	}
	    	else if(tab.getText().equals("Events")) {
	    		((SherlockFragmentActivity) getActivity()).getSupportFragmentManager().popBackStack();
	    		((SherlockFragmentActivity) getActivity()).getSupportFragmentManager().beginTransaction().replace(R.id.content_feed_frame, eventsFragment).commit();
	    		//ft.replace(R.id.content_feed_frame, eventsFragment);
	    	}
    	} catch(Exception e) {
    		
    	}
    }
	
    @Override
    public void onTabReselected(Tab tab, FragmentTransaction ft) {
    }

    @Override
    public void onTabUnselected(Tab tab, FragmentTransaction ft) {
    	try {
	    	if(tab.getText().equals("News")) {
	    		((SherlockFragmentActivity) getActivity()).getSupportFragmentManager().beginTransaction().remove(newsFragment).commit();
	    	}
	    	else if(tab.getText().equals("Photos")) {
	    		((SherlockFragmentActivity) getActivity()).getSupportFragmentManager().beginTransaction().remove(photosFragment).commit();
	    	}
	    	else if(tab.getText().equals("Events")) {
	    		((SherlockFragmentActivity) getActivity()).getSupportFragmentManager().beginTransaction().remove(eventsFragment).commit();
	    	}
    	} catch(Exception e) {
    		
    	}
    }
}