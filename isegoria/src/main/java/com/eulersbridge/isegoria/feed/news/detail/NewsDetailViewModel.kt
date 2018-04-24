package com.eulersbridge.isegoria.feed.news.detail

import android.arch.lifecycle.LiveData
import android.arch.lifecycle.MutableLiveData
import com.eulersbridge.isegoria.data.Repository
import com.eulersbridge.isegoria.network.api.model.NewsArticle
import com.eulersbridge.isegoria.util.BaseViewModel
import com.eulersbridge.isegoria.util.extension.subscribeSuccess
import com.eulersbridge.isegoria.util.extension.toLiveData
import javax.inject.Inject

class NewsDetailViewModel @Inject constructor(private val repository: Repository) : BaseViewModel() {

    private lateinit var _newsArticle: NewsArticle

    internal val newsArticle = MutableLiveData<NewsArticle>()
    internal val likeCount = MutableLiveData<Int>()
    internal val likedByUser = MutableLiveData<Boolean>()

    internal fun setNewsArticle(newsArticle: NewsArticle) {
        _newsArticle = newsArticle
        this.newsArticle.value = newsArticle

        fetchArticleLikes(newsArticle.id)
    }

    private fun fetchArticleLikes(articleId: Long) {
        repository.getNewsArticleLikes(articleId)
                .subscribeSuccess {
                    likeCount.postValue(it.size)

                    val likesContainUser = it.any { it.email == repository.getUser().email }
                    likedByUser.postValue(likesContainUser)
                }.addToDisposable()
    }

    /**
     * @return LiveData whose value is true on success, false on failure
     */
    internal fun likeArticle(): LiveData<Boolean> {
        return repository.likeArticle(_newsArticle.id).toLiveData()
    }

    /**
     * @return LiveData whose value is true on success, false on failure
     */
    internal fun unlikeArticle(): LiveData<Boolean> {
        return repository.unlikeArticle(_newsArticle.id).toLiveData()
    }
}
