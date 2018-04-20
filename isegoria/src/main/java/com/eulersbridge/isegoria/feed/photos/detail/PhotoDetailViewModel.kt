package com.eulersbridge.isegoria.feed.photos.detail

import android.arch.lifecycle.LiveData
import android.arch.lifecycle.ViewModel
import android.support.annotation.IntRange
import com.eulersbridge.isegoria.Repository
import com.eulersbridge.isegoria.network.api.model.Like
import com.eulersbridge.isegoria.network.api.model.Photo
import com.eulersbridge.isegoria.util.extension.toLiveData
import io.reactivex.BackpressureStrategy
import io.reactivex.Maybe
import io.reactivex.Observable
import io.reactivex.rxkotlin.mergeAllMaybes
import io.reactivex.subjects.BehaviorSubject
import javax.inject.Inject

class PhotoDetailViewModel
@Inject constructor(
        private val repository: Repository
) : ViewModel() {

    private var photos: List<Photo>? = null
    private val visiblePhotoIndex = BehaviorSubject.create<Int>()

    private val photo: Observable<Maybe<Photo>>
    private val photoLikes: Observable<List<Like>>

    init {
        photo = visiblePhotoIndex.map {
            val newPhoto = photos?.get(it)

            newPhoto?.let {
                Maybe.just(it)
            } ?: Maybe.empty()
        }

        photoLikes = photo
                .mergeAllMaybes()
                .switchMap { repository.getPhotoLikes(it.id).toObservable() }
    }

    internal fun setPhotos(photos: List<Photo>, startIndex: Int) {
        this.photos = photos
        visiblePhotoIndex.onNext(startIndex)
    }

    internal val photoCount: Int by lazy {
        photos?.size ?: 0
    }

    internal fun changePhoto(@IntRange(from = 0) position: Int) {
        visiblePhotoIndex.onNext(position)
    }

    internal fun getPhoto(): LiveData<Photo?> {
        val stream = photo.mergeAllMaybes()
        return stream.toLiveData(BackpressureStrategy.LATEST)
    }

    internal fun getPhotoLikeCount(): LiveData<Int> {
        return photoLikes.map { it.size }.toLiveData(BackpressureStrategy.LATEST)
    }

    internal fun getPhotoLikedByUser(): LiveData<Boolean> {
        val stream = photoLikes.map { it.singleOrNull { it.email == repository.getUser().email } != null }
        return stream.toLiveData(BackpressureStrategy.LATEST)
    }

    internal fun getPhotoUrl(position: Int): String? = photos?.get(position)?.getPhotoUrl()

    /**
     * @return LiveData whose value is true on success, false on failure
     */
    internal fun likePhoto(): LiveData<Boolean> {
        return photo.blockingFirst().flatMapSingle { repository.likePhoto(it.id) }.toLiveData()
    }

    /**
     * @return LiveData whose value is true on success, false on failure
     */
    internal fun unlikePhoto(): LiveData<Boolean> {
        return photo.blockingFirst().flatMapSingle { repository.unlikePhoto(it.id) }.toLiveData()
    }
}
