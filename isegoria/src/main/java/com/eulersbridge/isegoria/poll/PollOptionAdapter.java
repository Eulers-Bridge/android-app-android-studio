package com.eulersbridge.isegoria.poll;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.bumptech.glide.load.resource.drawable.DrawableTransitionOptions;
import com.eulersbridge.isegoria.GlideApp;
import com.eulersbridge.isegoria.R;
import com.eulersbridge.isegoria.models.PollOption;
import com.eulersbridge.isegoria.models.PollResult;
import com.eulersbridge.isegoria.common.ClickableViewHolder;

import java.util.ArrayList;
import java.util.List;

public class PollOptionAdapter extends RecyclerView.Adapter<PollOptionViewHolder> implements ClickableViewHolder.ClickListener {

    public interface PollOptionVoteListener {
        /**
         * Called when any view of a poll option is clicked, but not when poll voting is disabled
         */
        void onPollOptionClick(int position);
    }

    private boolean pollVotingEnabled = true;

    final private @NonNull PollOptionVoteListener optionVoteListener;

    final private List<PollOption> items = new ArrayList<>();

    PollOptionAdapter(@NonNull PollOptionVoteListener optionVoteListener) {
        this.optionVoteListener = optionVoteListener;
    }

    void replaceItems(List<PollOption> newItems) {
        items.clear();
        items.addAll(newItems);
    }

    void setPollVotingEnabled(boolean pollVotingEnabled) {
        this.pollVotingEnabled = pollVotingEnabled;
    }

    @Override
    public int getItemCount() { return items.size(); }

    @Override
    public void onBindViewHolder(PollOptionViewHolder viewHolder, int index) {
        final PollOption item = items.get(index);

        viewHolder.textTextView.setText(item.text);

        if (item.photo != null) {
            viewHolder.imageView.setVisibility(View.VISIBLE);

            GlideApp.with(viewHolder.imageView)
                    .load(item.photo.thumbnailUrl)
                    .transition(DrawableTransitionOptions.withCrossFade())
                    .into(viewHolder.imageView);
        } else {
            viewHolder.imageView.setVisibility(View.GONE);
        }

        Context context = viewHolder.checkBoxImageView.getContext();

        if (item.hasVoted) {
            viewHolder.checkBoxImageView.setImageResource(R.drawable.tickgreen);
            viewHolder.checkBoxImageView.setContentDescription(context.getString(R.string.checkbox_checked));
        } else {
            viewHolder.checkBoxImageView.setImageResource(R.drawable.tickempty);
            viewHolder.checkBoxImageView.setContentDescription(context.getString(R.string.checkbox_unchecked));
        }

        PollResult result = item.getResult();
        if (result != null) {
            viewHolder.progressBar.setProgress((int)result.count);
        }
    }

    @Override
    public void onItemClick(RecyclerView.ViewHolder viewHolder, int position) {
        if (pollVotingEnabled) {
            PollOption option = items.get(position);

            if (!option.hasVoted) {
                optionVoteListener.onPollOptionClick(position);

                PollOptionViewHolder pollOptionViewHolder = (PollOptionViewHolder)viewHolder;
                pollOptionViewHolder.progressBar.setProgress(pollOptionViewHolder.progressBar.getMax());
            }
        }
    }

    @Override
    public PollOptionViewHolder onCreateViewHolder(ViewGroup viewGroup, int viewType) {
        View itemView = LayoutInflater.from(viewGroup.getContext()).
                inflate(R.layout.poll_vote_option_list_item, viewGroup, false);
        return new PollOptionViewHolder(itemView, this);
    }
}