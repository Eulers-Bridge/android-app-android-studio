package com.eulersbridge.isegoria.feed;

import android.support.annotation.NonNull;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.eulersbridge.isegoria.R;
import com.eulersbridge.isegoria.utilities.ClickableViewHolder;

public class NewsViewHolder extends ClickableViewHolder {

    final ImageView imageView;
    final TextView titleTextView;
    final TextView dateTextView;

    NewsViewHolder(View view, @NonNull ClickListener onClickListener) {
        super(view, onClickListener);

        imageView = view.findViewById(R.id.news_grid_item_image_view);
        imageView.setOnClickListener(this);

        titleTextView = view.findViewById(R.id.news_grid_item_title_text_view);
        dateTextView = view.findViewById(R.id.news_grid_item_date_text_view);
    }
}