package com.eulersbridge.isegoria.feed.photos

import android.support.annotation.ColorRes
import android.view.View
import android.widget.ImageView
import android.widget.TextView
import com.bumptech.glide.load.resource.drawable.DrawableTransitionOptions
import com.eulersbridge.isegoria.GlideApp
import com.eulersbridge.isegoria.R
import com.eulersbridge.isegoria.network.api.models.PhotoAlbum
import com.eulersbridge.isegoria.util.ui.LoadingAdapter

internal class PhotoAlbumViewHolder(itemView: View, private val clickListener: ClickListener?) : LoadingAdapter.ItemViewHolder<PhotoAlbum>(itemView) {

    private var item: PhotoAlbum? = null

    private val imageView: ImageView = itemView.findViewById(R.id.photo_album_list_item_image_view)
    private val nameTextView: TextView = itemView.findViewById(R.id.photo_album_list_item_title_text_view)
    private val descriptionTextView: TextView = itemView.findViewById(R.id.photo_album_list_item_description_text_view)

    private var isImageLoadStarted = false

    internal interface ClickListener {
        fun onClick(item: PhotoAlbum?)
    }

    init {
        itemView.setOnClickListener {
            clickListener?.onClick(item)
        }
    }

    override fun onRecycled() {
        if (imageView.context != null && isImageLoadStarted)
            GlideApp.with(imageView.context).clear(imageView)
    }

    override fun setItem(item: PhotoAlbum?) {
        this.item = item

        @ColorRes val placeholderColourRes = R.color.lightGrey

        if (item == null) {
            imageView.setBackgroundResource(placeholderColourRes)

        } else {
            nameTextView.text = item.name
            descriptionTextView.text = item.description

            GlideApp.with(imageView.context)
                    .load(item.thumbnailUrl)
                    .placeholder(placeholderColourRes)
                    .transition(DrawableTransitionOptions.withCrossFade())
                    .into(imageView)

            isImageLoadStarted = true
        }
    }
}