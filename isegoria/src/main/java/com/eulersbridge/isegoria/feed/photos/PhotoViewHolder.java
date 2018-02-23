package com.eulersbridge.isegoria.feed.photos;

import android.content.Context;
import android.support.annotation.Nullable;
import android.view.View;
import android.widget.ImageView;

import com.bumptech.glide.load.resource.drawable.DrawableTransitionOptions;
import com.eulersbridge.isegoria.GlideApp;
import com.eulersbridge.isegoria.R;
import com.eulersbridge.isegoria.network.api.models.Photo;
import com.eulersbridge.isegoria.util.ui.LoadingAdapter;

class PhotoViewHolder extends LoadingAdapter.ItemViewHolder<Photo> {

    interface ClickListener {
        void onClick(Context context, int position);
    }

    private final ClickListener clickListener;
    final private ImageView imageView;

    private boolean isImageLoadStarted = false;

    PhotoViewHolder(View itemView, ClickListener clickListener) {
        super(itemView);

        this.clickListener = clickListener;

        imageView = itemView.findViewById(R.id.photo_grid_item_image_view);
        imageView.setOnClickListener(view -> {
            if (this.clickListener != null)
                this.clickListener.onClick(imageView.getContext(), getAdapterPosition());
        });
    }

    @Override
    protected void onRecycled() {
        if (imageView.getContext() != null && isImageLoadStarted)
            GlideApp.with(imageView.getContext()).clear(imageView);
    }

    @Override
    protected void setItem(@Nullable Photo item) {
        if (item != null) {
            imageView.setContentDescription(item.title);

            GlideApp.with(imageView.getContext())
                    .load(item.thumbnailUrl)
                    .transition(DrawableTransitionOptions.withCrossFade())
                    .into(imageView);

            isImageLoadStarted = true;
        }
    }
}