package com.eulersbridge.isegoria;

import android.os.Bundle;
import android.support.v4.app.FragmentManager;
import android.support.v4.view.ViewPager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.actionbarsherlock.app.ActionBar;
import com.actionbarsherlock.app.SherlockFragment;
import com.actionbarsherlock.app.SherlockFragmentActivity;

import java.util.ArrayList;

public class PhotoViewPagerFragment extends SherlockFragment  {
    private View rootView;
    private ViewPager mPager;
    private PhotoPagerAdapter mPagerAdapter;

    private ArrayList<SherlockFragment> fragmentList;
    private int position;

    public PhotoViewPagerFragment() {
        fragmentList = new ArrayList<SherlockFragment>();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        rootView = inflater.inflate(R.layout.photo_view_pager_fragment, container, false);
        ((SherlockFragmentActivity) getActivity()).getSupportActionBar().setNavigationMode(ActionBar.NAVIGATION_MODE_TABS);

        FragmentManager fm = ((SherlockFragmentActivity) getActivity()).getSupportFragmentManager();

        ViewPager.SimpleOnPageChangeListener ViewPagerListener = new ViewPager.SimpleOnPageChangeListener() {
            @Override
            public void onPageSelected(int position) {
                super.onPageSelected(position);
            }
        };

        mPager = (ViewPager) rootView.findViewById(R.id.photoViewPagerFragment);
        mPager.setOnPageChangeListener(ViewPagerListener);

        mPagerAdapter = new PhotoPagerAdapter(fm, fragmentList);
        mPager.setAdapter(mPagerAdapter);

        mPager.setCurrentItem(this.position);

        return rootView;
    }

    public int addFragment(SherlockFragment fragment) {
        fragmentList.add(fragment);
        return fragmentList.size()-1;
    }

    public void setPosition(int pos) {
        this.position = pos;
    }
}