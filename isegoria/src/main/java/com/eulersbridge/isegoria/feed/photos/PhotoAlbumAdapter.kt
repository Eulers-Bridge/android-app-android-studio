package com.eulersbridge.isegoria.feed.photos

import android.os.Bundle
import android.support.v4.app.Fragment
import android.view.LayoutInflater
import android.view.ViewGroup
import com.eulersbridge.isegoria.MainActivity
import com.eulersbridge.isegoria.R
import com.eulersbridge.isegoria.network.api.models.PhotoAlbum
import com.eulersbridge.isegoria.util.Constants
import com.eulersbridge.isegoria.util.ui.LoadingAdapter
import org.parceler.Parcels
import java.lang.ref.WeakReference

internal class PhotoAlbumAdapter internal constructor(fragment: Fragment) : LoadingAdapter<PhotoAlbum, PhotoAlbumViewHolder>(0), PhotoAlbumViewHolder.ClickListener {

    private val weakFragment: WeakReference<Fragment> = WeakReference(fragment)

    private fun isValidFragment(fragment: Fragment?): Boolean {
        return (fragment != null
                && fragment.activity != null
                && !fragment.isDetached
                && fragment.isAdded)
    }

    override fun onCreateViewHolder(viewGroup: ViewGroup, viewType: Int): PhotoAlbumViewHolder {
        val itemView = LayoutInflater.from(viewGroup.context).inflate(R.layout.photo_album_list_item, viewGroup, false)
        return PhotoAlbumViewHolder(itemView, this)
    }

    override fun onClick(item: PhotoAlbum?) {
        val fragment = weakFragment.get()

        if (isValidFragment(fragment)) {
            val albumFragment = PhotoAlbumFragment()

            val args = Bundle()
            args.putParcelable(Constants.FRAGMENT_EXTRA_PHOTO_ALBUM, Parcels.wrap<PhotoAlbum>(item))

            albumFragment.arguments = args

            (fragment!!.activity as? MainActivity)?.presentContent(albumFragment)
        }
    }
}