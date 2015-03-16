package com.eulersbridge.isegoria;

import java.lang.reflect.Field;
import java.util.List;
import java.util.Vector;

import com.actionbarsherlock.app.SherlockFragment;
import com.actionbarsherlock.app.SherlockFragmentActivity;
import com.viewpagerindicator.CirclePageIndicator;
import com.viewpagerindicator.TabPageIndicator;
import com.viewpagerindicator.TitlePageIndicator;

import android.app.ActionBar;
import android.app.ActionBar.Tab;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.view.ViewPager;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

public class CandidateFragment extends SherlockFragment {
	private View rootView;
	private boolean loaded = false;
	private CandidatePagerAdapter candidatePagerAdapter;
	public List<SherlockFragment> fragments;
	
	public CandidateFragment() {

	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {   
		rootView = inflater.inflate(R.layout.candidate_fragment, container, false);

		fragments = new Vector<SherlockFragment>();
        fragments.add((SherlockFragment) SherlockFragment.instantiate(getActivity(), CandidatePositionsFragment.class.getName()));
        fragments.add((SherlockFragment) SherlockFragment.instantiate(getActivity(), CandidateTicketFragment.class.getName()));
        fragments.add((SherlockFragment) SherlockFragment.instantiate(getActivity(), CandidateAllFragment.class.getName()));

		ViewPager mViewPager = (ViewPager) rootView.findViewById(R.id.candidateViewPager);
		candidatePagerAdapter = new CandidatePagerAdapter(((SherlockFragmentActivity) getSherlockActivity()).getSupportFragmentManager(), fragments);
		mViewPager.setAdapter(candidatePagerAdapter);
		
		TabPageIndicator tabPageIndicator = (TabPageIndicator) rootView.findViewById(R.id.tabPageIndicatorCandidate);
		tabPageIndicator.setViewPager(mViewPager);
		tabPageIndicator.setBackgroundDrawable(new ColorDrawable(Color.parseColor("#313E4D")));
	    
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
}