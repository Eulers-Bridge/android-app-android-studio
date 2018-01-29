package com.eulersbridge.isegoria.profile

import android.support.v4.app.Fragment
import android.support.v7.widget.RecyclerView
import android.util.DisplayMetrics
import android.view.LayoutInflater
import android.view.ViewGroup
import com.eulersbridge.isegoria.IsegoriaApp
import com.eulersbridge.isegoria.R
import com.eulersbridge.isegoria.network.api.models.Task
import com.eulersbridge.isegoria.network.api.responses.PhotosResponse
import com.eulersbridge.isegoria.util.network.SimpleCallback
import retrofit2.Response
import java.lang.ref.WeakReference
import java.util.*

internal class TaskAdapter(fragment: Fragment) : RecyclerView.Adapter<TaskViewHolder>() {
    private val weakFragment: WeakReference<Fragment> = WeakReference(fragment)
    private val items = ArrayList<Task>()

    fun setItems(newItems: List<Task>) {
        items.clear()
        items.addAll(newItems)
        notifyItemRangeChanged(0, newItems.size)
    }

    override fun getItemCount() = items.size

    private fun isValidFragment(fragment: Fragment?): Boolean {
        return (fragment != null
                && fragment.activity != null
                && !fragment.isDetached
                && fragment.isAdded)
    }

    private fun getImageIndex(fragment: Fragment): Int {
        val dpi = fragment.resources.displayMetrics.densityDpi
        return when (dpi) {
            DisplayMetrics.DENSITY_LOW -> 5
            DisplayMetrics.DENSITY_MEDIUM -> 4
            else -> 3
        }
    }

    override fun onBindViewHolder(viewHolder: TaskViewHolder, index: Int) {
        val item = items[index]

        viewHolder.setItem(item)

        val fragment = weakFragment.get()

        if (isValidFragment(fragment)) {

            val app = fragment!!.activity!!.application as IsegoriaApp?

            if (app != null) {
                val imageIndex = getImageIndex(fragment)

                val weakViewHolder = WeakReference(viewHolder)

                app.api.getPhotos(item.id).enqueue(object : SimpleCallback<PhotosResponse>() {
                    override fun handleResponse(response: Response<PhotosResponse>) {
                        val body = response.body()

                        if (body != null && body.totalPhotos > imageIndex + 1) {
                            val innerViewHolder = weakViewHolder.get()

                            if (innerViewHolder != null) {
                                val imageUrl = body.photos?.get(imageIndex)?.thumbnailUrl

                                if (!imageUrl.isNullOrBlank())
                                    innerViewHolder.loadItemImage(item.id, imageUrl!!)
                            }
                        }
                    }
                })
            }
        }
    }

    override fun onCreateViewHolder(viewGroup: ViewGroup, viewType: Int): TaskViewHolder {
        val itemView = LayoutInflater.from(viewGroup.context)
            .inflate(R.layout.profile_tasks_list_item, viewGroup, false)
        return TaskViewHolder(itemView)
    }
}