package com.eulersbridge.isegoria.feed.photos

import android.os.Bundle
import android.support.v4.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.bumptech.glide.load.resource.drawable.DrawableTransitionOptions
import com.eulersbridge.isegoria.*
import com.eulersbridge.isegoria.network.api.models.Photo
import com.eulersbridge.isegoria.network.api.models.PhotoAlbum
import kotlinx.android.synthetic.main.photo_album_fragment.*

class PhotoAlbumFragment : Fragment() {

    private val adapter: PhotoAdapter = PhotoAdapter()
    private var album: PhotoAlbum? = null

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val rootView = inflater.inflate(R.layout.photo_album_fragment, container, false)

        album = arguments?.getParcelable(FRAGMENT_EXTRA_PHOTO_ALBUM)

        return rootView
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        gridView.adapter = adapter

        album?.let {
            titleTextView.text = it.name
            descriptionTextView.text = it.description

            GlideApp.with(this)
                    .load(it.thumbnailPhotoUrl)
                    .placeholder(R.color.lightGrey)
                    .transition(DrawableTransitionOptions.withCrossFade())
                    .into(thumbnailImageView)

            val app: IsegoriaApp? = activity?.application as? IsegoriaApp

            app?.api?.getAlbumPhotos(it.id.toLong())?.onSuccess {
                setPhotos(it.photos)
            }
        }
    }

    private fun setPhotos(photos: List<Photo>?) {
        adapter.apply {
            isLoading = true

            if (photos != null)
                replaceItems(photos)
        }
    }
}
