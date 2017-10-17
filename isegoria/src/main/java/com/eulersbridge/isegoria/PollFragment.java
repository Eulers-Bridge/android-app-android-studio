package com.eulersbridge.isegoria;

import android.content.Context;
import android.os.Bundle;
import android.support.design.widget.TabLayout;
import android.support.v4.app.Fragment;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.util.Log;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;

import com.viewpagerindicator.TitlePageIndicator;

import java.util.List;
import java.util.Vector;

public class PollFragment extends Fragment {
    private TabLayout tabLayout;
    private PagerAdapter pollPagerAdapter;
	private List<Fragment> fragments;

    private com.sothree.slidinguppanel.SlidingUpPanelLayout slidingUpPanelLayout;
    private Network network;

    private EditText commentsField;
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

        commentsField = rootView.findViewById(R.id.commentsField);
        LinearLayout commentsLayout = rootView.findViewById(R.id.commentsLayout);

        commentsLayout.setOnTouchListener(new View.OnTouchListener() {
            public boolean onTouch(View v, MotionEvent event) {
                if (event.getActionMasked() == MotionEvent.ACTION_DOWN) {
                    if (!expanded) {
                        expanded = true;
                        int sliderHeight = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP,
                                (float) 400, getResources().getDisplayMetrics());
                        slidingUpPanelLayout.setPanelHeight(sliderHeight);
                        commentsField.setFocusableInTouchMode(true);
                        commentsField.requestFocus();
                        InputMethodManager imm = (InputMethodManager) getActivity().getSystemService(Context.INPUT_METHOD_SERVICE);
                        imm.showSoftInput(commentsField, InputMethodManager.SHOW_IMPLICIT);
                    }
                }
                return true;
            }
        });

        Button postButton = rootView.findViewById(R.id.postButton);
        postButton.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                PollVoteFragment pollVoteFragment = ((PollVoteFragment) fragments.get(mViewPager.getCurrentItem()));
                String comment = (PollFragment.this.getCommentsField().getText().toString());

                if(!comment.trim().equals("")) {
                    network.postPollComment(pollVoteFragment.getNodeId(), comment, PollFragment.this);
                    collapseBarSlideDown();

                    pollVoteFragment.addTableComment((PollFragment.this.network.getLoginGivenName()
                            + " " + PollFragment.this.network.getLoginFamilyName()), comment, network.getLoginEmail());
                    PollFragment.this.getCommentsField().setText("");
                }
            }
        });
		
		return rootView;
	}

    public void setTabLayout(TabLayout tabLayout) {
        this.tabLayout = tabLayout;

        tabLayout.setVisibility(View.GONE);
    }

    private EditText getCommentsField() {
        return commentsField;
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
            commentsField.setFocusableInTouchMode(false);
            InputMethodManager imm = (InputMethodManager) getActivity().getSystemService(Context.INPUT_METHOD_SERVICE);
            imm.hideSoftInputFromWindow(getActivity().getCurrentFocus().getWindowToken(), 0);
        }
    }

    public void setCommentsField(EditText commentsField) {
        this.commentsField = commentsField;
    }

    public void addQuestion(final int nodeId, final int creatorId, final String question, final String answers, final int numOfComments, final int numOfAnswers) {
		try {
			getActivity().runOnUiThread(new Runnable() {
			     @Override
			     public void run() {
			    	 PollVoteFragment pollVoteFragment = new PollVoteFragment();
			    	 pollVoteFragment.setData(nodeId, creatorId, question, answers, numOfComments, numOfAnswers);

			         fragments.add(pollVoteFragment);
                     network.getPollComments(nodeId, pollVoteFragment);
			    	 pollPagerAdapter.notifyDataSetChanged();

			     }
			});
		} catch(Exception e) {
            Log.d("Error", e.toString());
        }
	}
}