package com.eulersbridge.isegoria.feed.photos

import android.arch.lifecycle.MutableLiveData
import com.eulersbridge.isegoria.data.Repository
import com.eulersbridge.isegoria.network.api.model.PhotoAlbum
import com.eulersbridge.isegoria.util.BaseViewModel
import com.eulersbridge.isegoria.util.extension.subscribeSuccess
import javax.inject.Inject

class PhotoAlbumsViewModel @Inject constructor(private val repository: Repository) : BaseViewModel() {

    internal var photoAlbums = MutableLiveData<List<PhotoAlbum>>()

    init {
        fetchPhotoAlbums()
    }
    internal fun refresh() {
        fetchPhotoAlbums()
    }

    private fun fetchPhotoAlbums() {
        repository.getPhotoAlbums().subscribeSuccess {
            photoAlbums.postValue(it)
        }.addToDisposable()
    }

}
