package com.eulersbridge.isegoria.feed.news

import android.arch.lifecycle.MutableLiveData
import com.eulersbridge.isegoria.data.Repository
import com.eulersbridge.isegoria.network.api.model.NewsArticle
import com.eulersbridge.isegoria.util.BaseViewModel
import com.eulersbridge.isegoria.util.extension.subscribeSuccess
import com.eulersbridge.isegoria.util.extension.zipWithTimer
import javax.inject.Inject

class NewsViewModel @Inject constructor(private val repository: Repository) : BaseViewModel() {

    internal var isRefreshing = MutableLiveData<Boolean>()
    internal val newsArticles = MutableLiveData<List<NewsArticle>>()

    init {
        isRefreshing.value = false

        repository.getNewsArticles()
                .subscribeSuccess { newsArticles.postValue(it) }
                .addToDisposable()
    }

    internal fun refresh() {
        isRefreshing.postValue(true)

        zipWithTimer(repository.getNewsArticles())
                .subscribeSuccess {
                    isRefreshing.postValue(false)
                    newsArticles.postValue(it)
                }
                .addToDisposable()
    }
}
