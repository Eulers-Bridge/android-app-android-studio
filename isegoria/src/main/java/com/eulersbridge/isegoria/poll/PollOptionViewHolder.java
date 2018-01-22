package com.eulersbridge.isegoria.poll;

import android.support.annotation.Nullable;
import android.view.View;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.bumptech.glide.load.resource.drawable.DrawableTransitionOptions;
import com.eulersbridge.isegoria.GlideApp;
import com.eulersbridge.isegoria.R;
import com.eulersbridge.isegoria.util.ui.LoadingAdapter;
import com.eulersbridge.isegoria.network.api.models.PollOption;
import com.eulersbridge.isegoria.network.api.models.PollResult;

class PollOptionViewHolder extends LoadingAdapter.ItemViewHolder<PollOption> {

    interface ClickListener {
        void onClick(PollOption item, int position);
    }

    private final ClickListener clickListener;

    private PollOption item;
    final private ImageView imageView;
    final private ImageView checkBoxImageView;
    final private ProgressBar progressBar;
    final private TextView textTextView;

    PollOptionViewHolder(View itemView, ClickListener clickListener) {
        super(itemView);

        this.clickListener = clickListener;

        itemView.setOnClickListener(view -> {
            if (this.clickListener != null && item != null)
                this.clickListener.onClick(item, getAdapterPosition());
        });

        imageView = itemView.findViewById(R.id.poll_vote_option_list_item_image_view);
        checkBoxImageView = itemView.findViewById(R.id.poll_vote_option_list_item_check_box);
        progressBar = itemView.findViewById(R.id.poll_vote_option_progress_bar);
        textTextView = itemView.findViewById(R.id.poll_vote_option_list_item_text_text_view);
    }

    @Override
    protected void onRecycled() {
        GlideApp.with(imageView.getContext()).clear(imageView);
    }

    @Override
    protected void setItem(@Nullable PollOption item) {
        this.item = item;

        if (item == null) {
            textTextView.setText(null);
            imageView.setVisibility(View.GONE);
            checkBoxImageView.setImageResource(R.drawable.tickempty);

        } else {
            textTextView.setText(item.text);

            if (item.photo != null) {
                imageView.setVisibility(View.VISIBLE);

                GlideApp.with(imageView.getContext())
                        .load(item.photo.thumbnailUrl)
                        .transition(DrawableTransitionOptions.withCrossFade())
                        .into(imageView);
            } else {
                imageView.setVisibility(View.GONE);
            }

            if (item.hasVoted) {
                checkBoxImageView.setImageResource(R.drawable.tickgreen);
                checkBoxImageView.setContentDescription(
                        checkBoxImageView.getContext().getString(R.string.checkbox_checked));

                progressBar.setProgress(progressBar.getMax());
            } else {
                checkBoxImageView.setImageResource(R.drawable.tickempty);
                checkBoxImageView.setContentDescription(
                        checkBoxImageView.getContext().getString(R.string.checkbox_unchecked));
            }

            PollResult result = item.getResult();
            if (result != null)
                progressBar.setProgress((int)result.count);
        }
    }
}