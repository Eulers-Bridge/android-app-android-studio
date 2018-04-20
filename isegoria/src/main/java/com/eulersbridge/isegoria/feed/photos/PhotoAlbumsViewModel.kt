package com.eulersbridge.isegoria.feed.photos

import android.arch.lifecycle.MutableLiveData
import android.arch.lifecycle.ViewModel
import com.eulersbridge.isegoria.Repository
import com.eulersbridge.isegoria.network.api.model.PhotoAlbum
import com.eulersbridge.isegoria.util.extension.subscribeSuccess
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.rxkotlin.addTo
import javax.inject.Inject

class PhotoAlbumsViewModel
@Inject constructor(
        private val repository: Repository
) : ViewModel() {

    internal var photoAlbums = MutableLiveData<List<PhotoAlbum>>()
    private val compositeDisposable = CompositeDisposable()

    init {
        fetchPhotoAlbums()
    }

    override fun onCleared() {
        compositeDisposable.dispose()
    }

    internal fun refresh() {
        fetchPhotoAlbums()
    }

    private fun fetchPhotoAlbums() {
        repository.getPhotoAlbums().subscribeSuccess {
            photoAlbums.postValue(it)
        }.addTo(compositeDisposable)
    }

}
