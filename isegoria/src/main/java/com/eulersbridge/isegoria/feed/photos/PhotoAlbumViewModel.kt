package com.eulersbridge.isegoria.feed.photos

import android.arch.lifecycle.MutableLiveData
import android.arch.lifecycle.ViewModel
import com.eulersbridge.isegoria.network.api.API
import com.eulersbridge.isegoria.network.api.model.Photo
import com.eulersbridge.isegoria.network.api.model.PhotoAlbum
import com.eulersbridge.isegoria.util.extension.subscribeSuccess
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.rxkotlin.addTo

class PhotoAlbumViewModel(private val api: API) : ViewModel() {

    private lateinit var _photoAlbum: PhotoAlbum
    private val compositeDisposable = CompositeDisposable()

    internal val photoAlbum = MutableLiveData<PhotoAlbum>()
    internal val photos = MutableLiveData<List<Photo>>()

    override fun onCleared() {
        compositeDisposable.dispose()
    }

    internal fun setPhotoAlbum(album: PhotoAlbum) {
        this._photoAlbum = album
        photoAlbum.postValue(album)

        getAlbumPhotos()
    }

    private fun getAlbumPhotos() {
        api.getAlbumPhotos(_photoAlbum.id)
                .map { it.photos ?: emptyList() }
                .onErrorReturnItem(emptyList())
                .subscribeSuccess {
                    photos.postValue(it)
                }.addTo(compositeDisposable)
    }

}