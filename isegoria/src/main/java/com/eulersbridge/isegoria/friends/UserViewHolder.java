package com.eulersbridge.isegoria.friends;

import android.support.annotation.DrawableRes;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.load.resource.drawable.DrawableTransitionOptions;
import com.eulersbridge.isegoria.GlideApp;
import com.eulersbridge.isegoria.R;
import com.eulersbridge.isegoria.network.api.models.GenericUser;
import com.eulersbridge.isegoria.network.api.models.Institution;

class UserViewHolder extends RecyclerView.ViewHolder {

    interface OnClickListener {
        void onViewClick(@Nullable GenericUser user);
        void onActionClick(@Nullable GenericUser user);
    }

    private @Nullable GenericUser item;
    final private @Nullable OnClickListener clickListener;

    final private ImageView imageView;
    final private TextView nameTextView;
    final private TextView institutionTextView;

    UserViewHolder(View itemView, @DrawableRes int actionImageDrawableRes, @Nullable OnClickListener clickListener) {
        super(itemView);

        this.clickListener = clickListener;

        itemView.setOnClickListener(view -> {
            if (this.clickListener != null)
                this.clickListener.onViewClick(item);
        });

        imageView = itemView.findViewById(R.id.friends_list_image_view);
        nameTextView = itemView.findViewById(R.id.friends_list_name_text_view);
        institutionTextView = itemView.findViewById(R.id.friends_list_institution_text_view);

        ImageView actionImageView = itemView.findViewById(R.id.friends_list_action_image_view);
        actionImageView.setImageResource(actionImageDrawableRes);
        actionImageView.setOnClickListener(view -> {
            if (this.clickListener != null)
                this.clickListener.onActionClick(item);
        });
    }

    void setItem(@Nullable GenericUser item) {
        this.item = item;

        if (item == null) {
            nameTextView.setText(null);
            institutionTextView.setText(null);

        } else {
            nameTextView.setText(item.getFullName());

            GlideApp.with(imageView.getContext())
                    .load(item.profilePhotoURL)
                    .placeholder(R.color.white)
                    .transition(DrawableTransitionOptions.withCrossFade())
                    .into(imageView);
        }
    }

    void setInstitution(@NonNull Institution institution) {
        if (item != null && item.institutionId != null && institution.id == item.institutionId)
            institutionTextView.setText(institution.getName());
    }
}
