package com.eulersbridge.isegoria;


import com.actionbarsherlock.app.SherlockFragment;

import android.os.Bundle;

import android.support.v4.app.Fragment;
import android.util.DisplayMetrics;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TableLayout;

public class ElectionOverviewFragment extends SherlockFragment {
	private View rootView;
	
	private float dpWidth;
	private float dpHeight;
	
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {   
		rootView = inflater.inflate(R.layout.election_overview_fragment, container, false);
		DisplayMetrics displayMetrics = getActivity().getResources().getDisplayMetrics();

		dpWidth = displayMetrics.widthPixels / displayMetrics.density;
        dpHeight = displayMetrics.heightPixels / displayMetrics.density;  
		
		return rootView;
	}
}
