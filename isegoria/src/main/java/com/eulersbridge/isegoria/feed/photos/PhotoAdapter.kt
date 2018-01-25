package com.eulersbridge.isegoria.feed.photos

import android.content.Context
import android.content.Intent
import android.view.LayoutInflater
import android.view.ViewGroup
import com.eulersbridge.isegoria.R
import com.eulersbridge.isegoria.network.api.models.Photo
import com.eulersbridge.isegoria.util.Constants
import com.eulersbridge.isegoria.util.ui.LoadingAdapter
import org.parceler.Parcels

internal class PhotoAdapter : LoadingAdapter<Photo, PhotoViewHolder>(0), PhotoViewHolder.ClickListener {

    override fun onClick(context: Context, position: Int) {
        val activityIntent = Intent(context, PhotoDetailActivity::class.java)
        activityIntent.putExtra(Constants.ACTIVITY_EXTRA_PHOTOS, Parcels.wrap(items))
        activityIntent.putExtra(Constants.ACTIVITY_EXTRA_PHOTOS_POSITION, position)

        context.startActivity(activityIntent)
    }

    override fun onCreateViewHolder(viewGroup: ViewGroup, viewType: Int): PhotoViewHolder {
        val itemView = LayoutInflater.from(viewGroup.context).inflate(R.layout.photo_grid_item, viewGroup, false)
        return PhotoViewHolder(itemView, this)
    }
}