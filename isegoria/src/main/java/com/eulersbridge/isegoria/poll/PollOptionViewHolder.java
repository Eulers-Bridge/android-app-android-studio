package com.eulersbridge.isegoria.poll;

import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.eulersbridge.isegoria.R;
import com.eulersbridge.isegoria.utilities.RecyclerViewItemClickListener;

public class PollOptionViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {

    private @NonNull
    final RecyclerViewItemClickListener onClickListener;

    final ImageView imageView;
    final ImageView checkBoxImageView;
    final ProgressBar progressBar;
    final TextView textTextView;

    PollOptionViewHolder(View view, @NonNull RecyclerViewItemClickListener onClickListener) {
        super(view);

        imageView = view.findViewById(R.id.poll_vote_option_list_item_image_view);
        checkBoxImageView = view.findViewById(R.id.poll_vote_option_list_item_check_box);
        progressBar = view.findViewById(R.id.poll_vote_option_progress_bar);
        textTextView = view.findViewById(R.id.poll_vote_option_list_item_text_text_view);

        this.onClickListener = onClickListener;

        view.setOnClickListener(this);
    }

    @Override
    public void onClick(View view) {
        onClickListener.onItemClick(this, getAdapterPosition());
    }
}