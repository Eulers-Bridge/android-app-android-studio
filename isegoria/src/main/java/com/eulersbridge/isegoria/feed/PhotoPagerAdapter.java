package com.eulersbridge.isegoria.feed;

import android.os.Bundle;
import android.os.Parcelable;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;

import com.eulersbridge.isegoria.models.Photo;

import org.parceler.Parcels;

class PhotoPagerAdapter extends FragmentStatePagerAdapter {

    private final PhotoAdapter photoAdapter;

    PhotoPagerAdapter(FragmentManager fm, PhotoAdapter photoAdapter) {
        super(fm);
        this.photoAdapter = photoAdapter;
    }

    @Override
    public Fragment getItem(int position) {

        Photo photo = photoAdapter.getItem(position);
        PhotoViewFragment viewFragment = new PhotoViewFragment();

        Bundle args = new Bundle();
        args.putParcelable("photo", Parcels.wrap(photo));
        viewFragment.setArguments(args);

        return viewFragment;
    }

    @Override
    public void restoreState(Parcelable state, ClassLoader loader) {
        // Intentionally empty to avoid restoring fragment state
    }

    @Override
    public int getCount() {
        return photoAdapter.getItemCount();
    }
}
