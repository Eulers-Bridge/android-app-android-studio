package com.eulersbridge.isegoria.election;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.TabLayout;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.eulersbridge.isegoria.MainActivity;
import com.eulersbridge.isegoria.R;

public class ElectionMasterFragment extends Fragment {

	private ElectionOverviewFragment overviewFragment;
	private CandidateFragment candidateFragment;

	private TabLayout tabLayout;
	
	@Override
	public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		final View rootView = inflater.inflate(R.layout.election_master_layout, container, false);

		((MainActivity)getActivity()).setToolbarTitle(getString(R.string.section_title_election));

		overviewFragment = new ElectionOverviewFragment();
		candidateFragment = new CandidateFragment();

		setupTabLayout();
		showFirstTab();

		return rootView;
	}

	public void setTabLayout(TabLayout tabLayout) {
		this.tabLayout = tabLayout;
	}

	@Override
	public void onPause() {
		super.onPause();

		if (tabLayout != null) tabLayout.removeOnTabSelectedListener(onTabSelectedListener);
	}

	private void showFirstTab() {
		showTabFragment(overviewFragment);
	}

	private void showTabFragment(@NonNull Fragment fragment) {
		getActivity().getSupportFragmentManager()
				.beginTransaction()
				.replace(R.id.election_frame, fragment)
				.commitAllowingStateLoss();
	}

	private final TabLayout.OnTabSelectedListener onTabSelectedListener = new TabLayout.OnTabSelectedListener() {
		@Override
		public void onTabSelected(TabLayout.Tab tab) {
			Fragment subFragment = null;

			if (tab.getPosition() == 0) {
				subFragment = overviewFragment;

			} else if (tab.getPosition() == 1) {
				subFragment = candidateFragment;
			}

			if (subFragment != null && getActivity() != null)
				showTabFragment(subFragment);
		}

		@Override
		public void onTabUnselected(TabLayout.Tab tab) { }

		@Override
		public void onTabReselected(TabLayout.Tab tab) { }
	};

	private void setupTabLayout() {
		if (tabLayout == null) return;

		tabLayout.removeAllTabs();

		final String[] tabNames = {"Overview", "Candidates"};

		for (String tabName : tabNames) {
			tabLayout.addTab(tabLayout.newTab().setText(tabName));
		}

		tabLayout.addOnTabSelectedListener(onTabSelectedListener);
		tabLayout.setVisibility(View.VISIBLE);
	}
}
