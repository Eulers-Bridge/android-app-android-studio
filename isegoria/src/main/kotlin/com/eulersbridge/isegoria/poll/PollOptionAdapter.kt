package com.eulersbridge.isegoria.poll

import android.view.LayoutInflater
import android.view.ViewGroup
import com.eulersbridge.isegoria.R
import com.eulersbridge.isegoria.network.api.model.PollOption
import com.eulersbridge.isegoria.util.ui.LoadingAdapter

// A poll must have at least 2 options to choose between

internal class PollOptionAdapter internal constructor(private val optionVoteListener: PollOptionVoteListener)
    : LoadingAdapter<PollOption, PollOptionViewHolder>(2),
        PollOptionViewHolder.ClickListener {

    internal var pollVotingEnabled = true

    interface PollOptionVoteListener {
        /**
         * Called when any view of a poll option is clicked, but not when poll voting is disabled
         */
        fun onPollOptionClick(position: Int)
    }

    override fun onClick(item: PollOption, position: Int) {
        if (pollVotingEnabled && !item.hasVoted) {
            optionVoteListener.onPollOptionClick(position)

            items[position].hasVoted = true
            notifyItemChanged(position)
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PollOptionViewHolder {
        val itemView = LayoutInflater.from(parent.context).inflate(R.layout.poll_vote_option_list_item,
            parent, false)
        return PollOptionViewHolder(itemView, this)
    }
}