package com.eulersbridge.isegoria.feed;

import android.content.Context;
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
import com.eulersbridge.isegoria.R;
import com.eulersbridge.isegoria.common.TitledFragment;
import com.eulersbridge.isegoria.models.NewsArticle;
import com.eulersbridge.isegoria.models.User;
import com.eulersbridge.isegoria.network.SimpleCallback;

import java.util.List;

import retrofit2.Callback;
import retrofit2.Response;

public class NewsFragment extends Fragment implements TitledFragment {

    private NewsAdapter newsAdapter;
    private SwipeRefreshLayout refreshLayout;

    @Override
	public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		View rootView = inflater.inflate(R.layout.news_fragment, container, false);

        refreshLayout = rootView.findViewById(R.id.swipeContainerNews);
        refreshLayout.setColorSchemeResources(R.color.lightBlue);
        refreshLayout.setOnRefreshListener(() -> {
            refreshLayout.setRefreshing(true);

			refresh();

            refreshLayout.postDelayed(() -> refreshLayout.setRefreshing(false), 6000);
        });

		newsAdapter = new NewsAdapter();
		newsAdapter.setLoading(true);

        RecyclerView recyclerView = rootView.findViewById(R.id.news_grid_view);

        GridLayoutManager layoutManager = (GridLayoutManager)recyclerView.getLayoutManager();
        layoutManager.setSpanSizeLookup(new GridLayoutManager.SpanSizeLookup() {
            @Override
            public int getSpanSize(int position) {
                return (position == 0)? 2 : 1;
            }
        });

		recyclerView.setAdapter(newsAdapter);

        refresh();
        
		return rootView;
	}

    @Override
    public String getTitle(Context context) {
        return "News";
    }

	private void refresh() {
        Isegoria isegoria = (getActivity() != null)? (Isegoria) getActivity().getApplication() : null;

        if (isegoria != null) {
            User user = isegoria.getLoggedInUser();

            if (user != null && user.institutionId != null)
                isegoria.getAPI().getNewsArticles(user.institutionId).enqueue(callback);
        }
    }

    private final Callback<List<NewsArticle>> callback = new SimpleCallback<List<NewsArticle>>() {
        @Override
        protected void handleResponse(Response<List<NewsArticle>> response) {
            newsAdapter.setLoading(false);

            if (refreshLayout != null)
                refreshLayout.post(() -> refreshLayout.setRefreshing(false));

            List<NewsArticle> articles = response.body();

            if (articles != null)
                setNewsArticles(articles);
        }
    };

	private void setNewsArticles(@NonNull List<NewsArticle> articles) {
        newsAdapter.replaceItems(articles);
	}
}