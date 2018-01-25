package com.eulersbridge.isegoria.feed.news

import android.content.Intent
import android.os.Bundle
import android.support.annotation.DrawableRes
import android.support.v4.app.ActivityCompat
import android.support.v4.app.ActivityOptionsCompat
import android.support.v4.view.ViewCompat
import android.view.View
import android.widget.ImageView
import android.widget.TextView
import com.bumptech.glide.load.resource.bitmap.CenterCrop
import com.bumptech.glide.load.resource.drawable.DrawableTransitionOptions
import com.eulersbridge.isegoria.GlideApp
import com.eulersbridge.isegoria.R
import com.eulersbridge.isegoria.network.api.models.NewsArticle
import com.eulersbridge.isegoria.util.Constants
import com.eulersbridge.isegoria.util.Strings
import com.eulersbridge.isegoria.util.transformation.RoundedCornersTransformation
import com.eulersbridge.isegoria.util.transformation.TintTransformation
import com.eulersbridge.isegoria.util.ui.LoadingAdapter

class NewsViewHolder internal constructor(itemView: View) : LoadingAdapter.ItemViewHolder<NewsArticle>(itemView) {

    private var item: NewsArticle? = null

    private var imageView: ImageView = itemView.findViewById(R.id.imageView)
    private var titleTextView: TextView = itemView.findViewById(R.id.titleTextView)
    private var dateTextView: TextView = itemView.findViewById(R.id.dateTextView)

    init {
        imageView.setOnClickListener { view ->
            if (item != null) {
                val location = intArrayOf(0, 0)
                view.getLocationOnScreen(location)

                val activityIntent = Intent(view.context, NewsDetailActivity::class.java)

                val extras = Bundle()
                extras.putParcelable(Constants.ACTIVITY_EXTRA_NEWS_ARTICLE, item)
                activityIntent.putExtras(extras)

                //Animate with a scale-up transition between the activities
                val options = ActivityOptionsCompat.makeScaleUpAnimation(
                        view,
                        location[0],
                        location[1],
                        view.width,
                        view.height)
                        .toBundle()

                ActivityCompat.startActivity(view.context, activityIntent, options)
            }
        }
    }

    override fun onRecycled() = GlideApp.with(imageView.context).clear(imageView)

    override fun setItem(item: NewsArticle?) {
        this.item = item

        @DrawableRes val placeholderRes = R.drawable.round_rect_placeholder

        if (item == null) {
            ViewCompat.setTransitionName(titleTextView, null)

            imageView.setBackgroundResource(placeholderRes)
            ViewCompat.setTransitionName(imageView, null)

        } else {
            titleTextView.text = item.title

            val dateTime = Strings.fromTimestamp(dateTextView.context, item.dateTimestamp)
            dateTextView.text = dateTime

            ViewCompat.setTransitionName(titleTextView, item.title + "TextView")
            ViewCompat.setTransitionName(imageView, item.title + "ImageView")

            GlideApp.with(imageView.context)
                    .load(item.photoUrl)
                    .placeholder(placeholderRes)
                    .transforms(CenterCrop(), TintTransformation(), RoundedCornersTransformation())
                    .transition(DrawableTransitionOptions.withCrossFade())
                    .into(imageView)
        }
    }
}