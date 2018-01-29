package com.eulersbridge.isegoria.feed.photos

import android.app.Application
import android.arch.lifecycle.AndroidViewModel
import android.arch.lifecycle.LiveData
import android.arch.lifecycle.Transformations
import com.eulersbridge.isegoria.IsegoriaApp
import com.eulersbridge.isegoria.network.api.models.PhotoAlbum
import com.eulersbridge.isegoria.network.api.models.User
import com.eulersbridge.isegoria.util.data.RetrofitLiveData
import com.eulersbridge.isegoria.util.data.SingleLiveData

class PhotoAlbumsViewModel(application: Application) : AndroidViewModel(application) {

    private var photoAlbumsList: LiveData<List<PhotoAlbum>>? = null

    internal val photoAlbums: LiveData<List<PhotoAlbum>>
        get() {
            val app = getApplication<IsegoriaApp>()

            return Transformations.switchMap<User, List<PhotoAlbum>>(app.loggedInUser) { user ->
                if (user != null) {
                    photoAlbumsList = RetrofitLiveData(app.api.getPhotoAlbums(user.newsFeedId))
                    return@switchMap photoAlbumsList
                }

                SingleLiveData(null)
            }
        }

    override fun onCleared() {
        (photoAlbumsList as? RetrofitLiveData)?.cancel()
    }

}
