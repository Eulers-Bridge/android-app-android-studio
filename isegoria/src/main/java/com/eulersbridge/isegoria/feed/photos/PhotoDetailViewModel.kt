package com.eulersbridge.isegoria.feed.photos

import android.app.Application
import android.arch.lifecycle.AndroidViewModel
import android.arch.lifecycle.LiveData
import android.arch.lifecycle.MutableLiveData
import android.arch.lifecycle.Transformations
import android.support.annotation.IntRange
import com.eulersbridge.isegoria.IsegoriaApp
import com.eulersbridge.isegoria.network.api.models.Like
import com.eulersbridge.isegoria.network.api.models.Photo
import com.eulersbridge.isegoria.util.data.RetrofitLiveData
import com.eulersbridge.isegoria.util.data.SingleLiveData

class PhotoDetailViewModel(application: Application) : AndroidViewModel(application) {

    private var photos: List<Photo>? = null
    private val visibleIndex = MutableLiveData<Int>()

    internal val currentPhoto = Transformations.switchMap<Int, Photo>(visibleIndex) { index ->
        SingleLiveData(photos?.get(index))
    }

    internal fun getPhotoUrl(position: Int): String? = photos?.get(position)?.thumbnailUrl

    private fun getPhotoLikes(): LiveData<List<Like>> {
        return Transformations.switchMap(currentPhoto) { (id) ->
            val app = getApplication<IsegoriaApp>()
            val photoId = id.toLong()
            val photoLikes = RetrofitLiveData(app.api.getPhotoLikes(photoId))

            Transformations.switchMap<List<Like>, List<Like>>(photoLikes) likes@{ likes ->
                if (photoId == currentPhoto.value?.id?.toLong()) {
                    return@likes SingleLiveData(likes)

                } else {
                    return@likes SingleLiveData(null)
                }
            }
        }
    }

    internal fun getPhotoLikeCount(): LiveData<Int> {
        return Transformations.switchMap(getPhotoLikes()) { likes ->
            SingleLiveData(likes?.size ?: 0)
        }
    }

    internal fun getPhotoLikedByUser(): LiveData<Boolean> {
        return Transformations.switchMap(getPhotoLikes()) { likes ->
            val app = getApplication<IsegoriaApp>()
            val user = app.loggedInUser.value

            if (user != null) {
                val like = likes?.singleOrNull {
                    it.email == user.email
                }

                if (like != null)
                    SingleLiveData(true)
            }

            SingleLiveData(false)
        }
    }

    internal val photoCount: Int by lazy {
        photos?.size ?: 0
    }

    internal fun changePhoto(@IntRange(from = 0) position: Int) {
        visibleIndex.value = position
    }

    internal fun setPhotos(photos: List<Photo>, startIndex: Int) {
        this.photos = photos
        visibleIndex.value = startIndex
    }

    /**
     * @return Boolean.TRUE on success, Boolean.FALSE on failure
     */
    internal fun likePhoto(): LiveData<Boolean> {
        val app = getApplication<IsegoriaApp>()

        app.loggedInUser.value?.let { user ->
            return Transformations.switchMap(currentPhoto) { (id) ->
                val newsArticleLike =
                    RetrofitLiveData(app.api.likePhoto(id.toLong(), user.email))

                Transformations.switchMap(
                    newsArticleLike
                ) { likeResponse -> SingleLiveData(likeResponse != null && likeResponse.success) }
            }
        }

        return SingleLiveData(false)
    }

    /**
     * @return Boolean.TRUE on success, Boolean.FALSE on failure
     */
    internal fun unlikePhoto(): LiveData<Boolean> {
        val app = getApplication<IsegoriaApp>()

        app.loggedInUser.value?.let { user ->
            return Transformations.switchMap(
                currentPhoto
            ) { (id) ->
                val unlike = RetrofitLiveData(app.api.unlikePhoto(id.toLong(), user.email))

                Transformations.switchMap(unlike) { SingleLiveData(true) }
            }
        }

        return SingleLiveData(false)
    }
}
