package com.eulersbridge.isegoria.feed.news

import android.app.Application
import android.arch.lifecycle.AndroidViewModel
import android.arch.lifecycle.LiveData
import android.arch.lifecycle.Transformations
import com.eulersbridge.isegoria.IsegoriaApp
import com.eulersbridge.isegoria.network.api.models.NewsArticle
import com.eulersbridge.isegoria.network.api.models.User
import com.eulersbridge.isegoria.util.data.RetrofitLiveData
import com.eulersbridge.isegoria.util.data.SingleLiveData

class NewsViewModel(application: Application) : AndroidViewModel(application) {

    private var newsArticlesList: LiveData<List<NewsArticle>>? = null

    internal val newsArticles: LiveData<List<NewsArticle>>
        get() {
            newsArticlesList?.takeIf { it.value != null } ?: newsArticlesList

            val app = getApplication<IsegoriaApp>()

            return Transformations.switchMap<User, List<NewsArticle>>(app.loggedInUser) { user ->
                user?.institutionId?.let {
                    newsArticlesList = RetrofitLiveData(app.api.getNewsArticles(it))
                    return@switchMap newsArticlesList
                }

                SingleLiveData(null)
            }
        }

    init {
        val app = application as IsegoriaApp

        app.cachedLoginArticles?.let {
            newsArticlesList = SingleLiveData(it)
        }
    }

    override fun onCleared() {
        (newsArticlesList as? RetrofitLiveData)?.cancel()
    }

}
