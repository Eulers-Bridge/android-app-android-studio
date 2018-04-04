package com.eulersbridge.isegoria.feed.news

import android.arch.lifecycle.ViewModelProvider
import android.arch.lifecycle.ViewModelProviders
import android.content.Context
import android.os.Bundle
import android.support.v4.app.Fragment
import android.support.v7.widget.GridLayoutManager
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.view.postDelayed
import com.eulersbridge.isegoria.R
import com.eulersbridge.isegoria.network.api.models.NewsArticle
import com.eulersbridge.isegoria.observe
import com.eulersbridge.isegoria.util.ui.TitledFragment
import dagger.android.support.AndroidSupportInjection
import kotlinx.android.synthetic.main.news_fragment.*
import javax.inject.Inject

class NewsFragment : Fragment(), TitledFragment {

    @Inject
    lateinit var modelFactory: ViewModelProvider.Factory
    private lateinit var viewModel: NewsViewModel

    private val newsAdapter: NewsAdapter by lazy {
        val adapter = NewsAdapter()
        adapter.isLoading = true
        adapter
    }

    override fun onAttach(context: Context?) {
        AndroidSupportInjection.inject(this)
        super.onAttach(context)
        viewModel = ViewModelProviders.of(this, modelFactory)[NewsViewModel::class.java]
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View
            = inflater.inflate(R.layout.news_fragment, container, false)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        refreshLayout.apply {
            setColorSchemeResources(R.color.lightBlue)
            setOnRefreshListener {
                isRefreshing = true
                refresh()
                postDelayed(6000) { isRefreshing = false }
            }
        }

        val layoutManager = gridView.layoutManager as GridLayoutManager
        layoutManager.spanSizeLookup = object : GridLayoutManager.SpanSizeLookup() {
            override fun getSpanSize(position: Int): Int {
                return if (position == 0) 2 else 1
            }
        }

        gridView.adapter = newsAdapter

        refresh()
    }

    override fun getTitle(context: Context?) = "News"

    private fun refresh() {
        observe(viewModel.newsArticles) {
            setNewsArticles(it)
        }
    }

    private fun setNewsArticles(articles: List<NewsArticle>?) {
        newsAdapter.isLoading = false

        refreshLayout?.post({ refreshLayout.isRefreshing = false })

        if (articles != null)
            newsAdapter.replaceItems(articles)
    }
}