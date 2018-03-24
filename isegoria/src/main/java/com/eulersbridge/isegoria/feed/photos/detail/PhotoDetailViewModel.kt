package com.eulersbridge.isegoria.feed.photos.detail

import android.arch.lifecycle.LiveData
import android.arch.lifecycle.MutableLiveData
import android.arch.lifecycle.Transformations
import android.arch.lifecycle.ViewModel
import android.support.annotation.IntRange
import com.eulersbridge.isegoria.network.api.API
import com.eulersbridge.isegoria.network.api.models.Like
import com.eulersbridge.isegoria.network.api.models.Photo
import com.eulersbridge.isegoria.network.api.models.User
import com.eulersbridge.isegoria.util.data.RetrofitLiveData
import com.eulersbridge.isegoria.util.data.SingleLiveData
import javax.inject.Inject

class PhotoDetailViewModel
@Inject constructor(
    private val userData: LiveData<User>,
    private val api: API
) : ViewModel() {

    private var photos: List<Photo>? = null
    private val visibleIndex = MutableLiveData<Int>()

    internal val currentPhoto = Transformations.switchMap<Int, Photo>(visibleIndex) { index ->
        return@switchMap SingleLiveData(photos?.get(index))
    }

    internal fun getPhotoUrl(position: Int): String? = photos?.get(position)?.thumbnailUrl

    private fun getPhotoLikes(): LiveData<List<Like>> {
        return Transformations.switchMap(currentPhoto) { (id) ->
            val photoId = id.toLong()
            val photoLikes = RetrofitLiveData(api.getPhotoLikes(photoId))

            Transformations.switchMap<List<Like>, List<Like>>(photoLikes) likes@{ likes ->
                return@likes if (photoId == currentPhoto.value?.id?.toLong()) {
                    SingleLiveData(likes)

                } else {
                    SingleLiveData(null)
                }
            }
        }
    }

    internal fun getPhotoLikeCount(): LiveData<Int> {
        return Transformations.switchMap(getPhotoLikes()) { likes ->
            return@switchMap SingleLiveData(likes?.size ?: 0)
        }
    }

    internal fun getPhotoLikedByUser(): LiveData<Boolean> {
        return Transformations.switchMap(getPhotoLikes()) { likes ->
            val user = userData.value

            if (user != null) {
                val like = likes?.singleOrNull {
                    it.email == user.email
                }

                if (like != null)
                    return@switchMap SingleLiveData(true)
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
        userData.value?.let { user ->
            return Transformations.switchMap(currentPhoto) { (id) ->
                val newsArticleLike =
                    RetrofitLiveData(api.likePhoto(id.toLong(), user.email))

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
        userData.value?.let { user ->
            return Transformations.switchMap(currentPhoto) { (id) ->
                val unlike = RetrofitLiveData(api.unlikePhoto(id.toLong(), user.email))

                Transformations.switchMap(unlike) unlike@ { return@unlike SingleLiveData(true) }
            }
        }

        return SingleLiveData(false)
    }
}
