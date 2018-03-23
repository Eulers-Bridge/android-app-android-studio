package com.eulersbridge.isegoria.feed.photos

import android.arch.lifecycle.LiveData
import android.arch.lifecycle.Transformations
import android.arch.lifecycle.ViewModel
import com.eulersbridge.isegoria.IsegoriaApp
import com.eulersbridge.isegoria.network.NetworkService
import com.eulersbridge.isegoria.network.api.models.PhotoAlbum
import com.eulersbridge.isegoria.network.api.models.User
import com.eulersbridge.isegoria.util.data.RetrofitLiveData
import com.eulersbridge.isegoria.util.data.SingleLiveData
import javax.inject.Inject

class PhotoAlbumsViewModel
@Inject constructor(
    private val app: IsegoriaApp,
    private val networkService: NetworkService
) : ViewModel() {

    private var photoAlbumsList: LiveData<List<PhotoAlbum>>? = null

    internal val photoAlbums: LiveData<List<PhotoAlbum>>
        get() {
            return Transformations.switchMap<User, List<PhotoAlbum>>(app.loggedInUser) { user ->

                return@switchMap if (user == null) {
                    SingleLiveData(null)

                } else {
                    photoAlbumsList = RetrofitLiveData(networkService.api.getPhotoAlbums(user.newsFeedId))
                    photoAlbumsList
                }
            }
        }

    override fun onCleared() {
        (photoAlbumsList as? RetrofitLiveData)?.cancel()
    }

}
