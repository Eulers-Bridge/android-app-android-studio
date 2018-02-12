package com.eulersbridge.isegoria.feed.photos

import android.support.v4.app.Fragment
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.os.bundleOf
import com.eulersbridge.isegoria.MainActivity
import com.eulersbridge.isegoria.R
import com.eulersbridge.isegoria.network.api.models.PhotoAlbum
import com.eulersbridge.isegoria.util.ui.LoadingAdapter
import java.lang.ref.WeakReference

internal class PhotoAlbumAdapter internal constructor(fragment: Fragment)
    : LoadingAdapter<PhotoAlbum, PhotoAlbumViewHolder>(0),
    PhotoAlbumViewHolder.ClickListener {

    private val weakFragment: WeakReference<Fragment> = WeakReference(fragment)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PhotoAlbumViewHolder {
        val itemView = LayoutInflater.from(parent.context).inflate(R.layout.photo_album_list_item,
            parent, false)
        return PhotoAlbumViewHolder(itemView, this)
    }

    override fun onClick(item: PhotoAlbum?) {
        weakFragment.get()?.takeIf {
            it.activity != null && !it.isDetached && it.isAdded

        }?.let {
            val albumFragment = PhotoAlbumFragment()

            albumFragment.arguments = bundleOf(FRAGMENT_EXTRA_PHOTO_ALBUM to item)

            (it.activity as MainActivity).presentContent(albumFragment)
        }
    }
}