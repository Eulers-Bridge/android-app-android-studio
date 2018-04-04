package com.eulersbridge.isegoria.feed.news

import android.content.Intent
import android.support.annotation.DrawableRes
import android.support.v4.app.ActivityCompat
import android.support.v4.app.ActivityOptionsCompat
import android.view.View
import android.widget.ImageView
import android.widget.TextView
import androidx.core.os.bundleOf
import com.bumptech.glide.load.resource.bitmap.CenterCrop
import com.bumptech.glide.load.resource.drawable.DrawableTransitionOptions
import com.eulersbridge.isegoria.GlideApp
import com.eulersbridge.isegoria.R
import com.eulersbridge.isegoria.feed.news.detail.NewsDetailActivity
import com.eulersbridge.isegoria.network.api.models.NewsArticle
import com.eulersbridge.isegoria.toDateString
import com.eulersbridge.isegoria.util.transformation.RoundedCornersTransformation
import com.eulersbridge.isegoria.util.transformation.TintTransformation
import com.eulersbridge.isegoria.util.ui.LoadingAdapter

class NewsViewHolder internal constructor(itemView: View) : LoadingAdapter.ItemViewHolder<NewsArticle>(itemView) {

    private var item: NewsArticle? = null

    private var imageView: ImageView = itemView.findViewById(R.id.imageView)
    private var titleTextView: TextView = itemView.findViewById(R.id.titleTextView)
    private var dateTextView: TextView = itemView.findViewById(R.id.dateTextView)

    private var isImageLoadStarted = false

    init {
        imageView.setOnClickListener {
            if (item != null) {
                val location = intArrayOf(0, 0)
                it.getLocationOnScreen(location)

                val activityIntent = Intent(it.context, NewsDetailActivity::class.java)

                activityIntent.putExtras(bundleOf(ACTIVITY_EXTRA_NEWS_ARTICLE to item))

                //Animate with a scale-up transition between the activities
                val options = ActivityOptionsCompat.makeScaleUpAnimation(
                    it,
                    location[0],
                    location[1],
                    it.width,
                    it.height)
                .toBundle()

                ActivityCompat.startActivity(it.context, activityIntent, options)
            }
        }
    }

    override fun onRecycled() {
        if (imageView.context != null && isImageLoadStarted)
            GlideApp.with(imageView.context).clear(imageView)
    }

    override fun setItem(item: NewsArticle?) {
        this.item = item

        bindItem(item)
    }

    private fun bindItem(item: NewsArticle?) {
        @DrawableRes val placeholderRes = R.drawable.round_rect_placeholder

        if (item == null) {
            imageView.setBackgroundResource(placeholderRes)

        } else {
            titleTextView.text = item.title
            dateTextView.text = item.date.toDateString(dateTextView.context)

            GlideApp.with(imageView.context)
                .load(item.getPhotoUrl())
                .placeholder(placeholderRes)
                .transforms(CenterCrop(), TintTransformation(), RoundedCornersTransformation())
                .transition(DrawableTransitionOptions.withCrossFade())
                .into(imageView)

            isImageLoadStarted = true
        }
    }
}