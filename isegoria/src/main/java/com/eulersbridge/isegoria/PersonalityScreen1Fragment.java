package com.eulersbridge.isegoria;

import android.os.Bundle;
import android.support.v4.app.FragmentManager;
import android.support.v4.view.ViewPager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

import com.actionbarsherlock.app.SherlockFragment;
import com.actionbarsherlock.app.SherlockFragmentActivity;

/**
 * Created by Anthony on 01/04/2015.
 */
public class PersonalityScreen1Fragment extends SherlockFragment {
    private View rootView;

    private ViewPager mPager;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        rootView = inflater.inflate(R.layout.personality_screen1_fragment, container, false);

        FragmentManager fm = ((SherlockFragmentActivity) getActivity()).getSupportFragmentManager();

        final Button takePersonalityButton = (Button) rootView.findViewById(R.id.takePersonalityButton);
        takePersonalityButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mPager.setCurrentItem(1);
            }
        });


        return rootView;
    }

    public void setViewPager(ViewPager mPager) {
        this.mPager = mPager;
    }
}
