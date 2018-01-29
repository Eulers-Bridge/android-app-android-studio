package com.eulersbridge.isegoria.friends

import android.support.v7.util.DiffUtil
import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.ViewGroup
import com.eulersbridge.isegoria.R
import com.eulersbridge.isegoria.network.api.models.Contact
import com.eulersbridge.isegoria.network.api.models.GenericUser
import com.eulersbridge.isegoria.network.api.models.Institution
import java.lang.ref.WeakReference
import java.util.*

class FriendAdapter internal constructor(private val delegate: Delegate?) :
    RecyclerView.Adapter<UserViewHolder>(), UserViewHolder.OnClickListener {

    private val items = ArrayList<Contact>()

    internal interface Delegate {
        fun getContactInstitution(
            institutionId: Long,
            weakViewHolder: WeakReference<UserViewHolder>
        )

        fun onContactClick(contact: Contact)
    }

    override fun onViewClick(user: GenericUser?) {
        if (user != null)
            delegate?.onContactClick((user as Contact?)!!)
    }

    override fun onActionClick(user: GenericUser?) = onViewClick(user)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): UserViewHolder {
        val itemView = LayoutInflater.from(parent.context)
            .inflate(R.layout.friend_partial_list_item, parent, false)

        return UserViewHolder(itemView, R.drawable.profileactive, this)
    }

    override fun onBindViewHolder(holder: UserViewHolder, position: Int) {
        val item = items[position]
        holder.setItem(item)

        if (delegate != null && item.institutionId != null) {
            val weakViewHolder = WeakReference(holder)
            delegate.getContactInstitution(item.institutionId!!, weakViewHolder)
        }
    }

    override fun onBindViewHolder(holder: UserViewHolder, position: Int, payloads: List<Any>?) {
        onBindViewHolder(holder, position)
    }

    internal fun setInstitution(
        institution: Institution?,
        weakViewHolder: WeakReference<UserViewHolder>
    ) {
        if (institution != null)
            weakViewHolder.get()?.setInstitution(institution)
    }

    internal fun setItems(newItems: List<Contact>) {
        val result = DiffUtil.calculateDiff(object : DiffUtil.Callback() {
            override fun getOldListSize() = items.size

            override fun getNewListSize() = newItems.size

            override fun areItemsTheSame(oldItemPosition: Int, newItemPosition: Int): Boolean {
                val oldItem = items[oldItemPosition]
                val newItem = items[newItemPosition]
                return oldItem.email == newItem.email
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

    override fun getItemCount() = items.size
}
