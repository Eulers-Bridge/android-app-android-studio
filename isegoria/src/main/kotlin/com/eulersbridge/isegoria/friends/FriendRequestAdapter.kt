package com.eulersbridge.isegoria.friends

import android.support.v7.util.DiffUtil
import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.ViewGroup
import com.eulersbridge.isegoria.R
import com.eulersbridge.isegoria.network.api.model.FriendRequest
import com.eulersbridge.isegoria.network.api.model.Institution
import java.lang.ref.WeakReference
import java.util.*

internal class FriendRequestAdapter(
    @param:FriendsFragment.FriendRequestType @field:FriendsFragment.FriendRequestType private val itemType: Int,
    private val delegate: Delegate
) : RecyclerView.Adapter<RecyclerView.ViewHolder>(), ViewHolderDataSource {

    private val items = ArrayList<FriendRequest>()

    internal interface Delegate {
        fun getFriendRequestInstitution(
            institutionId: Long,
            @FriendsFragment.FriendRequestType type: Int,
            weakViewHolder: WeakReference<RecyclerView.ViewHolder>
        )

        fun performFriendRequestAction(@FriendsFragment.FriendRequestType type: Int, request: FriendRequest)
    }

    fun setItems(newItems: List<FriendRequest>) {
        val result = DiffUtil.calculateDiff(object : DiffUtil.Callback() {
            override fun getOldListSize() = items.size

            override fun getNewListSize() = newItems.size

            override fun areItemsTheSame(oldItemPosition: Int, newItemPosition: Int): Boolean {
                val oldItem = items[oldItemPosition]
                val newItem = items[newItemPosition]
                return oldItem.id == newItem.id
            }

            override fun areContentsTheSame(oldItemPosition: Int, newItemPosition: Int): Boolean {
                val oldItem = items[oldItemPosition]
                val newItem = items[newItemPosition]
                return oldItem == newItem
            }
        })

        items.clear()
        items.addAll(newItems)

        result.dispatchUpdatesTo(this)
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
        val item = items[position]

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

    override fun getFriendRequestInstitution(
        institutionId: Long?,
        weakViewHolder: WeakReference<RecyclerView.ViewHolder>
    ) {
        institutionId?.let {
            delegate.getFriendRequestInstitution(it, itemType, weakViewHolder)
        }
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

    fun setInstitution(
        institution: Institution?,
        weakViewHolder: WeakReference<RecyclerView.ViewHolder>
    ) {
        val viewHolder = weakViewHolder.get()

        if (viewHolder != null && institution != null) {
            (viewHolder as? ReceivedFriendRequestViewHolder)?.setInstitution(institution)
                    ?: (viewHolder as? SentFriendRequestViewHolder)?.setInstitution(institution)
        }
    }

    override fun getItemCount() = items.size
}
