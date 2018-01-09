package com.eulersbridge.isegoria.poll;

import android.support.annotation.NonNull;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.eulersbridge.isegoria.R;
import com.eulersbridge.isegoria.common.LoadingAdapter;
import com.eulersbridge.isegoria.models.PollOption;

public class PollOptionAdapter extends LoadingAdapter<PollOption, PollOptionViewHolder> implements PollOptionViewHolder.ClickListener {

    public interface PollOptionVoteListener {
        /**
         * Called when any view of a poll option is clicked, but not when poll voting is disabled
         */
        void onPollOptionClick(int position);
    }

    private boolean pollVotingEnabled = true;

    final private @NonNull PollOptionVoteListener optionVoteListener;

    PollOptionAdapter(@NonNull PollOptionVoteListener optionVoteListener) {
        // A poll must have at least 2 options to choose between
        super(2);

        this.optionVoteListener = optionVoteListener;
    }

    void setPollVotingEnabled(boolean pollVotingEnabled) {
        this.pollVotingEnabled = pollVotingEnabled;
    }

    @Override
    public void onClick(PollOption item, int position) {
        if (pollVotingEnabled && !item.hasVoted) {
            optionVoteListener.onPollOptionClick(position);

            getItems().get(position).hasVoted = true;
            notifyItemChanged(position);
        }
    }

    @Override
    public PollOptionViewHolder onCreateViewHolder(ViewGroup viewGroup, int viewType) {
        View itemView = LayoutInflater.from(viewGroup.getContext()).
                inflate(R.layout.poll_vote_option_list_item, viewGroup, false);
        return new PollOptionViewHolder(itemView, this);
    }
}