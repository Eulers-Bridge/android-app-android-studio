package com.eulersbridge.isegoria.feed.photos

import android.os.Bundle
import android.support.v4.app.Fragment
import android.view.LayoutInflater
import android.view.ViewGroup
import com.eulersbridge.isegoria.FRAGMENT_EXTRA_PHOTO_ALBUM
import com.eulersbridge.isegoria.MainActivity
import com.eulersbridge.isegoria.R
import com.eulersbridge.isegoria.network.api.models.PhotoAlbum
import com.eulersbridge.isegoria.util.ui.LoadingAdapter
import java.lang.ref.WeakReference

internal class PhotoAlbumAdapter internal constructor(fragment: Fragment) : LoadingAdapter<PhotoAlbum, PhotoAlbumViewHolder>(0), PhotoAlbumViewHolder.ClickListener {

    private val weakFragment: WeakReference<Fragment> = WeakReference(fragment)

    private fun isValidFragment(fragment: Fragment?): Boolean {
        return (fragment != null
                && fragment.activity != null
                && !fragment.isDetached
                && fragment.isAdded)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PhotoAlbumViewHolder {
        val itemView = LayoutInflater.from(parent.context).inflate(R.layout.photo_album_list_item,
            parent, false)
        return PhotoAlbumViewHolder(itemView, this)
    }

    override fun onClick(item: PhotoAlbum?) {
        val fragment = weakFragment.get()

        if (isValidFragment(fragment)) {
            val albumFragment = PhotoAlbumFragment()

            val args = Bundle()
            args.putParcelable(FRAGMENT_EXTRA_PHOTO_ALBUM, item)

            albumFragment.arguments = args

            (fragment!!.activity as? MainActivity)?.presentContent(albumFragment)
        }
    }
}