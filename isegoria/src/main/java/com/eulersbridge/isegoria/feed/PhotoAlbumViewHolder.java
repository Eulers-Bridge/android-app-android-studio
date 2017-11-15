package com.eulersbridge.isegoria.feed;

import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.eulersbridge.isegoria.R;
import com.eulersbridge.isegoria.utilities.RecyclerViewItemClickListener;

public class PhotoAlbumViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {

    private @NonNull final RecyclerViewItemClickListener onClickListener;

    final ImageView imageView;
    final TextView nameTextView;
    final TextView descriptionTextView;

    PhotoAlbumViewHolder(View view, @NonNull RecyclerViewItemClickListener onClickListener) {
        super(view);

        this.onClickListener = onClickListener;

        view.setOnClickListener(this);

        imageView = view.findViewById(R.id.photo_album_list_item_image_view);

        nameTextView = view.findViewById(R.id.photo_album_list_item_title_text_view);
        descriptionTextView = view.findViewById(R.id.photo_album_list_item_description_text_view);
    }

    @Override
    public void onClick(View view) {
        onClickListener.onItemClick(this, getAdapterPosition());
    }
}