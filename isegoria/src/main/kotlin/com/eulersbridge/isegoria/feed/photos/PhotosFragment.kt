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
import androidx.core.os.bundleOf
import com.eulersbridge.isegoria.MainActivity
import com.eulersbridge.isegoria.R
import com.eulersbridge.isegoria.network.api.model.PhotoAlbum
import com.eulersbridge.isegoria.util.extension.observe
import com.eulersbridge.isegoria.util.extension.observeBoolean
import com.eulersbridge.isegoria.util.ui.TitledFragment
import dagger.android.support.AndroidSupportInjection
import kotlinx.android.synthetic.main.photos_fragment.*
import javax.inject.Inject

class PhotosFragment : Fragment(), TitledFragment, PhotoAlbumAdapter.PhotoAlbumClickListener {

    @Inject
    internal lateinit var modelFactory: ViewModelProvider.Factory
    private lateinit var viewModel: PhotoAlbumsViewModel

    private val adapter = PhotoAlbumAdapter(this)

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View?
            = inflater.inflate(R.layout.photos_fragment, container, false)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        refreshLayout.setOnRefreshListener { viewModel.refresh() }

        albumsListView.apply {
            addItemDecoration(DividerItemDecoration(context, LinearLayoutManager.VERTICAL))
            adapter = this@PhotosFragment.adapter
        }
    }

    override fun onAttach(context: Context?) {
        AndroidSupportInjection.inject(this)
        super.onAttach(context)
        viewModel = ViewModelProviders.of(this, modelFactory)[PhotoAlbumsViewModel::class.java]
        createViewModelObservers()
    }

    private fun createViewModelObservers() {
        observeBoolean(viewModel.isRefreshing) {
            refreshLayout.post { refreshLayout?.isRefreshing = it }
        }

        observe(viewModel.photoAlbums) {
            adapter.replaceItems(it!!)
        }
    }

    override fun getTitle(context: Context?) = "Photos"

    override fun onClick(item: PhotoAlbum) {
        val albumFragment = PhotoAlbumFragment()
        albumFragment.arguments = bundleOf(FRAGMENT_EXTRA_PHOTO_ALBUM to item)

        val man = childFragmentManager

        (activity as? MainActivity)?.presentContent(albumFragment)
    }
}
