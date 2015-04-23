package com.eulersbridge.isegoria;

import android.app.ActionBar;
import android.content.Context;
import android.os.Bundle;
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

import com.actionbarsherlock.app.SherlockFragment;
import com.actionbarsherlock.app.SherlockFragmentActivity;
import com.sothree.slidinguppanel.SlidingUpPanelLayout;
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
    private boolean expanded = false;

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
        slidingUpPanelLayout.setTouchEnabled(false);
        slidingUpPanelLayout.setEnabled(false);
        SlidingUpPanelLayout.PanelSlideListener panelListener = new SlidingUpPanelLayout.PanelSlideListener(){
            public void onPanelCollapsed(View arg0) {

            }
            public void onPanelHidden(View arg0) {

            }
            public void onPanelAnchored(View arg0) {

            }
            public void onPanelExpanded(View arg0) {

            }
            public void onPanelSlide(View arg0, float value) {

            }
        };
        slidingUpPanelLayout.setPanelSlideListener(panelListener);

        commentsField = (EditText) rootView.findViewById(R.id.commentsField);
        LinearLayout commentsLayout = (LinearLayout) rootView.findViewById(R.id.commentsLayout);

        commentsLayout.setOnTouchListener(new View.OnTouchListener() {
            public boolean onTouch(View v, MotionEvent event) {
                if (event.getActionMasked() == MotionEvent.ACTION_DOWN) {
                    if (expanded == false) {
                        expanded = true;
                        int sliderHeight = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP,
                                (float) 400, getResources().getDisplayMetrics());
                        slidingUpPanelLayout.setPanelHeight(sliderHeight);
                        commentsField.setFocusableInTouchMode(true);
                        commentsField.requestFocus();
                        InputMethodManager imm = (InputMethodManager) getSherlockActivity().getSystemService(Context.INPUT_METHOD_SERVICE);
                        imm.showSoftInput(commentsField, InputMethodManager.SHOW_IMPLICIT);
                    }
                }
                return true;
            }
        });

        Button postButton = (Button) rootView.findViewById(R.id.postButton);
        postButton.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                PollVoteFragment pollVoteFragment = ((PollVoteFragment) fragments.get(mViewPager.getCurrentItem()));
                String comment = (pollFragment.getCommentsField().getText().toString());

                if(!comment.trim().equals("")) {
                    network.postPollComment(pollVoteFragment.getNodeId(), comment, pollFragment);
                    collapseBarSlideDown();

                    pollVoteFragment.addTableComment((pollFragment.network.getLoginGivenName()
                            + " " + pollFragment.network.getLoginFamilyName()), comment, network.getLoginEmail());
                    pollFragment.getCommentsField().setText("");
                }
            }
        });
		
		return rootView;
	}

    public EditText getCommentsField() {
        return commentsField;
    }

    public void collapseBar(MotionEvent ev) {
        if (ev.getActionMasked() == MotionEvent.ACTION_DOWN) {
            collapseBarSlideDown();
        }
    }

    public void collapseBarSlideDown() {
        if (expanded == true) {
            expanded = false;
            slidingUpPanelLayout.setPanelHeight(100);
            commentsField.setFocusableInTouchMode(false);
            InputMethodManager imm = (InputMethodManager) getSherlockActivity().getSystemService(Context.INPUT_METHOD_SERVICE);
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