package com.eulersbridge.isegoria.feed.photos

import android.arch.lifecycle.MutableLiveData
import com.eulersbridge.isegoria.data.Repository
import com.eulersbridge.isegoria.network.api.model.PhotoAlbum
import com.eulersbridge.isegoria.util.BaseViewModel
import com.eulersbridge.isegoria.util.extension.subscribeSuccess
import com.eulersbridge.isegoria.util.extension.zipWithTimer
import javax.inject.Inject

class PhotoAlbumsViewModel @Inject constructor(private val repository: Repository) : BaseViewModel() {

    internal var isRefreshing = MutableLiveData<Boolean>()
    internal var photoAlbums = MutableLiveData<List<PhotoAlbum>>()

    init {
        isRefreshing.value = false
        refresh()
    }

    internal fun refresh() {
        isRefreshing.postValue(true)

        zipWithTimer(repository.getPhotoAlbums())
                .subscribeSuccess {
                    photoAlbums.postValue(it)
                    isRefreshing.postValue(false)
                }
                .addToDisposable()
    }

}
