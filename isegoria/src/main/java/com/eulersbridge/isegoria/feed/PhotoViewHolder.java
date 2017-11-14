package com.eulersbridge.isegoria.feed;

import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.ImageView;

import com.eulersbridge.isegoria.R;
import com.eulersbridge.isegoria.utilities.RecyclerViewItemClickListener;

class PhotoViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {

    private @NonNull final RecyclerViewItemClickListener onClickListener;

    final ImageView imageView;

    PhotoViewHolder(View view, @NonNull RecyclerViewItemClickListener onClickListener) {
        super(view);

        this.onClickListener = onClickListener;

        imageView = view.findViewById(R.id.photo_grid_item_image_view);
        imageView.setOnClickListener(this);
    }

    @Override
    public void onClick(View view) {
        onClickListener.onItemClick(this, getAdapterPosition());
    }
}