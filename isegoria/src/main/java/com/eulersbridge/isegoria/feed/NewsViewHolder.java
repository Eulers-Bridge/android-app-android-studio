package com.eulersbridge.isegoria.feed;

import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.eulersbridge.isegoria.R;
import com.eulersbridge.isegoria.utilities.RecyclerViewItemClickListener;

public class NewsViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {

    private @NonNull
    final RecyclerViewItemClickListener onClickListener;

    final ImageView imageView;
    final TextView titleTextView;
    final TextView dateTextView;

    NewsViewHolder(View view, @NonNull RecyclerViewItemClickListener onClickListener) {
        super(view);

        this.onClickListener = onClickListener;

        imageView = view.findViewById(R.id.news_grid_item_image_view);
        imageView.setOnClickListener(this);

        titleTextView = view.findViewById(R.id.news_grid_item_title_text_view);
        dateTextView = view.findViewById(R.id.news_grid_item_date_text_view);
    }

    @Override
    public void onClick(View view) {
        onClickListener.onItemClick(this, getAdapterPosition());
    }
}