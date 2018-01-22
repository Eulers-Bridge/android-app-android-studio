package com.eulersbridge.isegoria.feed.news;

import android.arch.lifecycle.ViewModelProviders;
import android.content.Context;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.eulersbridge.isegoria.network.api.models.NewsArticle;
import com.eulersbridge.isegoria.util.ui.TitledFragment;
import com.eulersbridge.isegoria.R;

import java.util.List;

public class NewsFragment extends Fragment implements TitledFragment {

    private NewsViewModel viewModel;
    private NewsAdapter newsAdapter;
    private SwipeRefreshLayout refreshLayout;

    @Override
	public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		View rootView = inflater.inflate(R.layout.news_fragment, container, false);

        viewModel = ViewModelProviders.of(this).get(NewsViewModel.class);

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
        viewModel.getNewsArticles().observe(this, this::setNewsArticles);
    }

	private void setNewsArticles(@Nullable List<NewsArticle> articles) {
        newsAdapter.setLoading(false);

        if (refreshLayout != null)
            refreshLayout.post(() -> refreshLayout.setRefreshing(false));

        if (articles != null)
            newsAdapter.replaceItems(articles);
	}
}