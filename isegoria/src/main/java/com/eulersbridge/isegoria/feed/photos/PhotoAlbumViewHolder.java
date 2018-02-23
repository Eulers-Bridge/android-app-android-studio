package com.eulersbridge.isegoria.feed.photos;

import android.support.annotation.ColorRes;
import android.support.annotation.Nullable;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.load.resource.drawable.DrawableTransitionOptions;
import com.eulersbridge.isegoria.GlideApp;
import com.eulersbridge.isegoria.R;
import com.eulersbridge.isegoria.network.api.models.PhotoAlbum;
import com.eulersbridge.isegoria.util.ui.LoadingAdapter;

class PhotoAlbumViewHolder extends LoadingAdapter.ItemViewHolder<PhotoAlbum> {

    interface ClickListener {
        void onClick(PhotoAlbum item);
    }

    private final ClickListener clickListener;

    private PhotoAlbum item;
    final private ImageView imageView;
    final private TextView nameTextView;
    final private TextView descriptionTextView;

    private boolean isImageLoadStarted = false;

    PhotoAlbumViewHolder(View itemView, ClickListener clickListener) {
        super(itemView);

        this.clickListener = clickListener;

        itemView.setOnClickListener(view -> {
            if (this.clickListener != null)
                clickListener.onClick(item);
        });

        imageView = itemView.findViewById(R.id.photo_album_list_item_image_view);

        nameTextView = itemView.findViewById(R.id.photo_album_list_item_title_text_view);
        descriptionTextView = itemView.findViewById(R.id.photo_album_list_item_description_text_view);
    }

    @Override
    protected void onRecycled() {
        if (imageView.getContext() != null && isImageLoadStarted)
            GlideApp.with(imageView.getContext()).clear(imageView);
    }

    @Override
    protected void setItem(@Nullable PhotoAlbum item) {
        this.item = item;

        @ColorRes int placeholderColourRes = R.color.lightGrey;

        if (item == null) {
            imageView.setBackgroundResource(placeholderColourRes);

        } else {
            imageView.setBackgroundResource(placeholderColourRes);
            nameTextView.setText(item.name);
            descriptionTextView.setText(item.description);

            GlideApp.with(imageView.getContext())
                    .load(item.thumbnailPhotoUrl)
                    .placeholder(placeholderColourRes)
                    .transition(DrawableTransitionOptions.withCrossFade())
                    .into(imageView);

            isImageLoadStarted = true;
        }
    }
}