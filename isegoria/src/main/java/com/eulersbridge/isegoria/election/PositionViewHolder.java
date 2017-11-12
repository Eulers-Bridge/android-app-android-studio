package com.eulersbridge.isegoria.election;

import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.eulersbridge.isegoria.R;
import com.eulersbridge.isegoria.utilities.RecyclerViewItemClickListener;

class PositionViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {

    private @NonNull final RecyclerViewItemClickListener onClickListener;

    final ImageView imageView;
    final TextView titleTextView;

    PositionViewHolder(View view, @NonNull RecyclerViewItemClickListener onClickListener) {
        super(view);

        imageView = view.findViewById(R.id.election_position_grid_item_image_view);
        titleTextView = view.findViewById(R.id.election_position_grid_item_title_text_view);

        this.onClickListener = onClickListener;

        view.setOnClickListener(this);
    }

    @Override
    public void onClick(View view) {
        onClickListener.onItemClick(view, getAdapterPosition());
    }
}