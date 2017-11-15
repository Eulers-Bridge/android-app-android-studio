package com.eulersbridge.isegoria.feed;


import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.eulersbridge.isegoria.Isegoria;
import com.eulersbridge.isegoria.MainActivity;
import com.eulersbridge.isegoria.R;
import com.eulersbridge.isegoria.models.NewsArticle;
import com.eulersbridge.isegoria.network.SimpleCallback;

import java.util.List;

import retrofit2.Callback;
import retrofit2.Response;

public class NewsFragment extends Fragment {
	
	private NewsAdapter newsAdapter;

	@Override
	public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		View rootView = inflater.inflate(R.layout.news_fragment, container, false);

		MainActivity mainActivity = (MainActivity) getActivity();
        Isegoria isegoria = mainActivity.getIsegoriaApplication();

		long institutionId = isegoria.getLoggedInUser().institutionId;

        SwipeRefreshLayout swipeContainerNews = rootView.findViewById(R.id.swipeContainerNews);
		swipeContainerNews.setColorSchemeResources(R.color.lightBlue);
        swipeContainerNews.setOnRefreshListener(() -> {
            swipeContainerNews.setRefreshing(true);

			isegoria.getAPI().getNewsArticles(institutionId).enqueue(callback);

            (new android.os.Handler()).postDelayed(() -> swipeContainerNews.setRefreshing(false), 6000);
        });

		GridLayoutManager layoutManager = new GridLayoutManager(getContext(), 2);

		layoutManager.setSpanSizeLookup(new GridLayoutManager.SpanSizeLookup() {
			@Override
			public int getSpanSize(int position) {
				return (position == 0)? 2 : 1;
			}
		});

		newsAdapter = new NewsAdapter(this);

		RecyclerView recyclerView = rootView.findViewById(R.id.news_grid_view);
		recyclerView.setLayoutManager(layoutManager);
		recyclerView.setAdapter(newsAdapter);

		isegoria.getAPI().getNewsArticles(institutionId).enqueue(callback);
        
		return rootView;
	}

    private final Callback<List<NewsArticle>> callback = new SimpleCallback<List<NewsArticle>>() {
        @Override
        protected void handleResponse(Response<List<NewsArticle>> response) {
            List<NewsArticle> articles = response.body();

            if (articles != null) {
                setNewsArticles(articles);
            }
        }
    };

	private void setNewsArticles(@NonNull List<NewsArticle> articles) {
		newsAdapter.replaceItems(articles);
		newsAdapter.notifyDataSetChanged();
	}
}