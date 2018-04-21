package com.eulersbridge.isegoria.feed.photos

import android.arch.lifecycle.ViewModelProvider
import android.arch.lifecycle.ViewModelProviders
import android.content.Context
import android.os.Bundle
import android.support.v4.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.bumptech.glide.load.resource.drawable.DrawableTransitionOptions
import com.eulersbridge.isegoria.GlideApp
import com.eulersbridge.isegoria.R
import com.eulersbridge.isegoria.network.api.model.Photo
import com.eulersbridge.isegoria.network.api.model.PhotoAlbum
import com.eulersbridge.isegoria.util.extension.observe
import dagger.android.support.AndroidSupportInjection
import kotlinx.android.synthetic.main.photo_album_fragment.*
import javax.inject.Inject

class PhotoAlbumFragment : Fragment() {

    @Inject
    lateinit var modelFactory: ViewModelProvider.Factory
    private lateinit var viewModel: PhotoAlbumViewModel
    private val adapter: PhotoAdapter = PhotoAdapter()

    override fun onAttach(context: Context?) {
        AndroidSupportInjection.inject(this)
        super.onAttach(context)
        viewModel = ViewModelProviders.of(this, modelFactory)[PhotoAlbumViewModel::class.java]
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val rootView = inflater.inflate(R.layout.photo_album_fragment, container, false)

        val album: PhotoAlbum? = arguments?.getParcelable(FRAGMENT_EXTRA_PHOTO_ALBUM)
        viewModel.setPhotoAlbum(album!!)

        return rootView
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        gridView.adapter = adapter

        observe(viewModel.photos) { setPhotos(it!!) }

        observe(viewModel.photoAlbum) {
            titleTextView.text = it!!.name
            descriptionTextView.text = it.description

            GlideApp.with(this)
                    .load(it.thumbnailUrl)
                    .placeholder(R.color.lightGrey)
                    .transition(DrawableTransitionOptions.withCrossFade())
                    .into(thumbnailImageView)
        }
    }

    private fun setPhotos(photos: List<Photo>) {
        adapter.apply {
            isLoading = false
            replaceItems(photos)
        }
    }
}
