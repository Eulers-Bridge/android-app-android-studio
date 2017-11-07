package com.eulersbridge.isegoria.feed;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.view.ViewPager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.eulersbridge.isegoria.R;

import java.util.ArrayList;

public class PhotoViewPagerFragment extends Fragment {

    private final ArrayList<Fragment> fragmentList;
    private int position;

    public PhotoViewPagerFragment() {
        fragmentList = new ArrayList<>();
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.photo_view_pager_fragment, container, false);

        FragmentManager fm = getActivity().getSupportFragmentManager();

        ViewPager mPager = rootView.findViewById(R.id.photoViewPagerFragment);

        PhotoPagerAdapter mPagerAdapter = new PhotoPagerAdapter(fm, fragmentList);
        mPager.setAdapter(mPagerAdapter);

        mPager.setCurrentItem(this.position);

        return rootView;
    }

    public int addFragment(Fragment fragment) {
        fragmentList.add(fragment);
        return fragmentList.size()-1;
    }

    public void setPosition(int pos) {
        this.position = pos;
    }
}