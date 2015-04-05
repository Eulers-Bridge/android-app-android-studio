package com.eulersbridge.isegoria;

import android.app.ActionBar;
import android.os.Bundle;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;

import com.actionbarsherlock.app.SherlockFragment;
import com.actionbarsherlock.app.SherlockFragmentActivity;
import com.viewpagerindicator.TitlePageIndicator;

import java.util.List;
import java.util.Vector;

public class PollFragment extends SherlockFragment {
	private View rootView;
	private PagerAdapter pollPagerAdapter;
	public List<SherlockFragment> fragments;

    private com.sothree.slidinguppanel.SlidingUpPanelLayout slidingUpPanelLayout;
    private PollFragment pollFragment;
    private Network network;

    private EditText commentsField;

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {   
		rootView = inflater.inflate(R.layout.poll_vote_fragment, container, false);
		((SherlockFragmentActivity) getActivity()).getSupportActionBar().setNavigationMode(ActionBar.NAVIGATION_MODE_STANDARD);

        pollFragment = this;
		fragments = new Vector<SherlockFragment>();
		
		final ViewPager mViewPager = (ViewPager) rootView.findViewById(R.id.pollViewPager);
		pollPagerAdapter = new PollPagerAdapter(getChildFragmentManager(), fragments);
		mViewPager.setAdapter(pollPagerAdapter);

        TitlePageIndicator tabIndicator = (TitlePageIndicator) rootView.findViewById(R.id.tabPageIndidcatorVote);
		tabIndicator.setViewPager(mViewPager);
		
        MainActivity mainActivity = (MainActivity) getActivity();
        network = mainActivity.getIsegoriaApplication().getNetwork();
        network.getPollQuestions(this);

        slidingUpPanelLayout = (com.sothree.slidinguppanel.SlidingUpPanelLayout)
                rootView.findViewById(R.id.sliding_layout);
        //slidingUpPanelLayout.setTouchEnabled(false);

        commentsField = (EditText) rootView.findViewById(R.id.commentsField);

        Button postButton = (Button) rootView.findViewById(R.id.postButton);
        postButton.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                PollVoteFragment pollVoteFragment = ((PollVoteFragment) fragments.get(mViewPager.getCurrentItem()));
                network.postPollComment(pollVoteFragment.getNodeId(), pollFragment);
            }
        });
		
		return rootView;
	}

    public EditText getCommentsField() {
        return commentsField;
    }

    public void setCommentsField(EditText commentsField) {
        this.commentsField = commentsField;
    }

    public void addQuestion(final int nodeId, final int creatorId, final String question, final String answers) {
		try {
			getActivity().runOnUiThread(new Runnable() {
			     @Override
			     public void run() {
			    	 PollVoteFragment pollVoteFragment = new PollVoteFragment();
			    	 pollVoteFragment.setData(nodeId, creatorId, question, answers);

			         fragments.add((SherlockFragment) pollVoteFragment);
                     network.getPollComments(nodeId, pollVoteFragment);
			    	 pollPagerAdapter.notifyDataSetChanged();

			     }
			});
		} catch(Exception e) {
            Log.d("Error", e.toString());
        }
	}
}