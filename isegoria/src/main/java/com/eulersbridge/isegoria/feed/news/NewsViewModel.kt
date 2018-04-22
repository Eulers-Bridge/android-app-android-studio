package com.eulersbridge.isegoria.feed.news

import android.arch.lifecycle.MutableLiveData
import android.arch.lifecycle.ViewModel
import com.eulersbridge.isegoria.data.Repository
import com.eulersbridge.isegoria.network.api.model.NewsArticle
import com.eulersbridge.isegoria.util.extension.subscribeSuccess
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.rxkotlin.addTo
import javax.inject.Inject

class NewsViewModel
@Inject constructor(
    private val repository: Repository
) : ViewModel() {

    private val compositeDisposable = CompositeDisposable()
    internal val newsArticles = MutableLiveData<List<NewsArticle>>()

    init {
        fetchArticles()
    }

    override fun onCleared() {
        compositeDisposable.dispose()
    }

    internal fun onRefresh() {
        fetchArticles()
    }

    private fun fetchArticles() {
        repository.getNewsArticles().subscribeSuccess {
            newsArticles.postValue(it)
        }.addTo(compositeDisposable)
    }
}
