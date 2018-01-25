package com.eulersbridge.isegoria.feed.news;

import android.app.Application;
import android.arch.lifecycle.AndroidViewModel;
import android.arch.lifecycle.LiveData;
import android.arch.lifecycle.Transformations;
import android.support.annotation.NonNull;

import com.eulersbridge.isegoria.IsegoriaApp;
import com.eulersbridge.isegoria.network.api.models.NewsArticle;
import com.eulersbridge.isegoria.util.data.SingleLiveData;
import com.eulersbridge.isegoria.util.data.RetrofitLiveData;

import java.util.List;

@SuppressWarnings("WeakerAccess")
public class NewsViewModel extends AndroidViewModel {

    private LiveData<List<NewsArticle>> newsArticlesList;

    public NewsViewModel(@NonNull Application application) {
        super(application);

        IsegoriaApp app = (IsegoriaApp) application;
        List<NewsArticle> cachedLoginArticles = app.cachedLoginArticles;

        if (cachedLoginArticles != null)
            newsArticlesList = new SingleLiveData<>(cachedLoginArticles);
    }

    LiveData<List<NewsArticle>> getNewsArticles() {
        if (newsArticlesList != null && newsArticlesList.getValue() != null)
            return newsArticlesList;

        IsegoriaApp app = getApplication();

        return Transformations.switchMap(app.loggedInUser, user -> {
            if (user != null && user.institutionId != null) {
                newsArticlesList = new RetrofitLiveData<>(app.getAPI().getNewsArticles(user.institutionId));
                return newsArticlesList;
            }

            return new SingleLiveData<>(null);
        });
    }

    @Override
    protected void onCleared() {
        if (newsArticlesList != null && newsArticlesList instanceof RetrofitLiveData)
            ((RetrofitLiveData) newsArticlesList).cancel();
    }

}
