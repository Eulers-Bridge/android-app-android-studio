package com.eulersbridge.isegoria.friends

import android.support.v7.util.DiffUtil
import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.ViewGroup
import com.eulersbridge.isegoria.R
import com.eulersbridge.isegoria.network.api.models.GenericUser
import com.eulersbridge.isegoria.network.api.models.Institution
import com.eulersbridge.isegoria.network.api.models.User
import java.lang.ref.WeakReference
import java.util.*

internal class SearchAdapter(private val delegate: UserDelegate?) :
    RecyclerView.Adapter<UserViewHolder>(), UserViewHolder.OnClickListener {

    private val items = ArrayList<User>()

    internal interface UserDelegate {
        fun getSearchedUserInstitution(
            institutionId: Long,
            weakViewHolder: WeakReference<UserViewHolder>
        )

        fun onSearchedUserClick(user: User?)
        fun onSearchedUserActionClick(user: User?)
    }

    fun clearItems() {
        val oldItemCount = items.size
        items.clear()
        notifyItemRangeRemoved(0, oldItemCount)
    }

    fun setItems(newItems: List<User>) {
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

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): UserViewHolder {
        val itemView = LayoutInflater.from(parent.context)
            .inflate(R.layout.friend_partial_list_item, parent, false)

        return UserViewHolder(itemView, R.drawable.addedinactive, this)
    }

    override fun onBindViewHolder(viewHolder: UserViewHolder, position: Int) {
        val item = items[position]
        viewHolder.setItem(item)

        if (delegate != null && item.institutionId != null) {
            val weakViewHolder = WeakReference(viewHolder)
            delegate.getSearchedUserInstitution(item.institutionId!!, weakViewHolder)
        }
    }

    override fun onBindViewHolder(holder: UserViewHolder, position: Int, payloads: MutableList<Any>) {
        onBindViewHolder(holder, position)
    }

    fun setInstitution(institution: Institution?, weakViewHolder: WeakReference<UserViewHolder>) {
        institution?.let {
            weakViewHolder.get()?.setInstitution(it)
        }
    }

    override fun getItemCount() = items.size

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
