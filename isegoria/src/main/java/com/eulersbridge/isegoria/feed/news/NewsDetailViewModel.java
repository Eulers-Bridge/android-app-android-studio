package com.eulersbridge.isegoria.feed.news;

import android.app.Application;
import android.arch.lifecycle.AndroidViewModel;
import android.arch.lifecycle.LiveData;
import android.arch.lifecycle.MutableLiveData;
import android.arch.lifecycle.Transformations;
import android.support.annotation.NonNull;

import com.eulersbridge.isegoria.IsegoriaApp;
import com.eulersbridge.isegoria.network.api.models.Like;
import com.eulersbridge.isegoria.network.api.models.NewsArticle;
import com.eulersbridge.isegoria.network.api.models.User;
import com.eulersbridge.isegoria.network.api.responses.LikeResponse;
import com.eulersbridge.isegoria.util.data.RetrofitLiveData;
import com.eulersbridge.isegoria.util.data.FixedData;

import java.util.List;

@SuppressWarnings("WeakerAccess")
public class NewsDetailViewModel extends AndroidViewModel {

    final MutableLiveData<NewsArticle> newsArticle = new MutableLiveData<>();

    final LiveData<List<Like>> articleLikes = Transformations.switchMap(newsArticle, article -> {
        if (article == null) {
            return new FixedData<>(null);
        } else {
            IsegoriaApp isegoriaApp = getApplication();
            return new RetrofitLiveData<>(isegoriaApp.getAPI().getNewsArticleLikes(article.id));
        }
    });

    final LiveData<Boolean> articleLikedByUser = Transformations.switchMap(articleLikes, likes -> {
        if (likes != null) {
            IsegoriaApp isegoriaApp = getApplication();
            User user = isegoriaApp.getLoggedInUser();

            if (user != null) {
                for (Like like : likes) {
                    if (like.email.equals(isegoriaApp.getLoggedInUser().email)) {
                        return new FixedData<>(true);
                    }
                }
            }
        }

        return new FixedData<>(false);
    });

    public NewsDetailViewModel(@NonNull Application application) {
        super(application);
    }

    @Override
    protected void onCleared() {
        if (articleLikes instanceof RetrofitLiveData)
            ((RetrofitLiveData) articleLikes).cancel();
    }

    /**
     * @return Boolean.TRUE on success, Boolean.FALSE on failure
     */
    LiveData<Boolean> likeArticle() {
        final IsegoriaApp isegoriaApp = getApplication();

        final User user = isegoriaApp.getLoggedInUser();

        if (user != null) {
            return Transformations.switchMap(newsArticle, article -> {
                LiveData<LikeResponse> like = new RetrofitLiveData<>(isegoriaApp.getAPI().likeArticle(article.id, user.email));

                return Transformations.switchMap(like,
                        likeResponse -> new FixedData<>(likeResponse != null && likeResponse.success));
            });
        }

        return new FixedData<>(false);
    }

    /**
     * @return Boolean.TRUE on success, Boolean.FALSE on failure
     */
    LiveData<Boolean> unlikeArticle() {
        IsegoriaApp isegoriaApp = getApplication();

        User user = isegoriaApp.getLoggedInUser();

        if (user != null) {

            return Transformations.switchMap(newsArticle,
                    article -> {
                        LiveData<Void> unlike = new RetrofitLiveData<>(isegoriaApp.getAPI().unlikeArticle(article.id, user.email));

                        return Transformations.switchMap(unlike, __ -> new FixedData<>(true));
                    });
        }

        return new FixedData<>(false);
    }
}
