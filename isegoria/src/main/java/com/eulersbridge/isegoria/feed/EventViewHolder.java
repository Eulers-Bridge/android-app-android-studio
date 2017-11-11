package com.eulersbridge.isegoria.feed;

import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.eulersbridge.isegoria.R;
import com.eulersbridge.isegoria.utilities.RecyclerViewItemClickListener;

class EventViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {

    private @NonNull final RecyclerViewItemClickListener onClickListener;

    final ImageView imageView;
    final TextView titleTextView;
    final TextView detailsTextView;

    EventViewHolder(View view, @NonNull RecyclerViewItemClickListener onClickListener) {
        super(view);

        imageView = view.findViewById(R.id.event_list_image_view);
        titleTextView = view.findViewById(R.id.event_list_title_text_view);
        detailsTextView = view.findViewById(R.id.event_list_details_text_view);

        this.onClickListener = onClickListener;

        view.setOnClickListener(this);
    }

    @Override
    public void onClick(View view) {
        onClickListener.onItemClick(view, getAdapterPosition());
    }
}