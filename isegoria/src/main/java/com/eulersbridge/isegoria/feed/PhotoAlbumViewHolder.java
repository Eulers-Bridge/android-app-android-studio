package com.eulersbridge.isegoria.feed;

import android.support.annotation.NonNull;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.eulersbridge.isegoria.R;
import com.eulersbridge.isegoria.utilities.ClickableViewHolder;

public class PhotoAlbumViewHolder extends ClickableViewHolder {

    final ImageView imageView;
    final TextView nameTextView;
    final TextView descriptionTextView;

    PhotoAlbumViewHolder(View view, @NonNull ClickListener onClickListener) {
        super(view, onClickListener);

        view.setOnClickListener(this);

        imageView = view.findViewById(R.id.photo_album_list_item_image_view);

        nameTextView = view.findViewById(R.id.photo_album_list_item_title_text_view);
        descriptionTextView = view.findViewById(R.id.photo_album_list_item_description_text_view);
    }
}