package com.eulersbridge.isegoria.profile.badges

import android.support.v4.app.Fragment
import android.support.v7.widget.RecyclerView
import android.util.DisplayMetrics
import android.view.LayoutInflater
import android.view.ViewGroup
import com.eulersbridge.isegoria.IsegoriaApp
import com.eulersbridge.isegoria.R
import com.eulersbridge.isegoria.network.api.models.Badge
import com.eulersbridge.isegoria.onSuccess
import java.lang.ref.WeakReference
import java.util.*

internal class BadgeAdapter(fragment: Fragment) : RecyclerView.Adapter<BadgeViewHolder>() {

    private val weakFragment: WeakReference<Fragment> = WeakReference(fragment)

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

    private fun getImageIndex(fragment: Fragment): Int {
        val dm = fragment.resources.displayMetrics

        val dpi = dm.densityDpi
        return when (dpi) {
            DisplayMetrics.DENSITY_LOW -> 5
            DisplayMetrics.DENSITY_MEDIUM -> 4
            else -> 3
        }
    }

    private fun isValidFragment(fragment: Fragment?): Boolean {
        return (fragment != null
                && fragment.activity != null
                && !fragment.isDetached
                && fragment.isAdded)
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

        weakFragment.get()?.takeIf {
            it.activity != null && !it.isDetached && it.isAdded
        }?.let {
            val app = it.activity?.application as? IsegoriaApp
            val imageIndex = getImageIndex(it)
            val weakViewHolder = WeakReference(viewHolder)

            app?.api?.getPhotos(item.id)?.onSuccess {
                if (it.totalPhotos > imageIndex + 1) {
                    val innerViewHolder = weakViewHolder.get()

                    if (innerViewHolder != null) {
                        val imageUrl = it.photos?.get(imageIndex)?.thumbnailUrl

                        if (!imageUrl.isNullOrBlank())
                            innerViewHolder.loadItemImage(item.id, imageUrl!!)
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