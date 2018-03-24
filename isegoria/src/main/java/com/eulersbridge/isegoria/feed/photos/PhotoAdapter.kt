package com.eulersbridge.isegoria.feed.photos

import android.content.Context
import android.content.Intent
import android.view.LayoutInflater
import android.view.ViewGroup
import com.eulersbridge.isegoria.R
import com.eulersbridge.isegoria.feed.photos.detail.PhotoDetailActivity
import com.eulersbridge.isegoria.network.api.models.Photo
import com.eulersbridge.isegoria.util.ui.LoadingAdapter

internal class PhotoAdapter : LoadingAdapter<Photo, PhotoViewHolder>(0), PhotoViewHolder.ClickListener {

    override fun onClick(context: Context, position: Int) {
        val activityIntent = Intent(context, PhotoDetailActivity::class.java)

        activityIntent.also {
            it.putParcelableArrayListExtra(ACTIVITY_EXTRA_PHOTOS, items)
            it.putExtra(ACTIVITY_EXTRA_PHOTOS_POSITION, position)

            context.startActivity(it)
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PhotoViewHolder {
        val itemView = LayoutInflater.from(parent.context).inflate(R.layout.photo_grid_item,
            parent, false)
        return PhotoViewHolder(itemView, this)
    }
}