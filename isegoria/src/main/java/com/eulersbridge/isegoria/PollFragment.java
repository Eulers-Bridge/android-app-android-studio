package com.eulersbridge.isegoria;

import android.content.Context;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.design.widget.TabLayout;
import android.support.v4.app.Fragment;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;

import com.viewpagerindicator.TitlePageIndicator;

import java.util.List;
import java.util.Vector;

public class PollFragment extends Fragment {
    private TabLayout tabLayout;
    private PagerAdapter pollPagerAdapter;
	private List<Fragment> fragments;

    private com.sothree.slidinguppanel.SlidingUpPanelLayout slidingUpPanelLayout;
    private Network network;

    private boolean expanded = false;

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        final View rootView = inflater.inflate(R.layout.poll_vote_fragment, container, false);

		fragments = new Vector<>();
		
		final ViewPager mViewPager = rootView.findViewById(R.id.pollViewPager);
		pollPagerAdapter = new PollPagerAdapter(getChildFragmentManager(), fragments);
		mViewPager.setAdapter(pollPagerAdapter);

        TitlePageIndicator tabIndicator = rootView.findViewById(R.id.tabPageIndidcatorVote);
		tabIndicator.setViewPager(mViewPager);
		
        MainActivity mainActivity = (MainActivity) getActivity();
        network = mainActivity.getIsegoriaApplication().getNetwork();
        network.getPollQuestions(this);

        slidingUpPanelLayout = rootView.findViewById(R.id.sliding_layout);
        slidingUpPanelLayout.setTouchEnabled(false);
        slidingUpPanelLayout.setEnabled(false);
		
		return rootView;
	}

    public void setTabLayout(TabLayout tabLayout) {
        this.tabLayout = tabLayout;

        tabLayout.setVisibility(View.GONE);
    }

    public void collapseBar(MotionEvent ev) {
        if (ev.getActionMasked() == MotionEvent.ACTION_DOWN) {
            collapseBarSlideDown();
        }
    }

    private void collapseBarSlideDown() {
        if (expanded) {
            expanded = false;
            slidingUpPanelLayout.setPanelHeight(100);
            InputMethodManager imm = (InputMethodManager) getActivity().getSystemService(Context.INPUT_METHOD_SERVICE);
            imm.hideSoftInputFromWindow(getActivity().getCurrentFocus().getWindowToken(), 0);
        }
    }

    public void addQuestion(final int nodeId, @Nullable final User creator, final String question, final String answers, final int numOfAnswers) {
		try {
			getActivity().runOnUiThread(new Runnable() {
			     @Override
			     public void run() {
			    	 PollVoteFragment pollVoteFragment = new PollVoteFragment();
			    	 pollVoteFragment.setData(nodeId, creator, question, answers, numOfAnswers);

			         fragments.add(pollVoteFragment);
			    	 pollPagerAdapter.notifyDataSetChanged();

			     }
			});
		} catch(Exception e) {
            Log.d("Error", e.toString());
        }
	}
}