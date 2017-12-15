package com.eulersbridge.isegoria.feed;

import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.bumptech.glide.load.resource.drawable.DrawableTransitionOptions;
import com.eulersbridge.isegoria.common.Constant;
import com.eulersbridge.isegoria.GlideApp;
import com.eulersbridge.isegoria.R;
import com.eulersbridge.isegoria.models.Photo;
import com.eulersbridge.isegoria.common.ClickableViewHolder;

import org.parceler.Parcels;

import java.util.ArrayList;
import java.util.List;

class PhotoAdapter extends RecyclerView.Adapter<PhotoViewHolder> implements ClickableViewHolder.ClickListener {
    final private Fragment fragment;
    final private List<Photo> items = new ArrayList<>();

    PhotoAdapter(Fragment fragment) {
        this.fragment = fragment;
    }

    void replaceItems(@NonNull List<Photo> newItems) {
        items.clear();
        items.addAll(newItems);
    }

    @Override
    public int getItemCount() { return items.size(); }

    @Override
    public void onBindViewHolder(PhotoViewHolder viewHolder, int index) {
        final Photo item = items.get(index);

        viewHolder.imageView.setBackgroundResource(R.color.grey);
        viewHolder.imageView.setContentDescription(item.title);

        GlideApp.with(fragment)
                .load(item.thumbnailUrl)
                .placeholder(R.color.grey)
                .centerCrop()
                .transition(DrawableTransitionOptions.withCrossFade())
                .into(viewHolder.imageView);
    }

    @Override
    public void onItemClick(RecyclerView.ViewHolder viewHolder, int position) {

        Intent activityIntent = new Intent(fragment.getContext(), PhotoDetailActivity.class);
        activityIntent.putExtra(Constant.ACTIVITY_EXTRA_PHOTOS, Parcels.wrap(items));
        activityIntent.putExtra(Constant.ACTIVITY_EXTRA_PHOTOS_POSITION, position);

        fragment.startActivity(activityIntent);
    }

    @Override
    public PhotoViewHolder onCreateViewHolder(ViewGroup viewGroup, int viewType) {
        View itemView = LayoutInflater.from(viewGroup.getContext()).
                inflate(R.layout.photo_grid_item, viewGroup, false);
        return new PhotoViewHolder(itemView, this);
    }
}