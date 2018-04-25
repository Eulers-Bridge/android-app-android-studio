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
import com.eulersbridge.isegoria.R
import com.eulersbridge.isegoria.util.extension.observe
import com.eulersbridge.isegoria.util.extension.observeBoolean
import com.eulersbridge.isegoria.util.ui.TitledFragment
import dagger.android.support.AndroidSupportInjection
import kotlinx.android.synthetic.main.news_fragment.*
import javax.inject.Inject

class NewsFragment : Fragment(), TitledFragment {

    @Inject
    lateinit var modelFactory: ViewModelProvider.Factory
    private lateinit var viewModel: NewsViewModel

    private val newsAdapter = NewsAdapter()

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
            setOnRefreshListener { viewModel.refresh() }
        }

        val layoutManager = gridView.layoutManager as GridLayoutManager
        layoutManager.spanSizeLookup = object : GridLayoutManager.SpanSizeLookup() {
            override fun getSpanSize(position: Int) = if (position == 0) 2 else 1
        }

        gridView.adapter = newsAdapter

        createViewModelObserver()
    }

    override fun getTitle(context: Context?) = "News"

    private fun createViewModelObserver() {
        observeBoolean(viewModel.isRefreshing) {
            refreshLayout.post { refreshLayout?.isRefreshing = it }
        }

        observe(viewModel.newsArticles) {
            newsAdapter.replaceItems(it!!)
        }
    }
}