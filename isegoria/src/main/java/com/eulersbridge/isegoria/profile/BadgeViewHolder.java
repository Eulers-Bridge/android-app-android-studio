package com.eulersbridge.isegoria.profile;

import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.eulersbridge.isegoria.R;

class BadgeViewHolder extends RecyclerView.ViewHolder {

    final ImageView imageView;
    final TextView nameTextView;
    final TextView descriptionTextView;

    BadgeViewHolder(View view) {
        super(view);

        imageView = view.findViewById(R.id.badge_list_image_view);
        nameTextView = view.findViewById(R.id.badge_list_name_text_view);
        descriptionTextView = view.findViewById(R.id.badge_list_xp_text_view);
    }
}
