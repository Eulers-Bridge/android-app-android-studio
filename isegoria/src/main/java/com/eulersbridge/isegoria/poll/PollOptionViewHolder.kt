package com.eulersbridge.isegoria.poll

import android.view.View
import android.widget.ImageView
import android.widget.ProgressBar
import android.widget.TextView
import com.bumptech.glide.load.resource.drawable.DrawableTransitionOptions
import com.eulersbridge.isegoria.GlideApp
import com.eulersbridge.isegoria.R
import com.eulersbridge.isegoria.network.api.models.PollOption
import com.eulersbridge.isegoria.util.ui.LoadingAdapter

internal class PollOptionViewHolder(itemView: View, private val clickListener: ClickListener?)
    : LoadingAdapter.ItemViewHolder<PollOption>(itemView) {

    private var item: PollOption? = null
    private val imageView: ImageView
    private val checkBoxImageView: ImageView
    private val progressBar: ProgressBar
    private val textTextView: TextView

    internal interface ClickListener {
        fun onClick(item: PollOption, position: Int)
    }

    init {

        itemView.setOnClickListener {
            if (item != null)
                clickListener?.onClick(item!!, adapterPosition)
        }

        imageView = itemView.findViewById(R.id.poll_vote_option_list_item_image_view)
        checkBoxImageView = itemView.findViewById(R.id.poll_vote_option_list_item_check_box)
        progressBar = itemView.findViewById(R.id.poll_vote_option_progress_bar)
        textTextView = itemView.findViewById(R.id.poll_vote_option_list_item_text_text_view)
    }

    override fun onRecycled() = GlideApp.with(imageView.context).clear(imageView)

    override fun setItem(item: PollOption?) {
        this.item = item

        if (item == null) {
            textTextView.text = null
            imageView.visibility = View.GONE
            checkBoxImageView.setImageResource(R.drawable.tickempty)

        } else {
            textTextView.text = item.text

            if (item.photo != null) {
                imageView.visibility = View.VISIBLE

                GlideApp.with(imageView.context)
                        .load(item.photo.thumbnailUrl)
                        .transition(DrawableTransitionOptions.withCrossFade())
                        .into(imageView)
            } else {
                imageView.visibility = View.GONE
            }

            if (item.hasVoted) {
                checkBoxImageView.setImageResource(R.drawable.tickgreen)
                checkBoxImageView.contentDescription = checkBoxImageView.context.getString(R.string.checkbox_checked)

                progressBar.progress = progressBar.max
            } else {
                checkBoxImageView.setImageResource(R.drawable.tickempty)
                checkBoxImageView.contentDescription = checkBoxImageView.context.getString(R.string.checkbox_unchecked)
            }

            item.result?.let {
                progressBar.progress = it.count.toInt()
            }
        }
    }
}