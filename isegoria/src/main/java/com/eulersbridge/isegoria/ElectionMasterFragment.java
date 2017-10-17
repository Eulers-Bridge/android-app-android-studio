package com.eulersbridge.isegoria;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.TabLayout;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

public class ElectionMasterFragment extends Fragment {

	private ElectionFragment electionFragment;
	private CandidateFragment candidateFragment;

	private TabLayout tabLayout;
	
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {  
		final View rootView = inflater.inflate(R.layout.election_master_layout, container, false);

		electionFragment = new ElectionFragment();
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
		showTabFragment(electionFragment);
	}

	private void showTabFragment(@NonNull Fragment fragment) {
		getActivity().getSupportFragmentManager().beginTransaction().replace(R.id.content_election_frame1, fragment).commitAllowingStateLoss();
	}

	private final TabLayout.OnTabSelectedListener onTabSelectedListener = new TabLayout.OnTabSelectedListener() {
		@Override
		public void onTabSelected(TabLayout.Tab tab) {
			Fragment subFragment = null;

			if (tab.getText().equals("Election")) {
				subFragment = electionFragment;

			} else if (tab.getText().equals("Candidates")) {
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

		final String[] tabNames = {"Election", "Candidates"};

		for (String tabName : tabNames) {
			tabLayout.addTab(tabLayout.newTab().setText(tabName));
		}

		tabLayout.addOnTabSelectedListener(onTabSelectedListener);

		tabLayout.setVisibility(View.VISIBLE);
	}
}
