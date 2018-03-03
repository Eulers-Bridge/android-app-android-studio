package com.eulersbridge.isegoria.profile.badges

import android.content.res.Resources
import android.support.v7.widget.RecyclerView
import android.util.DisplayMetrics
import android.view.LayoutInflater
import android.view.ViewGroup
import com.bumptech.glide.RequestManager
import com.eulersbridge.isegoria.R
import com.eulersbridge.isegoria.network.api.API
import com.eulersbridge.isegoria.network.api.models.Badge
import com.eulersbridge.isegoria.onSuccess
import java.lang.ref.WeakReference
import java.util.*

internal class BadgeAdapter(
    private val glide: RequestManager,
    private val api: API
) : RecyclerView.Adapter<BadgeViewHolder>() {

    private val completedItems = ArrayList<Badge>()
    private val remainingItems = ArrayList<Badge>()

    fun replaceCompletedItems(newItems: List<Badge>) {
        completedItems.clear()
        completedItems.addAll(newItems)
        notifyItemRangeChanged(0, newItems.size)
    }

    fun replaceRemainingItems(newItems: List<Badge>) {
        remainingItems.clear()
        remainingItems.addAll(newItems)

        var remainingItemsStartIndex = remainingItems.size - 1

        // remainingItemsStartIndex < 0 if remainingItems.size() == 0
        if (remainingItemsStartIndex < 0)
            remainingItemsStartIndex = 0

        // Remaining items show after completed items
        notifyItemRangeChanged(remainingItemsStartIndex, newItems.size)
    }

    override fun getItemCount() = completedItems.size + remainingItems.size

    private fun getImageIndex(): Int {
        return when (Resources.getSystem().displayMetrics.densityDpi) {
            DisplayMetrics.DENSITY_LOW -> 5
            DisplayMetrics.DENSITY_MEDIUM -> 4
            else -> 3
        }
    }

    override fun onBindViewHolder(viewHolder: BadgeViewHolder, index: Int) {

        val item: Badge
        var completed = false

        when {
            (index < completedItems.size) -> {
                item = completedItems[index]
                completed = true
            }
            (index < remainingItems.size) -> item = remainingItems[index]
            else -> {
                viewHolder.setItem(null, false)
                return
            }
        }

        viewHolder.setItem(item, completed)

        val imageIndex = getImageIndex()
        val itemId = item.id

        val weakViewHolder = WeakReference(viewHolder)

        api.getPhotos(itemId).onSuccess {
            if (it.totalPhotos > imageIndex + 1) {
                val innerViewHolder = weakViewHolder.get()

                if (innerViewHolder != null) {
                    it.photos?.get(imageIndex)?.thumbnailUrl
                        ?.takeUnless { it.isBlank() }
                        ?.also {
                            innerViewHolder.setImageUrl(glide, item.id, it)
                        }
                }
            }
        }
    }

    override fun onCreateViewHolder(viewGroup: ViewGroup, viewType: Int): BadgeViewHolder {
        val itemView = LayoutInflater.from(viewGroup.context)
            .inflate(R.layout.profile_badges_list_item, viewGroup, false)
        return BadgeViewHolder(itemView)
    }
}