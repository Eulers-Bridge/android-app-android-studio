package com.eulersbridge.isegoria.election.candidates.positions

import android.view.View
import android.widget.ImageView
import android.widget.TextView
import com.bumptech.glide.load.resource.drawable.DrawableTransitionOptions
import com.eulersbridge.isegoria.GlideApp
import com.eulersbridge.isegoria.R
import com.eulersbridge.isegoria.network.api.models.Position
import com.eulersbridge.isegoria.util.transformation.TintTransformation
import com.eulersbridge.isegoria.util.ui.LoadingAdapter

internal class PositionViewHolder(itemView: View, private val listener: PositionItemListener?) :
    LoadingAdapter.ItemViewHolder<Position>(itemView) {

    private var item: Position? = null
    private val imageView: ImageView = itemView.findViewById(R.id.election_position_grid_item_image_view)
    private val titleTextView: TextView = itemView.findViewById(R.id.election_position_grid_item_title_text_view)

    internal interface PositionItemListener {
        fun onClick(item: Position?)
        fun getPhoto(viewHolder: PositionViewHolder, itemId: Long)
    }

    init {
        itemView.setOnClickListener {
            listener?.onClick(item)
        }
    }

    override fun setItem(item: Position?) {
        this.item = item

        imageView.setImageResource(R.color.lightGrey)

        if (item == null) {
            titleTextView.text = null
            imageView.setImageDrawable(null)

        } else {
            titleTextView.text = item.name
            listener?.getPhoto(this, item.id)
        }
    }

    override fun onRecycled() = GlideApp.with(imageView.context).clear(imageView)

    fun setImageURL(imageURL: String?, itemId: Long) {
        if (itemId == item!!.id && !imageURL.isNullOrBlank()) {
            GlideApp.with(imageView.context)
                .load(imageURL)
                .placeholder(R.color.lightGrey)
                .transform(TintTransformation())
                .transition(DrawableTransitionOptions.withCrossFade())
                .into(imageView)
        }
    }
}