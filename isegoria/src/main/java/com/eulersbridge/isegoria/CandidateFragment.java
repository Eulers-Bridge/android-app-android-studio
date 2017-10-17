package com.eulersbridge.isegoria;

import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.view.ViewPager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.viewpagerindicator.TabPageIndicator;

import java.util.List;
import java.util.Vector;

public class CandidateFragment extends Fragment {
	private boolean loaded = false;

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		final View rootView = inflater.inflate(R.layout.candidate_fragment, container, false);

		final List<Fragment> fragments = new Vector<>();
        fragments.add(Fragment.instantiate(getActivity(), CandidatePositionsFragment.class.getName()));
        fragments.add(Fragment.instantiate(getActivity(), CandidateTicketFragment.class.getName()));
        fragments.add(Fragment.instantiate(getActivity(), CandidateAllFragment.class.getName()));

		final ViewPager mViewPager = rootView.findViewById(R.id.candidateViewPager);
		final CandidatePagerAdapter candidatePagerAdapter = new CandidatePagerAdapter(getActivity().getSupportFragmentManager(), fragments);
		mViewPager.setAdapter(candidatePagerAdapter);
		
		final TabPageIndicator tabPageIndicator = rootView.findViewById(R.id.tabPageIndicatorCandidate);
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