package com.eulersbridge.isegoria.feed;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.eulersbridge.isegoria.MainActivity;
import com.eulersbridge.isegoria.R;
import com.eulersbridge.isegoria.common.Constant;
import com.eulersbridge.isegoria.common.LoadingAdapter;
import com.eulersbridge.isegoria.models.PhotoAlbum;

import org.parceler.Parcels;

import java.lang.ref.WeakReference;

public class PhotoAlbumAdapter extends LoadingAdapter<PhotoAlbum, PhotoAlbumViewHolder> implements PhotoAlbumViewHolder.ClickListener {

    final private WeakReference<Fragment> weakFragment;

    PhotoAlbumAdapter(@NonNull Fragment fragment) {
        super(0);

        weakFragment = new WeakReference<>(fragment);
    }

    private boolean isValidFragment(@Nullable Fragment fragment) {
        return (fragment != null
                && fragment.getActivity() != null
                && !fragment.isDetached()
                && fragment.isAdded());
    }

    @Override
    public PhotoAlbumViewHolder onCreateViewHolder(ViewGroup viewGroup, int viewType) {
        View itemView = LayoutInflater.from(viewGroup.getContext()).
                inflate(R.layout.photo_album_list_item, viewGroup, false);
        return new PhotoAlbumViewHolder(itemView, this);
    }

    @Override
    public void onClick(PhotoAlbum item) {
        Fragment fragment = weakFragment.get();

        if (isValidFragment(fragment)) {
            PhotoAlbumFragment albumFragment = new PhotoAlbumFragment();

            Bundle args = new Bundle();
            args.putParcelable(Constant.FRAGMENT_EXTRA_PHOTO_ALBUM, Parcels.wrap(item));

            albumFragment.setArguments(args);

            MainActivity mainActivity = (MainActivity) fragment.getActivity();
            if (mainActivity != null)
                mainActivity.presentContent(albumFragment);
        }
    }
}