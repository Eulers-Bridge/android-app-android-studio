package com.eulersbridge.isegoria;

import android.app.ActionBar;
import android.os.Bundle;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.actionbarsherlock.app.SherlockFragment;
import com.actionbarsherlock.app.SherlockFragmentActivity;
import com.viewpagerindicator.TitlePageIndicator;

import java.util.List;
import java.util.Vector;

public class PollFragment extends SherlockFragment {
	private View rootView;
	private PagerAdapter pollPagerAdapter;
	public List<SherlockFragment> fragments;
	
	public PollFragment() {
		
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {   
		rootView = inflater.inflate(R.layout.poll_vote_fragment, container, false);
		((SherlockFragmentActivity) getActivity()).getSupportActionBar().setNavigationMode(ActionBar.NAVIGATION_MODE_STANDARD);
		
		fragments = new Vector<SherlockFragment>();
       // fragments.add((SherlockFragment) SherlockFragment.instantiate(getActivity(), PollVoteFragment.class.getName()));
       // fragments.add((SherlockFragment) SherlockFragment.instantiate(getActivity(), PollVoteFragment.class.getName()));
       // fragments.add((SherlockFragment) SherlockFragment.instantiate(getActivity(), PollVoteFragment.class.getName()));
		
		ViewPager mViewPager = (ViewPager) rootView.findViewById(R.id.pollViewPager);
		pollPagerAdapter = new PollPagerAdapter(((SherlockFragmentActivity) getActivity()).getSupportFragmentManager(), fragments);
		mViewPager.setAdapter(pollPagerAdapter);

        TitlePageIndicator tabIndicator = (TitlePageIndicator) rootView.findViewById(R.id.tabPageIndidcatorVote);
		tabIndicator.setViewPager(mViewPager);
		
        MainActivity mainActivity = (MainActivity) getActivity();
        Network network = mainActivity.getIsegoriaApplication().getNetwork();
        network.getPollQuestions(this);
		
		return rootView;
	}
	
	public void addQuestion(final int nodeId, final int creatorId, final String question, final String answers) {
		try {
			getActivity().runOnUiThread(new Runnable() {
			     @Override
			     public void run() {
			    	 PollVoteFragment pollVoteFragment = new PollVoteFragment();
			    	 pollVoteFragment.setData(nodeId, creatorId, question, answers);

			         fragments.add((SherlockFragment) pollVoteFragment);
			    	 pollPagerAdapter.notifyDataSetChanged();

			     }
			});
		} catch(Exception e) {
            Log.d("Error", e.toString());
        }
	}
}