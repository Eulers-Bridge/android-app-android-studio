package com.eulersbridge.isegoria.feed.news.detail

import android.arch.lifecycle.ViewModelProvider
import android.arch.lifecycle.ViewModelProviders
import android.os.Bundle
import android.view.View
import androidx.core.view.isGone
import com.bumptech.glide.Priority
import com.bumptech.glide.load.resource.drawable.DrawableTransitionOptions
import com.eulersbridge.isegoria.GlideApp
import com.eulersbridge.isegoria.R
import com.eulersbridge.isegoria.feed.news.ACTIVITY_EXTRA_NEWS_ARTICLE
import com.eulersbridge.isegoria.network.api.model.NewsArticle
import com.eulersbridge.isegoria.util.extension.ifTrue
import com.eulersbridge.isegoria.util.extension.observe
import com.eulersbridge.isegoria.util.extension.observeBoolean
import com.eulersbridge.isegoria.util.extension.toDateString
import com.eulersbridge.isegoria.util.transformation.BlurTransformation
import com.eulersbridge.isegoria.util.transformation.TintTransformation
import dagger.android.support.DaggerAppCompatActivity
import kotlinx.android.synthetic.main.news_detail_activity.*
import javax.inject.Inject

class NewsDetailActivity : DaggerAppCompatActivity() {

    @Inject
    lateinit var modelFactory: ViewModelProvider.Factory
    private lateinit var viewModel: NewsDetailViewModel

    private var userLikedArticle = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContentView(R.layout.news_detail_activity)

        window.decorView.systemUiVisibility = View.SYSTEM_UI_FLAG_LAYOUT_STABLE or View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN

        backButton.setOnClickListener { onBackPressed() }

        val article = intent.getParcelableExtra<NewsArticle>(ACTIVITY_EXTRA_NEWS_ARTICLE)

        viewModel = ViewModelProviders.of(this, modelFactory)[NewsDetailViewModel::class.java]
        viewModel.setNewsArticle(article)
        createViewModelObservers()
    }

    private fun createViewModelObservers() {
        observe(viewModel.newsArticle) {
            it?.let {
                populateUIWithArticle(it)
            }
        }

        observe(viewModel.likeCount) {
            runOnUiThread { likesTextView.text = it.toString() }
        }

        ifTrue(viewModel.likedByUser) {
            runOnUiThread {
                userLikedArticle = true
                starImageView.setImageResource(R.drawable.star)
                starImageView.isEnabled = true
            }
        }
    }

    private fun populateUIWithArticle(article: NewsArticle) {
        GlideApp.with(this)
                .load(article.getPhotoUrl())
                .priority(Priority.HIGH)
                .transforms(BlurTransformation(this), TintTransformation())
                .placeholder(R.color.lightGrey)
                .transition(DrawableTransitionOptions.withCrossFade())
                .into(articleImageView)

        runOnUiThread {
            val creatorPhotoURL = article.creator.profilePhotoURL

            if (creatorPhotoURL.isNullOrBlank()) {
                authorImageView.isGone = true

            } else {
                GlideApp.with(this)
                        .load(creatorPhotoURL)
                        .priority(Priority.LOW)
                        .transition(DrawableTransitionOptions.withCrossFade())
                        .into(authorImageView)
            }

            titleTextView.text = article.title
            likesTextView.text = article.likeCount.toString()
            contentTextView.text = article.content
            authorNameTextView.text = article.creator.fullName
            dateTextView.text = article.date.toDateString(this)

            flagImageView.setOnClickListener { flagImageView.setImageResource(R.drawable.flag) }

            if (article.hasInappropriateContent)
                flagImageView.setImageResource(R.drawable.flag_default)

            starImageView.setOnClickListener { view ->
                userLikedArticle = !userLikedArticle

                view.isEnabled = false

                if (userLikedArticle) {
                    observeBoolean(viewModel.likeArticle()) { success ->
                        view.isEnabled = true

                        if (success) {
                            starImageView.setImageResource(R.drawable.star)

                            val newLikes = Integer.parseInt(likesTextView.text.toString()) + 1
                            likesTextView.text = newLikes.toString()
                        }
                    }

                } else {
                    observeBoolean(viewModel.unlikeArticle()) {success ->
                        view.isEnabled = true

                        if (success) {
                            starImageView.setImageResource(R.drawable.star_24dp)

                            val newLikes = Integer.parseInt(likesTextView.text.toString()) - 1
                            likesTextView.text = newLikes.toString()
                        }
                    }
                }
            }

            //ImageView headShotImageView = (ImageView) rootView.findViewById(R.id.newsArticleHeadView);
            //network.getFirstPhotoImage(email, headShotImageView);

            //authorImageView.setVisibility(ViewGroup.VISIBLE);
        }
    }
}
