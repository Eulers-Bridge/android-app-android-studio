package com.eulersbridge.isegoria.feed.photos

import android.view.LayoutInflater
import android.view.ViewGroup
import com.eulersbridge.isegoria.R
import com.eulersbridge.isegoria.network.api.model.PhotoAlbum
import com.eulersbridge.isegoria.util.ui.LoadingAdapter

internal class PhotoAlbumAdapter internal constructor(private val clickListener: PhotoAlbumAdapter.PhotoAlbumClickListener)
    : LoadingAdapter<PhotoAlbum, PhotoAlbumViewHolder>(0),
    PhotoAlbumViewHolder.ClickListener {

    interface PhotoAlbumClickListener {
        fun onClick(item: PhotoAlbum)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PhotoAlbumViewHolder {
        val itemView = LayoutInflater.from(parent.context).inflate(R.layout.photo_album_list_item,
            parent, false)
        return PhotoAlbumViewHolder(itemView, this)
    }

    override fun onClick(item: PhotoAlbum?) {
        if (item != null)
            clickListener.onClick(item)
    }
}