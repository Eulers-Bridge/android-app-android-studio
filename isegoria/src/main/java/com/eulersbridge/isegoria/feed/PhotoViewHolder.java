package com.eulersbridge.isegoria.feed;

import android.support.annotation.NonNull;
import android.view.View;
import android.widget.ImageView;

import com.eulersbridge.isegoria.R;
import com.eulersbridge.isegoria.common.ClickableViewHolder;

class PhotoViewHolder extends ClickableViewHolder {

    final ImageView imageView;

    PhotoViewHolder(View view, @NonNull ClickListener onClickListener) {
        super(view, onClickListener);

        imageView = view.findViewById(R.id.photo_grid_item_image_view);
        imageView.setOnClickListener(this);
    }
}