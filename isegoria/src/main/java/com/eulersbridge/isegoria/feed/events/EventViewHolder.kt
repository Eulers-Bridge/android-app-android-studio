package com.eulersbridge.isegoria.feed.events

import android.content.Intent
import android.support.annotation.ColorRes
import android.support.v4.app.ActivityCompat
import android.support.v4.app.ActivityOptionsCompat
import android.view.View
import android.widget.ImageView
import android.widget.TextView
import androidx.core.os.bundleOf
import com.bumptech.glide.load.resource.drawable.DrawableTransitionOptions
import com.eulersbridge.isegoria.GlideApp
import com.eulersbridge.isegoria.R
import com.eulersbridge.isegoria.feed.events.detail.EventDetailActivity
import com.eulersbridge.isegoria.network.api.model.Event
import com.eulersbridge.isegoria.util.extension.toDateString
import com.eulersbridge.isegoria.util.transformation.TintTransformation
import com.eulersbridge.isegoria.util.ui.LoadingAdapter

internal class EventViewHolder(view: View) : LoadingAdapter.ItemViewHolder<Event>(view), View.OnClickListener {

    private var item: Event? = null

    private val imageView: ImageView = view.findViewById(R.id.event_list_image_view)
    private val titleTextView: TextView = view.findViewById(R.id.event_list_title_text_view)
    private val detailsTextView: TextView = view.findViewById(R.id.event_list_details_text_view)

    private var isImageLoadStarted = false

    init {
        view.setOnClickListener(this)
    }

    override fun setItem(item: Event?) {
        this.item = item

        bindItem(item)
    }

    private fun bindItem(item: Event?) {
        @ColorRes val placeholderRes = R.color.lightGrey

        if (item == null) {
            imageView.setBackgroundResource(placeholderRes)

        } else {
            titleTextView.text = item.name
            detailsTextView.text = item.createdDate.toDateString(detailsTextView.context)

            GlideApp.with(imageView.context)
                .load(item.getPhotoUrl())
                .placeholder(placeholderRes)
                .transform(TintTransformation())
                .transition(DrawableTransitionOptions.withCrossFade())
                .into(imageView)

            isImageLoadStarted = true
        }
    }

    override fun onRecycled() {
        if (imageView.context != null && isImageLoadStarted)
            GlideApp.with(imageView.context).clear(imageView)
    }

    override fun onClick(view: View) {
        if (item == null) return

        val activityIntent = Intent(view.context, EventDetailActivity::class.java)
        activityIntent.putExtras(bundleOf(ACTIVITY_EXTRA_EVENT to item))

        val location = intArrayOf(0, 0)
        view.getLocationOnScreen(location)

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