package com.eulersbridge.isegoria.election.candidates;

import android.app.Activity;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import com.eulersbridge.isegoria.R;
import com.eulersbridge.isegoria.election.candidates.positions.CandidatePositionsFragment;
import com.viewpagerindicator.TabPageIndicator;

import java.util.List;
import java.util.Vector;

public class CandidateFragment extends Fragment {

	@Override
	public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		final View rootView = inflater.inflate(R.layout.candidate_fragment, container, false);

		final ViewPager viewPager = rootView.findViewById(R.id.candidateViewPager);

		final Activity activity = getActivity();

        final List<Fragment> fragments = new Vector<>();
        fragments.add(Fragment.instantiate(activity, CandidatePositionsFragment.class.getName()));
        fragments.add(Fragment.instantiate(activity, CandidateTicketFragment.class.getName()));
        fragments.add(Fragment.instantiate(activity, CandidateAllFragment.class.getName()));

        viewPager.setAdapter(new FragmentPagerAdapter(getChildFragmentManager()) {
			@Override
			public Fragment getItem(int position) {
				return fragments.get(position);
			}

			@Override
			public CharSequence getPageTitle(int position) {
				switch(position) {
					case 0:
						return "Position";
					case 1:
						return "Ticket";
					case 2:
						return "All";
					default:
						return "";
				}
			}

			@Override
			public int getCount() {
				return fragments.size();
			}
		});
		
		final TabPageIndicator tabPageIndicator = rootView.findViewById(R.id.tabPageIndicatorCandidate);
		tabPageIndicator.setViewPager(viewPager);
		tabPageIndicator.setBackgroundResource(R.color.barBackground);

		return rootView;
	}
}