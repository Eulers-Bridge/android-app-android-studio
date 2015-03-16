package com.eulersbridge.isegoria;

import com.actionbarsherlock.app.SherlockFragment;

import android.app.ActionBar;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.DisplayMetrics;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;

public class ElectionPositionFragment extends SherlockFragment {
	private View rootView;
	
	private float dpWidth;
	private float dpHeight;
	
	public ElectionPositionFragment() {

	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {   
		rootView = inflater.inflate(R.layout.election_position_fragment, container, false);
		
		DisplayMetrics displayMetrics = getActivity().getResources().getDisplayMetrics();
		dpWidth = displayMetrics.widthPixels / displayMetrics.density;
        dpHeight = displayMetrics.heightPixels / displayMetrics.density;  
		
		return rootView;
	}
}
