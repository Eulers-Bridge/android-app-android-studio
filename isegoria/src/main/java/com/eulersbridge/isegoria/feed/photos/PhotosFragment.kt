package com.eulersbridge.isegoria.feed.photos

import android.arch.lifecycle.ViewModelProviders
import android.content.Context
import android.os.Bundle
import android.support.v4.app.Fragment
import android.support.v7.widget.DividerItemDecoration
import android.support.v7.widget.LinearLayoutManager
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.os.bundleOf
import androidx.view.postDelayed
import com.eulersbridge.isegoria.*
import com.eulersbridge.isegoria.network.api.models.PhotoAlbum
import com.eulersbridge.isegoria.util.ui.TitledFragment
import kotlinx.android.synthetic.main.photos_fragment.*

class PhotosFragment : Fragment(), TitledFragment, PhotoAlbumAdapter.PhotoAlbumClickListener {

    private var app: IsegoriaApp? = null
    private val adapter = PhotoAlbumAdapter(this)

    private val viewModel: PhotoAlbumsViewModel by lazy {
        ViewModelProviders.of(requireActivity()).get(PhotoAlbumsViewModel::class.java)
    }

    private var fetchedPhotos = false

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val rootView = inflater.inflate(R.layout.photos_fragment, container, false)

        app = activity?.application as IsegoriaApp

        return rootView
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        refreshLayout.setOnRefreshListener {
            refreshLayout.isRefreshing = true
            refresh()
            refreshLayout.postDelayed(6000) { refreshLayout.isRefreshing = false }
        }

        albumsListView.apply {
            addItemDecoration(DividerItemDecoration(context, LinearLayoutManager.VERTICAL))
            adapter = this@PhotosFragment.adapter
        }

        refresh()
    }

    override fun getTitle(context: Context?) = "Photos"

    override fun setUserVisibleHint(isVisibleToUser: Boolean) {
        super.setUserVisibleHint(isVisibleToUser)

        if (view != null && app != null && !fetchedPhotos)
            refresh()
    }

    private fun refresh() {
        fetchedPhotos = true

        val user = app?.loggedInUser?.value
        if (user?.institutionId != null) {

            if (user.newsFeedId == 0L) {
                app!!.api.getInstitutionNewsFeed(user.institutionId!!).onSuccess { response ->
                    val updatedUser = user.copy()
                    updatedUser.newsFeedId = response.newsFeedId
                    app?.updateLoggedInUser(updatedUser)

                    fetchPhotoAlbums()
                }

            } else {
                fetchPhotoAlbums()
            }
        }
    }

    private fun fetchPhotoAlbums() {
        observe(viewModel.photoAlbums) { albums ->
            refreshLayout?.isRefreshing = false
            adapter.isLoading = false

            if (albums != null)
                adapter.replaceItems(albums)
        }
    }

    override fun onClick(item: PhotoAlbum) {
        val albumFragment = PhotoAlbumFragment()
        albumFragment.arguments = bundleOf(FRAGMENT_EXTRA_PHOTO_ALBUM to item)

        (activity as? MainActivity)?.presentContent(albumFragment)
    }
}
