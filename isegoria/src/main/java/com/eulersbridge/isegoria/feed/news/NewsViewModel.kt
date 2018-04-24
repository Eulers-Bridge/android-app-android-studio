package com.eulersbridge.isegoria.feed.news

import android.arch.lifecycle.MutableLiveData
import com.eulersbridge.isegoria.data.Repository
import com.eulersbridge.isegoria.network.api.model.NewsArticle
import com.eulersbridge.isegoria.util.BaseViewModel
import com.eulersbridge.isegoria.util.extension.subscribeSuccess
import javax.inject.Inject

class NewsViewModel @Inject constructor(private val repository: Repository) : BaseViewModel() {

    internal val newsArticles = MutableLiveData<List<NewsArticle>>()

    init {
        fetchArticles()
    }

    internal fun onRefresh() {
        fetchArticles()
    }

    private fun fetchArticles() {
        repository.getNewsArticles().subscribeSuccess {
            newsArticles.postValue(it)
        }.addToDisposable()
    }
}
