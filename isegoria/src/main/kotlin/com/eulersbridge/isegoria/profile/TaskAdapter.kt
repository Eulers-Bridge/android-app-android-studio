package com.eulersbridge.isegoria.profile

import android.content.res.Resources
import android.support.v7.recyclerview.extensions.ListAdapter
import android.support.v7.util.DiffUtil
import android.support.v7.widget.RecyclerView
import android.util.DisplayMetrics
import android.view.LayoutInflater
import android.view.ViewGroup
import com.bumptech.glide.RequestManager
import com.eulersbridge.isegoria.R
import com.eulersbridge.isegoria.data.Repository
import com.eulersbridge.isegoria.network.api.model.Task
import com.eulersbridge.isegoria.util.extension.subscribeSuccess
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.rxkotlin.addTo
import java.lang.ref.WeakReference

private class TaskDiffCallback : DiffUtil.ItemCallback<Task>() {
    override fun areItemsTheSame(oldItem: Task?, newItem: Task?): Boolean {
        return oldItem?.id == newItem?.id
    }

    override fun areContentsTheSame(oldItem: Task?, newItem: Task?): Boolean {
        return oldItem == newItem
    }
}

internal class TaskAdapter(
    private val glide: RequestManager,
    private val repository: Repository
) : ListAdapter<Task, TaskViewHolder>(TaskDiffCallback()) {

    private val compositeDisposable = CompositeDisposable()

    private fun getImageIndex(): Int {
        return when (Resources.getSystem().displayMetrics.densityDpi) {
            DisplayMetrics.DENSITY_LOW -> 5
            DisplayMetrics.DENSITY_MEDIUM -> 4
            else -> 3
        }
    }

    override fun onBindViewHolder(viewHolder: TaskViewHolder, index: Int) {
        val item = getItem(index)

        viewHolder.setItem(item)

        val imageIndex = getImageIndex()
        val itemId = item.id

        val weakViewHolder = WeakReference(viewHolder)
        repository.getPhotos(itemId)
                .subscribeSuccess {
                    if (it.totalPhotos > imageIndex + 1) {
                        val innerViewHolder = weakViewHolder.get()

                        if (innerViewHolder != null) {
                            val imageUrl = it.photos[imageIndex].getPhotoUrl()

                            if (!imageUrl.isNullOrBlank())
                                innerViewHolder.setImageUrl(glide, item.id, imageUrl!!)
                        }
                    }
                }
                .addTo(compositeDisposable)
    }

    override fun onViewRecycled(holder: TaskViewHolder) {
        holder.onRecycled()
    }

    override fun onCreateViewHolder(viewGroup: ViewGroup, viewType: Int): TaskViewHolder {
        val itemView = LayoutInflater.from(viewGroup.context)
            .inflate(R.layout.profile_tasks_list_item, viewGroup, false)
        return TaskViewHolder(itemView)
    }

    override fun onDetachedFromRecyclerView(recyclerView: RecyclerView) {
        compositeDisposable.dispose()
    }
}