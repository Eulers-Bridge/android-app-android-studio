package com.eulersbridge.isegoria.feed.photos

import android.arch.lifecycle.ViewModelProvider
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
import com.eulersbridge.isegoria.network.NetworkService
import com.eulersbridge.isegoria.network.api.models.PhotoAlbum
import com.eulersbridge.isegoria.util.ui.TitledFragment
import dagger.android.support.AndroidSupportInjection
import kotlinx.android.synthetic.main.photos_fragment.*
import javax.inject.Inject

class PhotosFragment : Fragment(), TitledFragment, PhotoAlbumAdapter.PhotoAlbumClickListener {

    @Inject
    lateinit var modelFactory: ViewModelProvider.Factory
    private lateinit var viewModel: PhotoAlbumsViewModel

    @Inject
    lateinit var app: IsegoriaApp

    @Inject
    lateinit var networkService: NetworkService

    private val adapter = PhotoAlbumAdapter(this)
    private var fetchedPhotos = false

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View?
            = inflater.inflate(R.layout.photos_fragment, container, false)

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

    override fun onAttach(context: Context?) {
        AndroidSupportInjection.inject(this)
        super.onAttach(context)
        viewModel = ViewModelProviders.of(this, modelFactory)[PhotoAlbumsViewModel::class.java]
    }

    override fun getTitle(context: Context?) = "Photos"

    override fun setUserVisibleHint(isVisibleToUser: Boolean) {
        super.setUserVisibleHint(isVisibleToUser)

        if (view != null && !fetchedPhotos)
            refresh()
    }

    private fun refresh() {
        fetchedPhotos = true

        val user = app.loggedInUser.value
        if (user?.institutionId != null) {

            if (user.newsFeedId == 0L) {
                networkService.api.getInstitutionNewsFeed(user.institutionId!!).onSuccess { response ->
                    val updatedUser = user.copy()
                    updatedUser.newsFeedId = response.newsFeedId
                    app.updateLoggedInUser(updatedUser)

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
