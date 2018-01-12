package com.eulersbridge.isegoria.poll;

import android.content.Context;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.UiThread;
import android.support.design.widget.TabLayout;
import android.support.v4.app.Fragment;
import android.support.v4.view.ViewPager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.eulersbridge.isegoria.IsegoriaApp;
import com.eulersbridge.isegoria.MainActivity;
import com.eulersbridge.isegoria.network.api.responses.PollsResponse;
import com.eulersbridge.isegoria.network.api.models.Poll;
import com.eulersbridge.isegoria.network.api.models.User;
import com.eulersbridge.isegoria.util.Constants;
import com.eulersbridge.isegoria.util.network.SimpleCallback;
import com.eulersbridge.isegoria.util.ui.SimpleFragmentPagerAdapter;
import com.eulersbridge.isegoria.util.ui.TitledFragment;
import com.eulersbridge.isegoria.R;

import org.parceler.Parcels;

import java.util.List;
import java.util.Vector;

import retrofit2.Response;

public class PollsFragment extends Fragment implements TitledFragment, MainActivity.TabbedFragment {

    private TabLayout tabLayout;
    private ViewPager viewPager;
    private SimpleFragmentPagerAdapter pagerAdapter;

	private List<Fragment> fragments;

    @Override
	public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        final View rootView = inflater.inflate(R.layout.poll_vote_fragment, container, false);

        fragments = new Vector<>();

		if (getActivity() != null) {
            // Ensure options menu from another fragment is not carried over
            getActivity().invalidateOptionsMenu();

            IsegoriaApp isegoriaApp = (IsegoriaApp)getActivity().getApplication();
            getPolls(isegoriaApp);
        }

        setupViewPager(rootView);
        setupTabLayout();
		
		return rootView;
	}

	private void getPolls(IsegoriaApp isegoriaApp) {
        User user = isegoriaApp.getLoggedInUser();
        if (user != null) {
            Long institutionId = isegoriaApp.getLoggedInUser().institutionId;

            if (institutionId != null) {
                isegoriaApp.getAPI().getPolls(institutionId).enqueue(new SimpleCallback<PollsResponse>() {
                    @Override
                    protected void handleResponse(Response<PollsResponse> response) {
                        PollsResponse body = response.body();

                        if (body != null && body.totalPolls > 0)
                            addPolls(body.polls);
                    }
                });
            }
        }
    }

    @Override
    public String getTitle(Context context) {
        return context.getString(R.string.section_title_poll);
    }

    private void setupViewPager(View rootView) {
        if (rootView == null) rootView = getView();

        if (viewPager == null && rootView != null) {
            viewPager = rootView.findViewById(R.id.poll_vote_view_pager);

            pagerAdapter = new SimpleFragmentPagerAdapter(getChildFragmentManager(), fragments) {
                @Override
                public CharSequence getPageTitle(int position) {
                    return getString(R.string.poll_title, position + 1);
                }
            };
            viewPager.setAdapter(pagerAdapter);

            viewPager.setCurrentItem(0);
        }
    }

    @Override
    public void setupTabLayout(TabLayout tabLayout) {
        this.tabLayout = tabLayout;

        tabLayout.removeAllTabs();
        tabLayout.setVisibility(View.VISIBLE);
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

        tabLayout.setupWithViewPager(viewPager);
        tabLayout.addOnTabSelectedListener(onTabSelectedListener);
    }

    private void addPolls(@NonNull List<Poll> polls) {
        if (getActivity() != null) {
            getActivity().runOnUiThread(() -> {
                for (Poll poll : polls) {
                    PollVoteFragment pollVoteFragment = new PollVoteFragment();

                    Bundle args = new Bundle();
                    args.putParcelable(Constants.ACTIVITY_EXTRA_POLL, Parcels.wrap(poll));

                    pollVoteFragment.setArguments(args);

                    fragments.add(pollVoteFragment);
                    updateTabs();
                }
            });
        }
	}
}