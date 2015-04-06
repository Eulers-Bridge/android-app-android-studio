package com.eulersbridge.isegoria;

import android.app.ActionBar;
import android.os.Bundle;
import android.util.DisplayMetrics;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.actionbarsherlock.app.SherlockFragment;
import com.actionbarsherlock.app.SherlockFragmentActivity;

/**
 * Created by Anthony on 06/04/2015.
 */
public class AboutScreenFragment extends SherlockFragment {
    private View rootView;

    private float dpWidth;
    private float dpHeight;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        rootView = inflater.inflate(R.layout.about_screen_fragment, container, false);
        ((SherlockFragmentActivity) getActivity()).getSupportActionBar().setNavigationMode(ActionBar.NAVIGATION_MODE_STANDARD);
        getActivity().getActionBar().removeAllTabs();

        DisplayMetrics displayMetrics = getActivity().getResources().getDisplayMetrics();
        dpWidth = displayMetrics.widthPixels / displayMetrics.density;
        dpHeight = displayMetrics.heightPixels / displayMetrics.density;

        return rootView;
    }
}
