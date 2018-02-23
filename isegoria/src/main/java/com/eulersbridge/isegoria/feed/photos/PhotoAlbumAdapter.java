package com.eulersbridge.isegoria.feed.photos;

import android.support.annotation.NonNull;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.eulersbridge.isegoria.R;
import com.eulersbridge.isegoria.network.api.models.PhotoAlbum;
import com.eulersbridge.isegoria.util.ui.LoadingAdapter;

public class PhotoAlbumAdapter extends LoadingAdapter<PhotoAlbum, PhotoAlbumViewHolder> implements PhotoAlbumViewHolder.ClickListener {

    interface PhotoAlbumClickListener {
        void onClick(PhotoAlbum item);
    }

    private final PhotoAlbumClickListener clickListener;

    PhotoAlbumAdapter(@NonNull PhotoAlbumClickListener clickListener) {
        super(0);

        this.clickListener = clickListener;
    }

    @Override
    public PhotoAlbumViewHolder onCreateViewHolder(ViewGroup viewGroup, int viewType) {
        View itemView = LayoutInflater.from(viewGroup.getContext()).
                inflate(R.layout.photo_album_list_item, viewGroup, false);
        return new PhotoAlbumViewHolder(itemView, this);
    }

    @Override
    public void onClick(PhotoAlbum item) {
        clickListener.onClick(item);
    }
}