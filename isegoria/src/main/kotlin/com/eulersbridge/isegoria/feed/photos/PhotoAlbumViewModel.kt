package com.eulersbridge.isegoria.feed.photos

import android.arch.lifecycle.MutableLiveData
import com.eulersbridge.isegoria.data.Repository
import com.eulersbridge.isegoria.network.api.model.Photo
import com.eulersbridge.isegoria.network.api.model.PhotoAlbum
import com.eulersbridge.isegoria.util.BaseViewModel
import com.eulersbridge.isegoria.util.extension.subscribeSuccess

class PhotoAlbumViewModel(private val repository: Repository) : BaseViewModel() {

    private lateinit var _photoAlbum: PhotoAlbum

    internal val photoAlbum = MutableLiveData<PhotoAlbum>()
    internal val photos = MutableLiveData<List<Photo>>()

    internal fun setPhotoAlbum(album: PhotoAlbum) {
        this._photoAlbum = album
        photoAlbum.postValue(album)

        getAlbumPhotos()
    }

    private fun getAlbumPhotos() {
        repository.getAlbumPhotos(_photoAlbum.id)
                .subscribeSuccess { photos.postValue(it) }
                .addToDisposable()
    }

}