package com.eulersbridge.isegoria.election;

import android.support.annotation.NonNull;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.eulersbridge.isegoria.R;
import com.eulersbridge.isegoria.common.ClickableViewHolder;

class PositionViewHolder extends ClickableViewHolder {

    final ImageView imageView;
    final TextView titleTextView;

    PositionViewHolder(View view, @NonNull ClickListener onClickListener) {
        super(view, onClickListener);

        view.setOnClickListener(this);

        imageView = view.findViewById(R.id.election_position_grid_item_image_view);
        titleTextView = view.findViewById(R.id.election_position_grid_item_title_text_view);
    }
}