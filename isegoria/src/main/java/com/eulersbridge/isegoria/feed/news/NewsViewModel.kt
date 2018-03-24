package com.eulersbridge.isegoria.feed.news

import android.arch.lifecycle.LiveData
import android.arch.lifecycle.Transformations
import android.arch.lifecycle.ViewModel
import com.eulersbridge.isegoria.IsegoriaApp
import com.eulersbridge.isegoria.network.api.API
import com.eulersbridge.isegoria.network.api.models.NewsArticle
import com.eulersbridge.isegoria.network.api.models.User
import com.eulersbridge.isegoria.util.data.RetrofitLiveData
import com.eulersbridge.isegoria.util.data.SingleLiveData
import javax.inject.Inject

class NewsViewModel
@Inject constructor(
    private val app: IsegoriaApp,
    private val api: API
) : ViewModel() {

    private var newsArticlesList: LiveData<List<NewsArticle>>? = null

    internal val newsArticles: LiveData<List<NewsArticle>>
        get() {
            newsArticlesList?.takeIf { it.value != null } ?: newsArticlesList

            return Transformations.switchMap<User, List<NewsArticle>>(app.loggedInUser) { user ->
                user?.institutionId?.let {
                    newsArticlesList = RetrofitLiveData(api.getNewsArticles(it))
                    return@switchMap newsArticlesList
                }

                SingleLiveData(null)
            }
        }

    init {
        app.cachedLoginArticles?.let {
            newsArticlesList = SingleLiveData(it)
        }
    }

    override fun onCleared() {
        (newsArticlesList as? RetrofitLiveData)?.cancel()
    }

}
