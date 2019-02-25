package com.eulersbridge.isegoria.friends

import android.support.annotation.DrawableRes
import android.support.v7.widget.RecyclerView
import android.view.View
import android.widget.ImageView
import android.widget.TextView
import androidx.core.view.isGone
import com.bumptech.glide.load.resource.drawable.DrawableTransitionOptions
import com.eulersbridge.isegoria.GlideApp
import com.eulersbridge.isegoria.R
import com.eulersbridge.isegoria.network.api.model.FriendRequest
import com.eulersbridge.isegoria.network.api.model.Institution
import java.lang.ref.WeakReference

internal class SentFriendRequestViewHolder(
        itemView: View,
        private val dataSource: ViewHolderDataSource
) : RecyclerView.ViewHolder(itemView) {

    private var item: FriendRequest? = null

    private val imageView: ImageView = itemView.findViewById(R.id.friends_list_image_view)
    private val nameTextView: TextView = itemView.findViewById(R.id.friends_list_name_text_view)
    private val institutionTextView: TextView = itemView.findViewById(R.id.friends_list_subtext_text_view)

    init {
        //Grabs the action image (adding friend button/pending)
        //Sets the images drawable to the pending one
        val actionImageView = itemView.findViewById<ImageView>(R.id.friends_list_action_image_view)
        actionImageView.setImageResource(R.drawable.added_inactive)

        itemView.setOnClickListener { dataSource.onClick(item) }
    }

    fun setItem(item: FriendRequest?) {
        this.item = item

        bindItem(item)
    }

    fun bindItem(item: FriendRequest?) {
        if (item == null) {
            nameTextView.text = null
            institutionTextView.text = null

        } else {
            val user = item.requestReceiver

            nameTextView.text = user!!.fullName
            dataSource.getFriendRequestInstitution(
                    user.institutionId,
                    WeakReference(this)
            )

            GlideApp.with(imageView.context)
                    .load(user.profilePhotoURL)
                    .placeholder(R.drawable.account_circle_24dp)
                    .transition(DrawableTransitionOptions.withCrossFade())
                    .into(imageView)
        }
    }

    fun setInstitution(institution: Institution) {
        if (item?.requester?.institutionId == institution.id)
            institutionTextView.text = institution.getName()
    }
}
