package com.eulersbridge.isegoria.profile

import android.support.v7.widget.RecyclerView
import android.view.View
import android.widget.ImageView
import android.widget.TextView
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.bumptech.glide.load.resource.drawable.DrawableTransitionOptions
import com.eulersbridge.isegoria.GlideApp
import com.eulersbridge.isegoria.R
import com.eulersbridge.isegoria.network.api.models.Task

internal class TaskViewHolder(view: View) : RecyclerView.ViewHolder(view) {

    private var item: Task? = null

    private val imageView: ImageView = view.findViewById(R.id.task_list_image_view)
    private val nameTextView: TextView = view.findViewById(R.id.task_list_name_text_view)
    private val xpTextView: TextView = view.findViewById(R.id.task_list_xp_text_view)

    fun setItem(item: Task?) {
        this.item = item

        if (item == null) {
            nameTextView.text = null
            xpTextView.text = null

        } else {
            nameTextView.text = item.action
            xpTextView.text = nameTextView.context.getString(R.string.profile_tasks_task_xp, item.xpValue)
        }
    }

    fun loadItemImage(itemId: Long, imageUrl: String) {
        if (item?.id == itemId)
            GlideApp.with(imageView.context)
                    .load(imageUrl)
                    .diskCacheStrategy(DiskCacheStrategy.RESOURCE)
                    .transition(DrawableTransitionOptions.withCrossFade())
                    .into(imageView)
    }
}
