package com.eulersbridge.isegoria.feed.news

import android.arch.lifecycle.Observer
import android.arch.lifecycle.ViewModelProviders
import android.content.Context
import android.os.Bundle
import android.support.v4.app.Fragment
import android.support.v7.widget.GridLayoutManager
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.eulersbridge.isegoria.R
import com.eulersbridge.isegoria.network.api.models.NewsArticle
import com.eulersbridge.isegoria.util.ui.TitledFragment
import kotlinx.android.synthetic.main.news_fragment.*

class NewsFragment : Fragment(), TitledFragment {

    private lateinit var viewModel: NewsViewModel
    private lateinit var newsAdapter: NewsAdapter

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val rootView = inflater.inflate(R.layout.news_fragment, container, false)

        viewModel = ViewModelProviders.of(this).get(NewsViewModel::class.java)

        newsAdapter = NewsAdapter()
        newsAdapter.setLoading(true)

        refresh()

        return rootView
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        refreshLayout.setColorSchemeResources(R.color.lightBlue)
        refreshLayout.setOnRefreshListener {
            refreshLayout.isRefreshing = true
            refresh()
            refreshLayout.postDelayed({ refreshLayout!!.isRefreshing = false }, 6000)
        }

        val layoutManager = gridView.layoutManager as GridLayoutManager
        layoutManager.spanSizeLookup = object : GridLayoutManager.SpanSizeLookup() {
            override fun getSpanSize(position: Int): Int {
                return if (position == 0) 2 else 1
            }
        }

        gridView.adapter = newsAdapter
    }

    override fun getTitle(context: Context): String? {
        return "News"
    }

    private fun refresh() {
        viewModel.newsArticles.observe(this, Observer {
            setNewsArticles(it)
        })
    }

    private fun setNewsArticles(articles: List<NewsArticle>?) {
        newsAdapter.setLoading(false)

        refreshLayout?.post({ refreshLayout.isRefreshing = false })

        if (articles != null)
            newsAdapter.replaceItems(articles)
    }
}