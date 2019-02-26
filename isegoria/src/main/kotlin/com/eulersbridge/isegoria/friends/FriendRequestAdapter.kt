package com.eulersbridge.isegoria.friends

import android.support.v7.recyclerview.extensions.ListAdapter
import android.support.v7.util.DiffUtil
import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.ViewGroup
import com.eulersbridge.isegoria.R
import com.eulersbridge.isegoria.network.api.model.FriendRequest
import com.eulersbridge.isegoria.network.api.model.Institution
import java.lang.ref.WeakReference

private class FriendRequestDiffCallback : DiffUtil.ItemCallback<FriendRequest>() {
    override fun areItemsTheSame(oldItem: FriendRequest?, newItem: FriendRequest?): Boolean {
        return oldItem?.id == newItem?.id
    }

    override fun areContentsTheSame(oldItem: FriendRequest?, newItem: FriendRequest?): Boolean {
        return oldItem == newItem
    }
}

internal class FriendRequestAdapter(
    @param:FriendsFragment.FriendRequestType @field:FriendsFragment.FriendRequestType private val itemType: Int,
    private val delegate: Delegate
) : ListAdapter<FriendRequest, RecyclerView.ViewHolder>(FriendRequestDiffCallback()), ViewHolderDataSource {

    internal interface Delegate {
        fun performFriendRequestAction(@FriendsFragment.FriendRequestType type: Int, request: FriendRequest)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        val itemView = LayoutInflater.from(parent.context)
                .inflate(R.layout.friend_partial_list_item, parent, false)

        return if (this.itemType == FriendsFragment.RECEIVED) {
            ReceivedFriendRequestViewHolder(itemView, this)
        } else {
            // itemType == FriendsFragment.SENT
            SentFriendRequestViewHolder(itemView, this)
        }
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        val item = getItem(position)

        if (this.itemType == FriendsFragment.RECEIVED) {
            (holder as ReceivedFriendRequestViewHolder).setItem(item)
        } else {
            // itemType == FriendsFragment.SENT
            (holder as SentFriendRequestViewHolder).setItem(item)
        }
    }

    override fun onBindViewHolder(
            holder: RecyclerView.ViewHolder,
            position: Int,
            payloads: MutableList<Any>
    ) {
        onBindViewHolder(holder, position)
    }

    override fun onClick(friendRequest: FriendRequest?) {
        friendRequest?.let {
            delegate.performFriendRequestAction(itemType, it)
        }
    }

    override fun onActionClick(friendRequest: FriendRequest?) {
        friendRequest?.let {
            delegate.performFriendRequestAction(itemType, it)
        }
    }
}
