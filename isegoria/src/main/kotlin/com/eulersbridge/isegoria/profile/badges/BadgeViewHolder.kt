package com.eulersbridge.isegoria.profile.badges

import android.graphics.Color
import android.support.v7.widget.RecyclerView
import android.view.View
import android.widget.ImageView
import android.widget.TextView
import com.bumptech.glide.RequestManager
import com.bumptech.glide.load.resource.drawable.DrawableTransitionOptions
import com.eulersbridge.isegoria.R
import com.eulersbridge.isegoria.network.api.model.Badge

internal class BadgeViewHolder(view: View) : RecyclerView.ViewHolder(view) {

    private var item: Badge? = null
    private var glide: RequestManager? = null

    private val imageView: ImageView = view.findViewById(R.id.badge_list_image_view)
    private val nameTextView: TextView = view.findViewById(R.id.badge_list_name_text_view)
    private val descriptionTextView: TextView = view.findViewById(R.id.badge_list_description_text_view)

    fun setItem(item: Badge?, completed: Boolean) {
        this.item = item

        bindItem(item, completed)
    }

    private fun bindItem(item: Badge?, completed: Boolean) {
        if (item == null) {
            nameTextView.text = null
            descriptionTextView.text = null
            imageView.setImageDrawable(null)

        } else {
            if (completed) {
                imageView.clearColorFilter()
            } else {
                imageView.setColorFilter(Color.argb(125, 35, 35, 35))
            }

            nameTextView.text = item.name
            descriptionTextView.text = item.description
            imageView.setImageDrawable(null)
        }
    }

    fun setImageUrl(glide: RequestManager, itemId: Long, imageUrl: String) {
        if (item?.id == itemId) {
            this.glide = glide

            imageView.post {
                glide.load(imageUrl)
                        .transition(DrawableTransitionOptions.withCrossFade())
                        .into(imageView)
            }
        }
    }

    fun onRecycled() {
        glide?.clear(imageView)
    }
}
