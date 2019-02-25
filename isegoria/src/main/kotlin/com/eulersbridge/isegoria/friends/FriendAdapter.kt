package com.eulersbridge.isegoria.friends

import android.support.v7.util.DiffUtil
import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.ViewGroup
import com.eulersbridge.isegoria.R
import com.eulersbridge.isegoria.network.api.model.Contact
import com.eulersbridge.isegoria.network.api.model.GenericUser
import com.eulersbridge.isegoria.network.api.model.Institution
import java.lang.ref.WeakReference

class FriendAdapter internal constructor(private val delegate: Delegate?) :
    RecyclerView.Adapter<UserViewHolder>(), UserViewHolder.OnClickListener {

    private val items = mutableListOf<Contact>()

    internal interface Delegate {
        fun onContactClick(contact: Contact)
    }

    override fun onViewClick(user: GenericUser?) {
        (user as? Contact)?.let {
            delegate?.onContactClick(it)
        }
    }

    override fun onActionClick(user: GenericUser?) = onViewClick(user)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): UserViewHolder {
        val itemView = LayoutInflater.from(parent.context)
            .inflate(R.layout.friend_partial_list_item, parent, false)

        return UserViewHolder(itemView, R.drawable.profile_active, this)
    }

    override fun onBindViewHolder(holder: UserViewHolder, position: Int) {
        val item = items[position]
        holder.setItem(item)
    }

    override fun onBindViewHolder(holder: UserViewHolder, position: Int, payloads: MutableList<Any>) {
        onBindViewHolder(holder, position)
    }

    internal fun setItems(newItems: List<Contact>) {
        val result = DiffUtil.calculateDiff(object : DiffUtil.Callback() {
            override fun getOldListSize() = items.size

            override fun getNewListSize() = newItems.size

            override fun areItemsTheSame(oldItemPosition: Int, newItemPosition: Int): Boolean {
                val oldItem = items[oldItemPosition]
                val newItem = newItems[newItemPosition]
                return oldItem.email == newItem.email
            }

            override fun areContentsTheSame(oldItemPosition: Int, newItemPosition: Int): Boolean {
                val oldItem = items[oldItemPosition]
                val newItem = newItems[newItemPosition]
                return oldItem == newItem
            }
        })

        items.clear()
        items.addAll(newItems)

        result.dispatchUpdatesTo(this)
    }

    override fun getItemCount() = items.size
}
