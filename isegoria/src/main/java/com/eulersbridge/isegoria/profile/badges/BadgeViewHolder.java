package com.eulersbridge.isegoria.profile.badges;

import android.graphics.Color;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.RequestManager;
import com.bumptech.glide.load.resource.drawable.DrawableTransitionOptions;
import com.eulersbridge.isegoria.R;
import com.eulersbridge.isegoria.network.api.models.Badge;

class BadgeViewHolder extends RecyclerView.ViewHolder {

    private Badge item;

    final private ImageView imageView;
    final private TextView nameTextView;
    final private TextView descriptionTextView;

    private @Nullable RequestManager glide;

    BadgeViewHolder(View view) {
        super(view);

        imageView = view.findViewById(R.id.badge_list_image_view);
        nameTextView = view.findViewById(R.id.badge_list_name_text_view);
        descriptionTextView = view.findViewById(R.id.badge_list_description_text_view);
    }

    void setItem(@Nullable Badge item, boolean completed) {
        this.item = item;

        if (item == null) {
            nameTextView.setText(null);
            descriptionTextView.setText(null);
            imageView.setImageDrawable(null);

        } else {
            if (!completed) {
                imageView.setColorFilter(Color.argb(125, 35, 35, 35));
            } else {
                imageView.clearColorFilter();
            }

            nameTextView.setText(item.name);
            descriptionTextView.setText(item.description);
            imageView.setImageDrawable(null);
        }
    }

    void setImageUrl(@NonNull RequestManager glide, long itemId, @NonNull String imageUrl) {
        if (item != null && item.id == itemId) {

            this.glide = glide;

            glide.load(imageUrl)
                    .transition(DrawableTransitionOptions.withCrossFade())
                    .into(imageView);
        }
    }

    void onRecycled() {
        if (glide != null)
            glide.clear(imageView);
    }
}
