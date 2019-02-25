package com.eulersbridge.isegoria.friends

import android.support.v7.recyclerview.extensions.ListAdapter
import android.support.v7.util.DiffUtil
import android.view.LayoutInflater
import android.view.ViewGroup
import com.eulersbridge.isegoria.R
import com.eulersbridge.isegoria.network.api.model.GenericUser
import com.eulersbridge.isegoria.network.api.model.Institution
import com.eulersbridge.isegoria.network.api.model.User
import java.lang.ref.WeakReference

private class UserDiffCallback : DiffUtil.ItemCallback<User>() {
    override fun areItemsTheSame(oldItem: User?, newItem: User?): Boolean {
        return oldItem?.id == newItem?.id
    }

    override fun areContentsTheSame(oldItem: User?, newItem: User?): Boolean {
        return oldItem == newItem
    }
}

internal class SearchAdapter(private val delegate: UserDelegate?) :
    ListAdapter<User, UserViewHolder>(UserDiffCallback()), UserViewHolder.OnClickListener {

    internal interface UserDelegate {

        fun onSearchedUserClick(user: User?)
        fun onSearchedUserActionClick(user: User?)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): UserViewHolder {
        val itemView = LayoutInflater.from(parent.context)
            .inflate(R.layout.friend_partial_list_item, parent, false)

        return UserViewHolder(itemView, R.drawable.friends, this)
    }

    override fun onBindViewHolder(viewHolder: UserViewHolder, position: Int) {
        val item = getItem(position)
        viewHolder.setItem(item)

        if (delegate != null && item.institutionId != null) {
            val weakViewHolder = WeakReference(viewHolder)
        }
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
