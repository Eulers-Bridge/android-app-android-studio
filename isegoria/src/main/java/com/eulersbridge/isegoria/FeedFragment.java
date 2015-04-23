package com.eulersbridge.isegoria;

import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.view.ViewPager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.actionbarsherlock.app.ActionBar;
import com.actionbarsherlock.app.ActionBar.Tab;
import com.actionbarsherlock.app.ActionBar.TabListener;
import com.actionbarsherlock.app.SherlockFragment;
import com.actionbarsherlock.app.SherlockFragmentActivity;


public class FeedFragment extends SherlockFragment implements TabListener {
	private View rootView;
	private ViewGroup container = null;
    private android.support.v4.widget.SwipeRefreshLayout swipeLayout;
    private ViewPager mPager;
    private ActionBar bar;

    private boolean complete = false;

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		rootView = inflater.inflate(R.layout.feed_fragment, container, false);
		((SherlockFragmentActivity) getActivity()).getSupportActionBar().setNavigationMode(ActionBar.NAVIGATION_MODE_TABS);
		((SherlockFragmentActivity) getActivity()).getSupportActionBar().show();

        FragmentManager fm = getChildFragmentManager();

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
	    
		bar = ((SherlockFragmentActivity) getActivity()).getSupportActionBar();
		bar.setBackgroundDrawable(new ColorDrawable(Color.parseColor("#3C7EC9")));
		bar.setStackedBackgroundDrawable(new ColorDrawable(Color.parseColor("#313E4D")));
		bar.setSplitBackgroundDrawable(new ColorDrawable(Color.parseColor("#313E4D")));

        String[] tabNames = {"News", "Photos", "Events"};

        for(int i = 0; i<bar.getTabCount(); i++){
            LayoutInflater inflater1 = LayoutInflater.from(getActivity());
            View customView = inflater1.inflate(R.layout.tab_title, null);

            TextView titleTV = (TextView) customView.findViewById(R.id.action_custom_title);
            titleTV.setText(tabNames[i]);
            //Here you can also add any other styling you want.

            bar.getTabAt(i).setCustomView(customView);
        }

        ViewPager.SimpleOnPageChangeListener ViewPagerListener = new ViewPager.SimpleOnPageChangeListener() {
            @Override
            public void onPageSelected(int position) {
                super.onPageSelected(position);
                bar.setSelectedNavigationItem(position);
            }
        };

        mPager = (android.support.v4.view.ViewPager) rootView.findViewById(R.id.feedViewPagerFragment);

        FeedViewPagerAdapter viewpageradapter = new FeedViewPagerAdapter(fm);
        mPager.setAdapter(viewpageradapter);
        mPager.setOnPageChangeListener(ViewPagerListener);

        mPager.setCurrentItem(0);
		
		complete = true;
		return rootView;
	}
	

    @Override
    public void onTabSelected(Tab tab, FragmentTransaction ft) {    	
    	try {
            mPager.setCurrentItem(tab.getPosition());
    	} catch(Exception e) {
    		e.printStackTrace();
    	}
    }
	
    @Override
    public void onTabReselected(Tab tab, FragmentTransaction ft) {
    }

    @Override
    public void onTabUnselected(Tab tab, FragmentTransaction ft) {
    	/*try {
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
    		
    	}*/
    }
}