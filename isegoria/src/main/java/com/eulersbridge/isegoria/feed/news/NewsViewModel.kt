package com.eulersbridge.isegoria.feed.news

import android.arch.lifecycle.LiveData
import android.arch.lifecycle.ViewModel
import com.eulersbridge.isegoria.data.Repository
import com.eulersbridge.isegoria.network.api.model.NewsArticle
import com.eulersbridge.isegoria.util.extension.toLiveData
import javax.inject.Inject

class NewsViewModel
@Inject constructor(
    private val repository: Repository
) : ViewModel() {

    internal fun getNewsArticles(): LiveData<List<NewsArticle>> {
        return repository.getNewsArticles().toLiveData()
    }
}
