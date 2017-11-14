package com.eulersbridge.isegoria.poll;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.UiThread;
import android.support.design.widget.TabLayout;
import android.support.v4.app.Fragment;
import android.support.v4.view.ViewPager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.eulersbridge.isegoria.Constant;
import com.eulersbridge.isegoria.Isegoria;
import com.eulersbridge.isegoria.R;
import com.eulersbridge.isegoria.utilities.TitledFragment;
import com.eulersbridge.isegoria.models.Poll;
import com.eulersbridge.isegoria.network.PollsResponse;
import com.eulersbridge.isegoria.network.SimpleCallback;
import com.eulersbridge.isegoria.utilities.SimpleFragmentPagerAdapter;

import org.parceler.Parcels;

import java.util.List;
import java.util.Vector;

import retrofit2.Response;

public class PollFragment extends Fragment implements TitledFragment {

    private TabLayout tabLayout;
    private ViewPager viewPager;
    private SimpleFragmentPagerAdapter pagerAdapter;

	private List<Fragment> fragments;

	@Override
	public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        final View rootView = inflater.inflate(R.layout.poll_vote_fragment, container, false);

        // Ensure options menu from another fragment is not carried over
        getActivity().invalidateOptionsMenu();

		fragments = new Vector<>();

		Isegoria isegoria = (Isegoria)getActivity().getApplication();

		long institutionId = isegoria.getLoggedInUser().institutionId;
		isegoria.getAPI().getPolls(institutionId).enqueue(new SimpleCallback<PollsResponse>() {
            @Override
            protected void handleResponse(Response<PollsResponse> response) {
                PollsResponse body = response.body();
                if (body != null && body.totalPolls > 0) {
                    for (Poll poll : body.polls) {
                        addPoll(poll);
                    }
                }
            }
        });

        setupViewPager(rootView);
        setupTabLayout();
		
		return rootView;
	}

    @Override
    public String getTitle() {
        return getString(R.string.section_title_poll);
    }

    private void setupViewPager(View rootView) {
        if (rootView == null) rootView = getView();

        if (viewPager == null && rootView != null) {
            viewPager = rootView.findViewById(R.id.poll_vote_view_pager);

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

        tabLayout.setVisibility(fragments.size() < 2? View.GONE : View.VISIBLE);
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

    private void addPoll(@NonNull Poll poll) {
        if (getActivity() != null) {
            getActivity().runOnUiThread(() -> {
                PollVoteFragment pollVoteFragment = new PollVoteFragment();

                Bundle args = new Bundle();
                args.putParcelable(Constant.ACTIVITY_EXTRA_POLL, Parcels.wrap(poll));

                pollVoteFragment.setArguments(args);

                fragments.add(pollVoteFragment);
                updateTabs();
            });
        }
	}
}