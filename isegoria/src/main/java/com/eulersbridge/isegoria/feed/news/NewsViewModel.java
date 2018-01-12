package com.eulersbridge.isegoria.feed.news;

import android.app.Application;
import android.arch.lifecycle.AndroidViewModel;
import android.arch.lifecycle.LiveData;
import android.support.annotation.NonNull;

import com.eulersbridge.isegoria.IsegoriaApp;
import com.eulersbridge.isegoria.network.api.models.NewsArticle;
import com.eulersbridge.isegoria.network.api.models.User;
import com.eulersbridge.isegoria.util.data.RetrofitLiveData;
import com.eulersbridge.isegoria.util.data.FixedData;

import java.util.List;

@SuppressWarnings("WeakerAccess")
public class NewsViewModel extends AndroidViewModel {

    private LiveData<List<NewsArticle>> newsArticlesList;

    public NewsViewModel(@NonNull Application application) {
        super(application);
    }

    LiveData<List<NewsArticle>> getNewsArticles() {
        IsegoriaApp isegoriaApp = getApplication();

        User user = isegoriaApp.getLoggedInUser();

        if (user != null && user.institutionId != null) {
            newsArticlesList = new RetrofitLiveData<>(isegoriaApp.getAPI().getNewsArticles(user.institutionId));

        } else {
            newsArticlesList = new FixedData<>(null);
        }

        return newsArticlesList;
    }

    @Override
    protected void onCleared() {
        if (newsArticlesList != null && newsArticlesList instanceof RetrofitLiveData)
            ((RetrofitLiveData) newsArticlesList).cancel();
    }

}
