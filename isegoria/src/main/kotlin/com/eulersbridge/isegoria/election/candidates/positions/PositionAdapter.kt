package com.eulersbridge.isegoria.election.candidates.positions

import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.ViewGroup
import com.bumptech.glide.RequestManager
import com.eulersbridge.isegoria.R
import com.eulersbridge.isegoria.data.Repository
import com.eulersbridge.isegoria.network.api.model.Position
import com.eulersbridge.isegoria.util.extension.subscribeSuccess
import com.eulersbridge.isegoria.util.ui.LoadingAdapter
import io.reactivex.disposables.CompositeDisposable
import java.lang.ref.WeakReference

internal class PositionAdapter(
    private val glide: RequestManager,
    private val repository: Repository?,
    private val clickListener: PositionClickListener
) :
    LoadingAdapter<Position,
    PositionViewHolder>(1),
    PositionViewHolder.PositionItemListener {

    interface PositionClickListener {
        fun onClick(item: Position)
    }

    private val compositeDisposable = CompositeDisposable()

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
        if (repository != null) {

            val weakViewHolder = WeakReference(viewHolder)

            repository.getPhotos(itemId).subscribeSuccess {
                it.photos.firstOrNull()?.getPhotoUrl()?.let { photoThumbnailUrl ->
                    weakViewHolder.get()?.apply {
                        setImageUrl(glide, photoThumbnailUrl, itemId)
                    }
                }
            }
        }
    }

    override fun onDetachedFromRecyclerView(recyclerView: RecyclerView) {
        compositeDisposable.dispose()
    }
}