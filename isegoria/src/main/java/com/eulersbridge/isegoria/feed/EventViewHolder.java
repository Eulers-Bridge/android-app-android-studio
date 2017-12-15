package com.eulersbridge.isegoria.feed;

import android.support.annotation.NonNull;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.eulersbridge.isegoria.R;
import com.eulersbridge.isegoria.common.ClickableViewHolder;

class EventViewHolder extends ClickableViewHolder {

    final ImageView imageView;
    final TextView titleTextView;
    final TextView detailsTextView;

    EventViewHolder(View view, @NonNull ClickListener onClickListener) {
        super(view, onClickListener);

        view.setOnClickListener(this);

        imageView = view.findViewById(R.id.event_list_image_view);
        titleTextView = view.findViewById(R.id.event_list_title_text_view);
        detailsTextView = view.findViewById(R.id.event_list_details_text_view);
    }
}