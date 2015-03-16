package com.eulersbridge.isegoria;

import java.lang.reflect.Field;
import java.util.List;
import java.util.Vector;

import com.actionbarsherlock.app.ActionBar;
import com.actionbarsherlock.app.SherlockFragment;
import com.actionbarsherlock.app.SherlockFragmentActivity;
import com.viewpagerindicator.CirclePageIndicator;
import com.viewpagerindicator.TabPageIndicator;
import com.viewpagerindicator.TitlePageIndicator;



import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.view.ViewPager;
import android.support.v4.view.ViewPager.OnPageChangeListener;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

public class ElectionFragment extends SherlockFragment implements OnPageChangeListener {
	private View rootView;
	private boolean loaded = false;
	private ElectionPagerAdapter electionPagerAdapter;
	public ViewPager mViewPager;
	public TabPageIndicator tabPageIndicator;
	public List<SherlockFragment> fragments;
	
	public ElectionFragment() {

	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {   
		rootView = inflater.inflate(R.layout.election_fragment, container, false);
		
		fragments = new Vector<SherlockFragment>();
        fragments.add((SherlockFragment) SherlockFragment.instantiate(getActivity(), ElectionOverviewFragment.class.getName()));
        fragments.add((SherlockFragment) SherlockFragment.instantiate(getActivity(), ElectionProcessFragment.class.getName()));
        fragments.add((SherlockFragment) SherlockFragment.instantiate(getActivity(), ElectionPositionsFragment.class.getName()));

		mViewPager = (ViewPager) rootView.findViewById(R.id.electionViewPager);
		electionPagerAdapter = new ElectionPagerAdapter(((SherlockFragmentActivity) getActivity()).getSupportFragmentManager(), fragments);
		mViewPager.setAdapter(electionPagerAdapter);
		
		tabPageIndicator = (TabPageIndicator) rootView.findViewById(R.id.tabPageIndicator);
		tabPageIndicator.setViewPager(mViewPager);
		tabPageIndicator.setBackgroundDrawable(new ColorDrawable(Color.parseColor("#313E4D")));
		tabPageIndicator.setOnPageChangeListener(this);
		
		return rootView;
	}
	
	@Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
		
	}
	
	public boolean isLoaded() {
		return loaded;
	}

	public void setLoaded(boolean loaded) {
		this.loaded = loaded;
	}
	
	public void getElectionTabs() {
	
	}
	
	public void getCandidatesTabs() {
	
	}

	@Override
	public void onPageScrollStateChanged(int arg0) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void onPageScrolled(int arg0, float arg1, int arg2) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void onPageSelected(int arg0) {
		// TODO Auto-generated method stub
		
	}
}