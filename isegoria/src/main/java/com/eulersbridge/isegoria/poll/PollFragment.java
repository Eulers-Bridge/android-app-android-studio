package com.eulersbridge.isegoria.poll;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.annotation.UiThread;
import android.support.design.widget.TabLayout;
import android.support.v4.app.Fragment;
import android.support.v4.view.ViewPager;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;

import com.eulersbridge.isegoria.MainActivity;
import com.eulersbridge.isegoria.Network;
import com.eulersbridge.isegoria.R;
import com.eulersbridge.isegoria.models.PollOption;
import com.eulersbridge.isegoria.utilities.SimpleFragmentPagerAdapter;
import com.eulersbridge.isegoria.utilities.Utils;
import com.eulersbridge.isegoria.models.User;

import java.util.ArrayList;
import java.util.List;
import java.util.Vector;

public class PollFragment extends Fragment {

    private TabLayout tabLayout;
    private ViewPager viewPager;
    private SimpleFragmentPagerAdapter pagerAdapter;

	private List<Fragment> fragments;

    private com.sothree.slidinguppanel.SlidingUpPanelLayout slidingUpPanelLayout;

    private boolean expanded = false;

	@Override
	public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        final View rootView = inflater.inflate(R.layout.poll_vote_fragment, container, false);

        // Ensure options menu from another fragment is not carried over
        getActivity().invalidateOptionsMenu();

        ((MainActivity)getActivity()).setToolbarTitle(getString(R.string.section_title_poll));

		fragments = new Vector<>();
		
        MainActivity mainActivity = (MainActivity) getActivity();
        Network network = mainActivity.getIsegoriaApplication().getNetwork();
        network.getPollQuestions(this);

        slidingUpPanelLayout = rootView.findViewById(R.id.sliding_layout);
        slidingUpPanelLayout.setTouchEnabled(false);
        slidingUpPanelLayout.setEnabled(false);

        setupViewPager(rootView);
        setupTabLayout();

        rootView.findViewById(R.id.voteButton).setOnClickListener(view -> {
            PollVoteFragment fragment = (PollVoteFragment)fragments.get(viewPager.getCurrentItem());
            fragment.postVote();
        });
		
		return rootView;
	}

    private void setupViewPager(View rootView) {
        if (rootView == null) rootView = getView();

        if (viewPager == null && rootView != null) {
            viewPager = rootView.findViewById(R.id.pollViewPager);

            pagerAdapter = new SimpleFragmentPagerAdapter(getChildFragmentManager(), fragments) {
                @Override
                public CharSequence getPageTitle(int position) {
                    return String.format("Poll %d", position + 1);
                }
            };
            viewPager.setAdapter(pagerAdapter);

            viewPager.setCurrentItem(0);
        }
    }

    public void setTabLayout(TabLayout tabLayout) {
        this.tabLayout = tabLayout;
    }

    @UiThread
    private void updateTabs() {
        pagerAdapter.notifyDataSetChanged();

        this.tabLayout.setVisibility(fragments.size() < 2? View.GONE : View.VISIBLE);
    }

    @Override
    public void onPause() {
        super.onPause();

        if (tabLayout != null) tabLayout.removeOnTabSelectedListener(onTabSelectedListener);
    }

    private final TabLayout.OnTabSelectedListener onTabSelectedListener = new TabLayout.OnTabSelectedListener() {
        @Override
        public void onTabSelected(TabLayout.Tab tab) {
            viewPager.setCurrentItem(tab.getPosition());
        }

        @Override
        public void onTabUnselected(TabLayout.Tab tab) { }

        @Override
        public void onTabReselected(TabLayout.Tab tab) { }
    };

    private void setupTabLayout() {
        if (tabLayout == null) return;

        tabLayout.removeAllTabs();
        tabLayout.setupWithViewPager(viewPager);
        tabLayout.addOnTabSelectedListener(onTabSelectedListener);
        tabLayout.setVisibility(View.VISIBLE);
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

            Utils.hideKeyboard(getActivity());
        }
    }

    public void addQuestion(final int nodeId, @Nullable final User creator, final String question, final ArrayList<PollOption> options) {
		try {
			getActivity().runOnUiThread(() -> {
                PollVoteFragment pollVoteFragment = new PollVoteFragment();
                pollVoteFragment.setData(nodeId, creator, question, options);

                fragments.add(pollVoteFragment);
                updateTabs();
            });
		} catch(Exception e) {
            Log.d("Error", e.toString());
        }
	}
}