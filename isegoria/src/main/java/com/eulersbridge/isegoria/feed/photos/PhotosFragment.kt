package com.eulersbridge.isegoria.feed.photos

import android.arch.lifecycle.Observer
import android.arch.lifecycle.ViewModelProviders
import android.content.Context
import android.os.Bundle
import android.support.v4.app.Fragment
import android.support.v7.widget.DividerItemDecoration
import android.support.v7.widget.LinearLayoutManager
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.eulersbridge.isegoria.IsegoriaApp
import com.eulersbridge.isegoria.R
import com.eulersbridge.isegoria.network.api.responses.NewsFeedResponse
import com.eulersbridge.isegoria.util.network.SimpleCallback
import com.eulersbridge.isegoria.util.ui.TitledFragment
import kotlinx.android.synthetic.main.photos_fragment.*
import retrofit2.Response

class PhotosFragment : Fragment(), TitledFragment {

    private var app: IsegoriaApp? = null
    private val adapter = PhotoAlbumAdapter(this)

    private val viewModel: PhotoAlbumsViewModel by lazy {
        ViewModelProviders.of(activity!!).get(PhotoAlbumsViewModel::class.java)
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
            refreshLayout.postDelayed({ refreshLayout.isRefreshing = false }, 6000)
        }

        albumsListView.apply {
            addItemDecoration(DividerItemDecoration(context, LinearLayoutManager.VERTICAL))
            adapter = this.adapter
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
                app!!.api.getInstitutionNewsFeed(user.institutionId!!).enqueue(object : SimpleCallback<NewsFeedResponse>() {
                    override fun handleResponse(response: Response<NewsFeedResponse>) {
                        val body = response.body()

                        if (body != null) {
                            val updatedUser = user.copy()
                            updatedUser.newsFeedId = body.newsFeedId
                            app!!.updateLoggedInUser(updatedUser)

                            fetchPhotoAlbums()
                        }
                    }
                })

            } else {
                fetchPhotoAlbums()
            }
        }
    }

    private fun fetchPhotoAlbums() {
        viewModel.photoAlbums.observe(this, Observer { albums ->
            refreshLayout?.isRefreshing = false
            adapter.isLoading = false

            if (albums != null)
                adapter.replaceItems(albums)
        })
    }
}
