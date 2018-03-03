package com.eulersbridge.isegoria.profile

import android.content.res.Resources
import android.support.v7.widget.RecyclerView
import android.util.DisplayMetrics
import android.view.LayoutInflater
import android.view.ViewGroup
import com.bumptech.glide.RequestManager
import com.eulersbridge.isegoria.R
import com.eulersbridge.isegoria.network.api.API
import com.eulersbridge.isegoria.network.api.models.Task
import com.eulersbridge.isegoria.onSuccess
import java.lang.ref.WeakReference
import java.util.*

internal class TaskAdapter(
    private val glide: RequestManager,
    private val api: API
) : RecyclerView.Adapter<TaskViewHolder>() {
    private val items = ArrayList<Task>()

    fun setItems(newItems: List<Task>) {
        items.clear()
        items.addAll(newItems)
        notifyItemRangeChanged(0, newItems.size)
    }

    override fun getItemCount() = items.size

    private fun getImageIndex(): Int {
        return when (Resources.getSystem().displayMetrics.densityDpi) {
            DisplayMetrics.DENSITY_LOW -> 5
            DisplayMetrics.DENSITY_MEDIUM -> 4
            else -> 3
        }
    }

    override fun onBindViewHolder(viewHolder: TaskViewHolder, index: Int) {
        val item = items[index]

        viewHolder.setItem(item)

        val imageIndex = getImageIndex()
        val itemId = item.id

        val weakViewHolder = WeakReference(viewHolder)
        api.getPhotos(itemId).onSuccess {
            if (it.totalPhotos > imageIndex + 1) {
                val innerViewHolder = weakViewHolder.get()

                if (innerViewHolder != null) {
                    val imageUrl = it.photos?.get(imageIndex)?.thumbnailUrl

                    if (!imageUrl.isNullOrBlank())
                        innerViewHolder.setImageUrl(glide, item.id, imageUrl!!)
                }
            }
        }
    }

    override fun onViewRecycled(holder: TaskViewHolder) {
        holder.onRecycled()

        super.onViewRecycled(holder)
    }

    override fun onCreateViewHolder(viewGroup: ViewGroup, viewType: Int): TaskViewHolder {
        val itemView = LayoutInflater.from(viewGroup.context)
            .inflate(R.layout.profile_tasks_list_item, viewGroup, false)
        return TaskViewHolder(itemView)
    }
}