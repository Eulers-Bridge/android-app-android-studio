package com.eulersbridge.isegoria.feed.photos

import android.content.Context
import android.view.View
import android.widget.ImageView

import com.bumptech.glide.load.resource.drawable.DrawableTransitionOptions
import com.eulersbridge.isegoria.GlideApp
import com.eulersbridge.isegoria.R
import com.eulersbridge.isegoria.network.api.models.Photo
import com.eulersbridge.isegoria.util.ui.LoadingAdapter

internal class PhotoViewHolder(itemView: View, private val clickListener: ClickListener?) : LoadingAdapter.ItemViewHolder<Photo>(itemView) {

    private val imageView: ImageView = itemView.findViewById(R.id.photo_grid_item_image_view)
    private var isImageLoadStarted = false

    internal interface ClickListener {
        fun onClick(context: Context, position: Int)
    }

    init {
        imageView.setOnClickListener {
            clickListener?.onClick(imageView.context, adapterPosition)
        }
    }

    override fun onRecycled() {
        if (imageView.context != null && isImageLoadStarted)
            GlideApp.with(imageView.context).clear(imageView)
    }

    override fun setItem(item: Photo?) {
        if (item != null) {
            imageView.contentDescription = item.title

            GlideApp.with(imageView.context)
                    .load(item.thumbnailUrl)
                    .transition(DrawableTransitionOptions.withCrossFade())
                    .into(imageView)

            isImageLoadStarted = true
        }
    }
}