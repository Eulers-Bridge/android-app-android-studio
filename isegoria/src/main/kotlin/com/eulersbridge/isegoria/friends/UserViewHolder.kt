package com.eulersbridge.isegoria.friends

import android.support.annotation.DrawableRes
import android.support.v7.widget.RecyclerView
import android.view.View
import android.widget.ImageView
import android.widget.TextView
import com.bumptech.glide.load.resource.bitmap.CircleCrop

import com.bumptech.glide.load.resource.drawable.DrawableTransitionOptions
import com.eulersbridge.isegoria.GlideApp
import com.eulersbridge.isegoria.R
import com.eulersbridge.isegoria.network.api.model.GenericUser

class UserViewHolder(
        itemView: View,
        private val clickListener: OnClickListener?
) : RecyclerView.ViewHolder(itemView) {

    private lateinit var item: GenericUser
    private lateinit var actionIcon: ActionIcon

    private val imageView: ImageView = itemView.findViewById(R.id.friends_list_image_view)
    private val nameTextView: TextView = itemView.findViewById(R.id.friends_list_name_text_view)
    private val subtextTextView: TextView= itemView.findViewById(R.id.friends_list_subtext_text_view)

    enum class ActionIcon {
        FRIEND,
        FRIEND_PENDING,
        ADD_FRIEND
    }

    interface OnClickListener {
        fun onViewClick(user: GenericUser?)
        fun onActionClick(user: GenericUser?)
    }

    init {
        itemView.setOnClickListener {
            clickListener?.onViewClick(item)
        }

        val actionImageView = itemView.findViewById<ImageView>(R.id.friends_list_action_image_view)
        actionImageView.apply {
            setOnClickListener {
                clickListener?.onActionClick(item)
            }
        }
    }

    fun setItem(item: GenericUser, actionIcon: ActionIcon) {
        this.item = item
        this.actionIcon = actionIcon

        bindItem(item, actionIcon)
    }

    private fun bindItem(user: GenericUser, actionIcon: ActionIcon) {
        if (user == null) {
            nameTextView.text = null
            subtextTextView.text = null

        } else {
            nameTextView.text = user.fullName
            subtextTextView.text = user.email

            GlideApp.with(imageView.context)
                    .load(user.profilePhotoURL)
                    .transform(CircleCrop())
                    .placeholder(R.drawable.account_circle_24dp)
                    .transition(DrawableTransitionOptions.withCrossFade())
                    .into(imageView)

            // check to see if the person is already a friend
            // if they are then show already a friend icon
            // if theyre not show the add friend button

            val actionImageView = itemView.findViewById<ImageView>(R.id.friends_list_action_image_view)

            actionImageView.setImageResource(
                when (actionIcon) {
                    ActionIcon.FRIEND -> R.drawable.added_active
                    ActionIcon.FRIEND_PENDING -> R.drawable.added_inactive
                    ActionIcon.ADD_FRIEND -> R.drawable.add_friend_blue
                }
            )
        }
    }
}
