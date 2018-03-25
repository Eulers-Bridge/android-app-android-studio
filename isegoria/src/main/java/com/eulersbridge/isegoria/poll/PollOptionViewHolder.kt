package com.eulersbridge.isegoria.poll

import android.view.View
import android.widget.ImageView
import android.widget.ProgressBar
import android.widget.TextView
import androidx.view.isGone
import androidx.view.isVisible
import com.bumptech.glide.load.resource.drawable.DrawableTransitionOptions
import com.eulersbridge.isegoria.GlideApp
import com.eulersbridge.isegoria.R
import com.eulersbridge.isegoria.network.api.models.PollOption
import com.eulersbridge.isegoria.setCompatTooltipText
import com.eulersbridge.isegoria.util.ui.LoadingAdapter

internal class PollOptionViewHolder(itemView: View, private val clickListener: ClickListener?)
    : LoadingAdapter.ItemViewHolder<PollOption>(itemView) {

    private var item: PollOption? = null
    private val imageView: ImageView
    private val checkBoxImageView: ImageView
    private val progressBar: ProgressBar
    private val textTextView: TextView

    private var isImageLoadStarted = false

    internal interface ClickListener {
        fun onClick(item: PollOption, position: Int)
    }

    init {

        itemView.setOnClickListener {
            item?.let {
                clickListener?.onClick(it, adapterPosition)
            }
        }

        imageView = itemView.findViewById(R.id.poll_vote_option_list_item_image_view)
        checkBoxImageView = itemView.findViewById(R.id.poll_vote_option_list_item_check_box)
        progressBar = itemView.findViewById(R.id.poll_vote_option_progress_bar)
        textTextView = itemView.findViewById(R.id.poll_vote_option_list_item_text_text_view)
    }

    override fun onRecycled() {
        if (imageView.context != null && isImageLoadStarted)
            GlideApp.with(imageView.context).clear(imageView)
    }

    override fun setItem(item: PollOption?) {
        this.item = item

        if (item == null) {
            textTextView.text = null
            imageView.isGone = true
            checkBoxImageView.setImageResource(R.drawable.tick_empty)

        } else {
            textTextView.text = item.text

            if (item.photo != null) {
                imageView.isVisible = true

                GlideApp.with(imageView.context)
                        .load(item.photo.getPhotoUrl())
                        .transition(DrawableTransitionOptions.withCrossFade())
                        .into(imageView)
            } else {
                imageView.isGone = true
            }

            if (item.hasVoted) {
                checkBoxImageView.setImageResource(R.drawable.tick_green)

                val contentDescription = checkBoxImageView.context.getString(R.string.checkbox_checked)
                checkBoxImageView.contentDescription = contentDescription
                checkBoxImageView.setCompatTooltipText(contentDescription)

                progressBar.progress = progressBar.max
            } else {

                checkBoxImageView.setImageResource(R.drawable.tick_empty)

                val contentDescription = checkBoxImageView.context.getString(R.string.checkbox_unchecked)
                checkBoxImageView.contentDescription = contentDescription
                checkBoxImageView.setCompatTooltipText(contentDescription)
            }

            item.result?.let {
                progressBar.progress = it.count.toInt()
            }
        }
    }
}