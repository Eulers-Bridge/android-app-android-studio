package com.eulersbridge.isegoria.feed.news

import android.arch.lifecycle.LiveData
import android.arch.lifecycle.Transformations
import android.arch.lifecycle.ViewModel
import com.eulersbridge.isegoria.IsegoriaApp
import com.eulersbridge.isegoria.network.api.API
import com.eulersbridge.isegoria.network.api.models.NewsArticle
import com.eulersbridge.isegoria.network.api.models.User
import com.eulersbridge.isegoria.toLiveData
import com.eulersbridge.isegoria.util.data.SingleLiveData
import javax.inject.Inject

class NewsViewModel
@Inject constructor(
    private val app: IsegoriaApp,
    private val api: API
) : ViewModel() {

    private var newsArticlesList: List<NewsArticle>? = null

    internal fun getNewsArticles(): LiveData<List<NewsArticle>> {
        return newsArticlesList?.let {
            SingleLiveData(it)

        } ?: Transformations.switchMap<User, List<NewsArticle>>(app.loggedInUser) { user ->
            user?.institutionId?.let {
                api.getNewsArticles(it)
                        .doOnSuccess {
                            newsArticlesList = it
                        }
                        .toLiveData()
            }

            SingleLiveData(null)
        }
    }

    init {
        newsArticlesList = app.cachedLoginArticles
    }

}
