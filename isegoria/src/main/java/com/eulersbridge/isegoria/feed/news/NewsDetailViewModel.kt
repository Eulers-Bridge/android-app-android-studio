package com.eulersbridge.isegoria.feed.news

import android.app.Application
import android.arch.lifecycle.AndroidViewModel
import android.arch.lifecycle.LiveData
import android.arch.lifecycle.MutableLiveData
import android.arch.lifecycle.Transformations
import com.eulersbridge.isegoria.IsegoriaApp
import com.eulersbridge.isegoria.network.api.models.Like
import com.eulersbridge.isegoria.network.api.models.NewsArticle
import com.eulersbridge.isegoria.network.api.models.User
import com.eulersbridge.isegoria.util.data.RetrofitLiveData
import com.eulersbridge.isegoria.util.data.SingleLiveData

class NewsDetailViewModel(application: Application) : AndroidViewModel(application) {

    internal val newsArticle = MutableLiveData<NewsArticle>()

    private val app: IsegoriaApp by lazy {
        getApplication<IsegoriaApp>()
    }

    private val articleLikes = Transformations.switchMap<NewsArticle, List<Like>>(newsArticle) { article ->
        if (article == null) {
            SingleLiveData(null)

        } else {
            RetrofitLiveData(app.api.getNewsArticleLikes(article.id))
        }
    }

    internal val articleLikeCount = Transformations.switchMap(articleLikes) { likes ->
        return@switchMap SingleLiveData(likes?.size ?: 0)
    }

    internal val articleLikedByUser = Transformations.switchMap(articleLikes) { likes ->
        if (likes != null) {
            Transformations.switchMap<User, Boolean>(app.loggedInUser, { user ->
                if (user != null)
                    likes
                        .filter { it.email == user.email }
                        .forEach { SingleLiveData(true) }

                SingleLiveData<Boolean>(false)
            })
        }

        SingleLiveData(false)
    }

    override fun onCleared() {
        (articleLikes as? RetrofitLiveData)?.cancel()
    }

    /**
     * @return Boolean.TRUE on success, Boolean.FALSE on failure
     */
    internal fun likeArticle(): LiveData<Boolean> {
        return Transformations.switchMap<User, Boolean>(app.loggedInUser) { user ->
            if (user != null)
                return@switchMap Transformations.switchMap<NewsArticle, Boolean>(newsArticle, { article ->
                    val like = RetrofitLiveData(app.api.likeArticle(article.id, user.email))

                    Transformations.switchMap(like) {
                        SingleLiveData(it != null && it.success)
                    }
                })

            SingleLiveData(false)
        }
    }

    /**
     * @return Boolean.TRUE on success, Boolean.FALSE on failure
     */
    internal fun unlikeArticle(): LiveData<Boolean> {
        return Transformations.switchMap<User, Boolean>(app.loggedInUser) { user ->
            if (user != null)
                Transformations.switchMap<NewsArticle, Boolean>(newsArticle, { article ->
                    val unlike = RetrofitLiveData(app.api.unlikeArticle(article.id, user.email))

                    Transformations.switchMap(unlike) { SingleLiveData(true) }
                })

            SingleLiveData(false)
        }
    }
}
