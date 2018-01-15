package com.eulersbridge.isegoria.friends;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.load.resource.drawable.DrawableTransitionOptions;
import com.eulersbridge.isegoria.GlideApp;
import com.eulersbridge.isegoria.R;
import com.eulersbridge.isegoria.network.api.models.FriendRequest;
import com.eulersbridge.isegoria.network.api.models.Institution;
import com.eulersbridge.isegoria.network.api.models.User;

import java.lang.ref.WeakReference;

class SentFriendRequestViewHolder extends RecyclerView.ViewHolder {

    private FriendRequest item;
    private @NonNull
    final
    ViewHolderDataSource dataSource;

    final private ImageView imageView;
    final private TextView nameTextView;
    final private TextView institutionTextView;

    SentFriendRequestViewHolder(View itemView, @NonNull ViewHolderDataSource dataSource) {
        super(itemView);

        this.dataSource = dataSource;

        // TODO: On click listener to view profile

        imageView = itemView.findViewById(R.id.friends_list_image_view);
        nameTextView = itemView.findViewById(R.id.friends_list_name_text_view);
        institutionTextView = itemView.findViewById(R.id.friends_list_institution_text_view);

        ImageView actionImageView = itemView.findViewById(R.id.friends_list_action_image_view);
        actionImageView.setVisibility(View.GONE);
    }

    void setItem(@Nullable FriendRequest item) {
        this.item = item;

        if (item == null) {
            nameTextView.setText(null);
            institutionTextView.setText(null);

        } else {
            User user = item.requestReceiver;

            nameTextView.setText(user.getFullName());
            dataSource.getFriendRequestInstitution(user.institutionId, new WeakReference<>(this));

            GlideApp.with(imageView.getContext())
                    .load(user.profilePhotoURL)
                    .placeholder(R.color.white)
                    .transition(DrawableTransitionOptions.withCrossFade())
                    .into(imageView);
        }
    }

    void setInstitution(@NonNull Institution institution) {
        if (item != null && item.requester.institutionId != null && institution.id == item.requester.institutionId)
            institutionTextView.setText(institution.getName());
    }
}
