package com.eulersbridge.isegoria.vote;

import android.content.Context;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.TabLayout;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.eulersbridge.isegoria.MainActivity;
import com.eulersbridge.isegoria.R;
import com.eulersbridge.isegoria.common.NonSwipeableViewPager;
import com.eulersbridge.isegoria.common.SimpleFragmentPagerAdapter;
import com.eulersbridge.isegoria.common.TitledFragment;
import com.eulersbridge.isegoria.models.VoteLocation;

import org.parceler.Parcels;

import java.util.ArrayList;
import java.util.Calendar;

public class VoteViewPagerFragment extends Fragment
        implements TitledFragment, MainActivity.TabbedFragment, VoteFragment.VoteFragmentListener, VotePledgeFragment.VotePledgeListener {

    private TabLayout tabLayout;

    private SimpleFragmentPagerAdapter viewPagerAdapter;
    private NonSwipeableViewPager viewPager;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.vote_view_pager_fragment, container, false);

        // Ensure options menu from another fragment is not carried over
        getActivity().invalidateOptionsMenu();

        setupViewPager(rootView);

        return rootView;
    }

    @Override
    public String getTitle(Context context) {
        return context.getString(R.string.section_title_vote);
    }

    private void setupViewPager(View rootView) {
        if (rootView == null) rootView = getView();

        if (viewPager == null && rootView != null) {
            viewPager = rootView.findViewById(R.id.vote_view_pager_fragment);

            ArrayList<Fragment> fragments = new ArrayList<>();

            VoteFragment voteFragment = new VoteFragment();
            voteFragment.setListener(this);

            VotePledgeFragment votePledgeFragment = new VotePledgeFragment();
            votePledgeFragment.setListener(this);

            fragments.add(voteFragment);
            fragments.add(votePledgeFragment);
            fragments.add(new VoteDoneFragment());

            viewPagerAdapter = new SimpleFragmentPagerAdapter(getChildFragmentManager(), fragments) {
                @Override
                public CharSequence getPageTitle(int position) {

                    Fragment fragment = viewPagerAdapter.getItem(position);
                    if (fragment instanceof TitledFragment) {
                        return ((TitledFragment) fragment).getTitle(getContext());

                    } else {
                        return null;
                    }
                }
            };

            viewPager.setAdapter(viewPagerAdapter);

            viewPager.setCurrentItem(0);
        }
    }

    @Override
    public void onComplete(VoteLocation voteLocation, Calendar dateTime) {
        viewPager.setCurrentItem(1);

        Bundle arguments = new Bundle();
        arguments.putParcelable("voteLocation", Parcels.wrap(voteLocation));
        arguments.putLong("dateTimeMillis", dateTime.getTimeInMillis());

        viewPagerAdapter.getItem(1).setArguments(arguments);
    }

    @Override
    public void onComplete() {
        viewPager.setCurrentItem(2);
    }

    @Override
    public void setupTabLayout(TabLayout tabLayout) {
        this.tabLayout = tabLayout;

        tabLayout.removeAllTabs();
        tabLayout.setVisibility(View.VISIBLE);

        tabLayout.setupWithViewPager(viewPager);
        tabLayout.addOnTabSelectedListener(onTabSelectedListener);
        tabLayout.setEnabled(false);
    }

    @Override
    public void onPause() {
        super.onPause();

        if (tabLayout != null) tabLayout.removeOnTabSelectedListener(onTabSelectedListener);
    }

    private final TabLayout.OnTabSelectedListener onTabSelectedListener = new TabLayout.OnTabSelectedListener() {
        @Override
        public void onTabSelected(TabLayout.Tab tab) { }

        @Override
        public void onTabUnselected(TabLayout.Tab tab) { }

        @Override
        public void onTabReselected(TabLayout.Tab tab) { }
    };
}