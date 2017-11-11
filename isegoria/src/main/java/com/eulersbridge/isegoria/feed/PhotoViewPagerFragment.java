package com.eulersbridge.isegoria.feed;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
import android.support.v4.view.ViewPager;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.eulersbridge.isegoria.R;

public class PhotoViewPagerFragment extends Fragment {

    private ViewPager pager;
    private boolean pagerSetup = false;

    private Fragment parentFragment;
    private PhotoAdapter photoAdapter;

    private int position = 0;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.photo_view_pager_fragment, container, false);

        pager = rootView.findViewById(R.id.photoViewPagerFragment);

        setupPager();

        return rootView;
    }

    public void setPhotoAdapter(Fragment parentFragment, PhotoAdapter photoAdapter) {
        this.parentFragment = parentFragment;
        this.photoAdapter = photoAdapter;

        setupPager();
    }

    private void setupPager() {
        if (pager == null || parentFragment == null || photoAdapter == null || pagerSetup) return;

        int marginDp = 8;
        float marginPixels = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, marginDp, getResources().getDisplayMetrics());
        pager.setPageMargin((int)marginPixels);

        pager.setOffscreenPageLimit(2);

        PhotoPagerAdapter pagerAdapter = new PhotoPagerAdapter(getChildFragmentManager(), photoAdapter);
        pager.setAdapter(pagerAdapter);

        pager.setCurrentItem(position);

        pagerSetup = false;
    }

    public void setPosition(int position) {
        this.position = position;

        if (pager != null) {
            pager.setCurrentItem(position);
        }
    }
}