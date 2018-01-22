package com.eulersbridge.isegoria.feed.photos;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.eulersbridge.isegoria.R;
import com.eulersbridge.isegoria.network.api.models.Photo;
import com.eulersbridge.isegoria.util.Constants;
import com.eulersbridge.isegoria.util.ui.LoadingAdapter;

import org.parceler.Parcels;

class PhotoAdapter extends LoadingAdapter<Photo, PhotoViewHolder> implements PhotoViewHolder.ClickListener {

    PhotoAdapter() {
        super(0);
    }

    @Override
    public void onClick(Context context, int position) {
        Intent activityIntent = new Intent(context, PhotoDetailActivity.class);
        activityIntent.putExtra(Constants.ACTIVITY_EXTRA_PHOTOS, Parcels.wrap(getItems()));
        activityIntent.putExtra(Constants.ACTIVITY_EXTRA_PHOTOS_POSITION, position);

        context.startActivity(activityIntent);
    }

    @Override
    public PhotoViewHolder onCreateViewHolder(ViewGroup viewGroup, int viewType) {
        View itemView = LayoutInflater.from(viewGroup.getContext()).
                inflate(R.layout.photo_grid_item, viewGroup, false);
        return new PhotoViewHolder(itemView, this);
    }
}