package com.eulersbridge.isegoria;

import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.actionbarsherlock.app.ActionBar;
import com.actionbarsherlock.app.ActionBar.Tab;
import com.actionbarsherlock.app.ActionBar.TabListener;
import com.actionbarsherlock.app.SherlockFragment;
import com.actionbarsherlock.app.SherlockFragmentActivity;

public class ElectionMasterFragment extends SherlockFragment implements TabListener {
	private View rootView;
	private FragmentManager fragmentManager = null;
	private ViewGroup container = null;
	
	private ElectionFragment electionFragment;
	private CandidateFragment candidateFragment;
	
	public ElectionMasterFragment() {

	}
	
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {  
		rootView = inflater.inflate(R.layout.election_master_layout, container, false);
		
		electionFragment = new ElectionFragment();
		candidateFragment = new CandidateFragment();
		
		((SherlockFragmentActivity) getActivity()).getSupportActionBar().setNavigationMode(ActionBar.NAVIGATION_MODE_TABS);
		((SherlockFragmentActivity) getActivity()).getSupportActionBar().show();
		
		((SherlockFragmentActivity) getActivity()).getSupportActionBar().removeAllTabs();
		((SherlockFragmentActivity) getActivity()).getSupportActionBar().addTab(
				((SherlockFragmentActivity) getActivity()).getSupportActionBar().newTab()
	            .setText("Election")
	            .setTabListener(this));
		((SherlockFragmentActivity) getActivity()).getSupportActionBar().addTab(
				((SherlockFragmentActivity) getActivity()).getSupportActionBar().newTab()
	            .setText("Candidates")
	            .setTabListener(this));
	    
		ActionBar bar = ((SherlockFragmentActivity) getActivity()).getSupportActionBar();
		bar.setBackgroundDrawable(new ColorDrawable(Color.parseColor("#3C7EC9")));
		bar.setStackedBackgroundDrawable(new ColorDrawable(Color.parseColor("#313E4D")));
		bar.setSplitBackgroundDrawable(new ColorDrawable(Color.parseColor("#313E4D")));

		return rootView;
	}
	
    @Override
    public void onTabSelected(Tab tab, FragmentTransaction ft) {
    	try {
    		if(tab.getText().equals("Election")) {
    			//electionFragment.mViewPager.setCurrentItem(2);
    			((SherlockFragmentActivity) getActivity()).getSupportFragmentManager().popBackStack();
    			((SherlockFragmentActivity) getActivity()).getSupportFragmentManager().beginTransaction().replace(R.id.content_election_frame1, new ElectionOverviewFragment()).commitAllowingStateLoss();
    		}
    		else if(tab.getText().equals("Candidates")) {
    			//ft.replace(R.id.content_election_frame1, candidateFragment);
    			((SherlockFragmentActivity) getActivity()).getSupportFragmentManager().popBackStack();
    			((SherlockFragmentActivity) getActivity()).getSupportFragmentManager().beginTransaction().replace(R.id.content_election_frame1, new CandidateFragment()).commitAllowingStateLoss();
    		}
    	} catch(Exception e) {
    		
    	}
    }
	
    @Override
    public void onTabReselected(Tab tab, FragmentTransaction ft) {
    }

    @Override
    public void onTabUnselected(Tab tab, FragmentTransaction ft) {
    	//try {
	    //	if(tab.getText().equals("Election")) {
	    //		((SherlockFragmentActivity) getActivity()).getSupportFragmentManager().beginTransaction().remove(electionFragment).commit();
	    //	}
	    	//else if(tab.getText().equals("Candidates")) {
	    	//	((SherlockFragmentActivity) getActivity()).getSupportFragmentManager().beginTransaction().remove(candidateFragment).commit();
	    	//}
    //	} catch(Exception e) {
    		
    	//}
    }
}
