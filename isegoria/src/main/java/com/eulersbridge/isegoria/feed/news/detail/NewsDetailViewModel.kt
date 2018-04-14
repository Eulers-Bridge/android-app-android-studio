package com.eulersbridge.isegoria.feed.news.detail

import android.arch.lifecycle.LiveData
import android.arch.lifecycle.MutableLiveData
import android.arch.lifecycle.ViewModel
import com.eulersbridge.isegoria.network.api.API
import com.eulersbridge.isegoria.network.api.models.NewsArticle
import com.eulersbridge.isegoria.network.api.models.User
import com.eulersbridge.isegoria.toBooleanSingle
import com.eulersbridge.isegoria.toLiveData
import com.eulersbridge.isegoria.util.data.SingleLiveData
import io.reactivex.disposables.Disposable
import javax.inject.Inject

class NewsDetailViewModel
@Inject constructor(
    private val userData: LiveData<User>,
    private val api: API
) : ViewModel() {

    private lateinit var _newsArticle: NewsArticle

    internal val newsArticle = MutableLiveData<NewsArticle>()
    internal val likeCount = MutableLiveData<Int>()
    internal val likedByUser = MutableLiveData<Boolean>()

    private lateinit var likesRequest: Disposable

    override fun onCleared() {
        if (!likesRequest.isDisposed) likesRequest.dispose()
    }

    internal fun setNewsArticle(newsArticle: NewsArticle) {
        _newsArticle = newsArticle
        this.newsArticle.value = newsArticle

        fetchArticleLikes(newsArticle.id)
    }

    private fun fetchArticleLikes(articleId: Long) {
        likesRequest = api.getNewsArticleLikes(articleId)
                .onErrorReturnItem(emptyList())
                .subscribe { likes ->
                    likeCount.postValue(likes.size)
                    likedByUser.postValue(likes.any { it.email == userData.value?.email })
                }
    }

    /**
     * @return LiveData whose value is true on success, false on failure
     */
    internal fun likeArticle(): LiveData<Boolean> {
        return userData.value?.let { user ->
            api.likeArticle(_newsArticle.id, user.email)
                    .map { it.success }
                    .onErrorReturnItem(false)
                    .toLiveData()
        } ?: SingleLiveData(false)
    }

    /**
     * @return LiveData whose value is true on success, false on failure
     */
    internal fun unlikeArticle(): LiveData<Boolean> {
        return userData.value?.let { user ->
            api.unlikeArticle(_newsArticle.id, user.email)
                    .toBooleanSingle()
                    .toLiveData()
        } ?: SingleLiveData(false)
    }
}
