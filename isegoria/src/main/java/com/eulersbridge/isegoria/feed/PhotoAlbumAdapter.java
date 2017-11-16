package com.eulersbridge.isegoria.feed;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.bumptech.glide.load.resource.drawable.DrawableTransitionOptions;
import com.eulersbridge.isegoria.Constant;
import com.eulersbridge.isegoria.GlideApp;
import com.eulersbridge.isegoria.R;
import com.eulersbridge.isegoria.models.PhotoAlbum;
import com.eulersbridge.isegoria.utilities.ClickableViewHolder;

import java.util.ArrayList;
import java.util.List;

public class PhotoAlbumAdapter extends RecyclerView.Adapter<PhotoAlbumViewHolder> implements ClickableViewHolder.ClickListener {
    final private Fragment fragment;
    final private List<PhotoAlbum> items = new ArrayList<>();

    PhotoAlbumAdapter(Fragment fragment) {
        this.fragment = fragment;
    }

    void replaceItems(@NonNull List<PhotoAlbum> newItems) {
        items.clear();
        items.addAll(newItems);
    }

    @Override
    public int getItemCount() { return items.size(); }

    @Override
    public void onBindViewHolder(PhotoAlbumViewHolder viewHolder, int index) {
        PhotoAlbum item = items.get(index);

        viewHolder.imageView.setBackgroundResource(R.color.grey);
        viewHolder.nameTextView.setText(item.name);
        viewHolder.descriptionTextView.setText(item.description);

        GlideApp.with(fragment)
                .load(item.thumbnailPhotoUrl)
                .placeholder(R.color.grey)
                .centerCrop()
                .transition(DrawableTransitionOptions.withCrossFade())
                .into(viewHolder.imageView);
    }

    private boolean isValidFragment() {
        return (fragment != null
                && fragment.getActivity() != null
                && !fragment.isDetached()
                && fragment.isAdded());
    }

    @Override
    public void onItemClick(RecyclerView.ViewHolder viewHolder, int position) {
        if (isValidFragment()) {
            PhotoAlbum item = items.get(position);

            PhotoAlbumFragment albumFragment = new PhotoAlbumFragment();
            Bundle args = new Bundle();
            args.putLong(Constant.FRAGMENT_EXTRA_PHOTO_ALBUM_ID, item.id);
            albumFragment.setArguments(args);

            fragment.getActivity().getSupportFragmentManager()
                    .beginTransaction()
                    .addToBackStack(null)
                    .add(R.id.photos_frame_layout, albumFragment)
                    .commit();
        }
    }

    @Override
    public PhotoAlbumViewHolder onCreateViewHolder(ViewGroup viewGroup, int viewType) {
        View itemView = LayoutInflater.from(viewGroup.getContext()).
                inflate(R.layout.photo_album_list_item, viewGroup, false);
        return new PhotoAlbumViewHolder(itemView, this);
    }
}