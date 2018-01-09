package com.eulersbridge.isegoria.feed;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.eulersbridge.isegoria.R;
import com.eulersbridge.isegoria.common.Constant;
import com.eulersbridge.isegoria.common.LoadingAdapter;
import com.eulersbridge.isegoria.models.Photo;

import org.parceler.Parcels;

import java.lang.ref.WeakReference;

class PhotoAdapter extends LoadingAdapter<Photo, PhotoViewHolder> implements PhotoViewHolder.ClickListener {

    PhotoAdapter() {
        super(0);
    }

    @Override
    public void onClick(Context context, int position, WeakReference<View> weakTransitionView) {
        Intent activityIntent = new Intent(context, PhotoDetailActivity.class);
        activityIntent.putExtra(Constant.ACTIVITY_EXTRA_PHOTOS, Parcels.wrap(getItems()));
        activityIntent.putExtra(Constant.ACTIVITY_EXTRA_PHOTOS_POSITION, position);

        context.startActivity(activityIntent);
    }

    @Override
    public PhotoViewHolder onCreateViewHolder(ViewGroup viewGroup, int viewType) {
        View itemView = LayoutInflater.from(viewGroup.getContext()).
                inflate(R.layout.photo_grid_item, viewGroup, false);
        return new PhotoViewHolder(itemView, this);
    }
}