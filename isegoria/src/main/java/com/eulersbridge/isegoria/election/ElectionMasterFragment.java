package com.eulersbridge.isegoria.election;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.TabLayout;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

import com.eulersbridge.isegoria.MainActivity;
import com.eulersbridge.isegoria.R;
import com.eulersbridge.isegoria.utilities.TitledFragment;

public class ElectionMasterFragment extends Fragment implements TitledFragment {

	private ElectionOverviewFragment overviewFragment;
	private CandidateFragment candidateFragment;

    private MainActivity mainActivity;
    private View rootView;
	private TabLayout tabLayout;
	
	@Override
	public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		rootView = inflater.inflate(R.layout.election_master_layout, container, false);

		mainActivity = (MainActivity)getActivity();

		// Ensure options menu from another fragment is not carried over
		mainActivity.invalidateOptionsMenu();

        setupTabLayout();

		overviewFragment = new ElectionOverviewFragment();
		candidateFragment = new CandidateFragment();

        showFirstTab();

		return rootView;
	}

	@Override
	public String getTitle() {
		return getString(R.string.section_title_election);
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
        mainActivity.getSupportFragmentManager()
                .beginTransaction()
                .replace(R.id.election_frame, fragment)
                .commitAllowingStateLoss();

        boolean userCompletedEfficacyQuestions = mainActivity.getIsegoriaApplication()
                .getLoggedInUser().hasPPSEQuestions;

        if (!userCompletedEfficacyQuestions) {

            View overlay = rootView.findViewById(R.id.election_efficacy_overlay);
            overlay.setVisibility(View.VISIBLE);

            Button efficacyStartButton = overlay.findViewById(R.id.election_efficacy_overlay_start);
            efficacyStartButton.setOnClickListener(view -> {
                SelfEfficacyQuestionsFragment selfEfficacyQuestionsFragment = new SelfEfficacyQuestionsFragment();

                mainActivity.getSupportFragmentManager()
                        .beginTransaction()
                        .addToBackStack(null)
                        .add(R.id.container, selfEfficacyQuestionsFragment)
                        .commit();
            });
        }
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
