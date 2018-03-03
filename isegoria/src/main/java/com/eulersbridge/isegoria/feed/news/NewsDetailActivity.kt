package com.eulersbridge.isegoria.feed.news

import android.arch.lifecycle.ViewModelProviders
import android.os.Bundle
import android.support.v4.content.ContextCompat
import android.support.v7.app.AppCompatActivity
import android.view.View
import androidx.view.isGone
import com.bumptech.glide.Priority
import com.bumptech.glide.load.resource.drawable.DrawableTransitionOptions
import com.eulersbridge.isegoria.GlideApp
import com.eulersbridge.isegoria.R
import com.eulersbridge.isegoria.network.api.models.NewsArticle
import com.eulersbridge.isegoria.observe
import com.eulersbridge.isegoria.toDateString
import com.eulersbridge.isegoria.util.transformation.BlurTransformation
import com.eulersbridge.isegoria.util.transformation.TintTransformation
import kotlinx.android.synthetic.main.news_detail_activity.*

class NewsDetailActivity : AppCompatActivity() {

    private var userLikedArticle = false
    private lateinit var viewModel: NewsDetailViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContentView(R.layout.news_detail_activity)

        window.decorView.systemUiVisibility = View.SYSTEM_UI_FLAG_LAYOUT_STABLE or View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN

        backButton.setOnClickListener { onBackPressed() }

        val article = intent.getParcelableExtra<NewsArticle>(ACTIVITY_EXTRA_NEWS_ARTICLE)

        viewModel = ViewModelProviders.of(this).get(NewsDetailViewModel::class.java)
        viewModel.newsArticle.value = article

        createViewModelObservers()
    }

    private fun createViewModelObservers() {
        viewModel.apply {
            observe(newsArticle) {
                it?.let {
                    populateUIWithArticle(it)
                }
            }

            observe(articleLikeCount) {
                runOnUiThread { likesTextView.text = it.toString() }
            }

            observe(articleLikedByUser) {
                if (it == true)
                    runOnUiThread {
                        userLikedArticle = true
                        starImageView.setImageResource(R.drawable.star)
                        starImageView.isEnabled = true
                    }
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
                flagImageView.setImageResource(R.drawable.flagdefault)

            starImageView.setOnClickListener { view ->
                userLikedArticle = !userLikedArticle

                view.isEnabled = false

                if (userLikedArticle) {
                    observe(viewModel.likeArticle()) { success ->
                        view.isEnabled = true

                        if (success == true) {
                            starImageView.setColorFilter(ContextCompat.getColor(this, R.color.star_active))

                            val newLikes = Integer.parseInt(likesTextView.text.toString()) + 1
                            likesTextView.text = newLikes.toString()
                        }
                    }

                } else {
                    observe(viewModel.unlikeArticle()) {success ->
                        view.isEnabled = true

                        if (success == true) {
                            starImageView.colorFilter = null

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
