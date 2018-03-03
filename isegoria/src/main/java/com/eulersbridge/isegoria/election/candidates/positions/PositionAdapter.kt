package com.eulersbridge.isegoria.election.candidates.positions

import android.view.LayoutInflater
import android.view.ViewGroup
import com.bumptech.glide.RequestManager
import com.eulersbridge.isegoria.R
import com.eulersbridge.isegoria.network.api.API
import com.eulersbridge.isegoria.network.api.models.Position
import com.eulersbridge.isegoria.onSuccess
import com.eulersbridge.isegoria.util.ui.LoadingAdapter
import java.lang.ref.WeakReference

internal class PositionAdapter(
    private val glide: RequestManager,
    private val api: API?,
    private val clickListener: PositionClickListener
) :
    LoadingAdapter<Position,
    PositionViewHolder>(1),
    PositionViewHolder.PositionItemListener {

    interface PositionClickListener {
        fun onClick(item: Position)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PositionViewHolder {
        val itemView = LayoutInflater.from(parent.context)
            .inflate(R.layout.election_position_grid_item, parent, false)
        return PositionViewHolder(itemView, this)
    }

    override fun onClick(item: Position?) {
        if (item != null)
            clickListener.onClick(item)
    }

    override fun getPhoto(viewHolder: PositionViewHolder, itemId: Long) {
        if (api != null) {

            val weakViewHolder = WeakReference(viewHolder)

            api.getPhotos(itemId).onSuccess {
                it.photos?.firstOrNull()?.thumbnailUrl?.let { photoThumbnailUrl ->
                    weakViewHolder.get()?.apply {
                        setImageUrl(glide, photoThumbnailUrl, itemId)
                    }
                }
            }
        }
    }
}