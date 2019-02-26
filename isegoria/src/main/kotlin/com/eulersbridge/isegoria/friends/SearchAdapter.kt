package com.eulersbridge.isegoria.friends

import android.support.v7.recyclerview.extensions.ListAdapter
import android.support.v7.util.DiffUtil
import android.view.LayoutInflater
import android.view.ViewGroup
import com.eulersbridge.isegoria.R
import com.eulersbridge.isegoria.network.api.model.GenericUser
import com.eulersbridge.isegoria.network.api.model.User

private class UserDiffCallback : DiffUtil.ItemCallback<FriendsViewModel.FriendSearchResult>() {
    override fun areItemsTheSame(oldItem: FriendsViewModel.FriendSearchResult?, newItem: FriendsViewModel.FriendSearchResult?): Boolean {
        return oldItem?.user?.id == newItem?.user?.id
    }

    override fun areContentsTheSame(oldItem: FriendsViewModel.FriendSearchResult?, newItem: FriendsViewModel.FriendSearchResult?): Boolean {
        return areItemsTheSame(oldItem, newItem) && oldItem?.friendStatus == newItem?.friendStatus
    }
}

internal class SearchAdapter(private val delegate: UserDelegate?) :
    ListAdapter<FriendsViewModel.FriendSearchResult, UserViewHolder>(UserDiffCallback()), UserViewHolder.OnClickListener {

    internal interface UserDelegate {

        fun onSearchedUserClick(user: User?)
        fun onSearchedUserActionClick(user: User?)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): UserViewHolder {
        val itemView = LayoutInflater.from(parent.context)
            .inflate(R.layout.friend_partial_list_item, parent, false)

        return UserViewHolder(itemView, R.drawable.add_friend_blue, this)
    }

    override fun onBindViewHolder(viewHolder: UserViewHolder, position: Int) {
        val item = getItem(position)
        viewHolder.setItem(
                item.user,
                when (item.friendStatus) {
                     FriendsViewModel.FriendStatus.FRIEND -> UserViewHolder.ActionIcon.FRIEND
                     FriendsViewModel.FriendStatus.PENDING -> UserViewHolder.ActionIcon.FRIEND_PENDING
                     FriendsViewModel.FriendStatus.NOT_FRIEND -> UserViewHolder.ActionIcon.ADD_FRIEND
                }
            )
    }

    override fun onBindViewHolder(holder: UserViewHolder, position: Int, payloads: MutableList<Any>) {
        onBindViewHolder(holder, position)
    }

    override fun onViewClick(user: GenericUser?) {
        (user as? User)?.let {
            delegate?.onSearchedUserClick(it)
        }
    }

    override fun onActionClick(user: GenericUser?) {
        (user as? User)?.let {
            delegate?.onSearchedUserActionClick(it)
        }
    }
}
