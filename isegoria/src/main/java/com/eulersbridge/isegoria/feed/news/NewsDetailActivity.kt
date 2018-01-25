package com.eulersbridge.isegoria.feed.news

import android.arch.lifecycle.Observer
import android.arch.lifecycle.ViewModelProviders
import android.os.Bundle
import android.support.v4.content.ContextCompat
import android.support.v7.app.AppCompatActivity
import android.text.TextUtils
import android.view.View
import com.bumptech.glide.Priority
import com.bumptech.glide.load.resource.drawable.DrawableTransitionOptions
import com.eulersbridge.isegoria.GlideApp
import com.eulersbridge.isegoria.R
import com.eulersbridge.isegoria.network.api.models.NewsArticle
import com.eulersbridge.isegoria.util.Constants
import com.eulersbridge.isegoria.util.Strings
import com.eulersbridge.isegoria.util.transformation.BlurTransformation
import com.eulersbridge.isegoria.util.transformation.TintTransformation
import kotlinx.android.synthetic.main.news_detail_activity.*

class NewsDetailActivity : AppCompatActivity() {

    private var userLikedArticle = false
    private lateinit var viewModel: NewsDetailViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContentView(R.layout.news_detail_activity)

        viewModel = ViewModelProviders.of(this).get(NewsDetailViewModel::class.java)
        setupModelObservers()

        window.decorView.systemUiVisibility = View.SYSTEM_UI_FLAG_LAYOUT_STABLE or View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN

        backButton.setOnClickListener { onBackPressed() }

        val article = intent.getParcelableExtra<NewsArticle>(Constants.ACTIVITY_EXTRA_NEWS_ARTICLE)
        viewModel.newsArticle.value = article
    }

    private fun setupModelObservers() {
        viewModel.newsArticle.observe(this, Observer<NewsArticle> {
            it?.let {
                populateUIWithArticle(it)
            }
        })

        viewModel.articleLikes.observe(this, Observer { likes ->
            likes?.let {
                runOnUiThread { likesTextView.text = likes.size.toString() }
            }
        })

        viewModel.articleLikedByUser.observe(this, Observer { liked ->
            if (liked == true) {
                runOnUiThread {
                    userLikedArticle = true
                    starImageView.setImageResource(R.drawable.star)
                    starImageView.isEnabled = true
                }
            }
        })
    }

    private fun populateUIWithArticle(article: NewsArticle) {
        GlideApp.with(this)
                .load(article.photoUrl)
                .priority(Priority.HIGH)
                .transforms(BlurTransformation(this), TintTransformation())
                .placeholder(R.color.lightGrey)
                .transition(DrawableTransitionOptions.withCrossFade())
                .into(articleImageView)

        runOnUiThread {
            val creatorPhotoURL = article.creator.profilePhotoURL

            if (TextUtils.isEmpty(creatorPhotoURL)) {
                authorImageView.visibility = View.GONE

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
            dateTextView.text = Strings.fromTimestamp(this, article.dateTimestamp)

            flagImageView.setOnClickListener { flagImageView.setImageResource(R.drawable.flag) }

            if (article.hasInappropriateContent)
                flagImageView.setImageResource(R.drawable.flagdefault)

            starImageView.setOnClickListener { view ->
                userLikedArticle = !userLikedArticle

                view.isEnabled = false

                if (userLikedArticle) {
                    viewModel.likeArticle().observe(this, Observer { success ->
                        view.isEnabled = true

                        if (success == true) {
                            starImageView.setColorFilter(ContextCompat.getColor(this, R.color.star_active))

                            val newLikes = Integer.parseInt(likesTextView.text.toString()) + 1
                            likesTextView.text = newLikes.toString()
                        }
                    })

                } else {
                    viewModel.unlikeArticle().observe(this, Observer { success ->
                        view.isEnabled = true

                        if (success == true) {
                            starImageView.colorFilter = null

                            val newLikes = Integer.parseInt(likesTextView.text.toString()) - 1
                            likesTextView.text = newLikes.toString()
                        }
                    })
                }
            }

            //ImageView headShotImageView = (ImageView) rootView.findViewById(R.id.newsArticleHeadView);
            //network.getFirstPhotoImage(email, headShotImageView);

            //authorImageView.setVisibility(ViewGroup.VISIBLE);
        }
    }
}
