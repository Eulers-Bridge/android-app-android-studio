package com.eulersbridge.isegoria.util.ui

import android.support.v7.widget.RecyclerView
import android.util.Log
import android.view.View
import android.view.ViewGroup
import com.eulersbridge.isegoria.BuildConfig
import java.util.*

open class LoadingAdapter<I, VH : LoadingAdapter.ItemViewHolder<I>>
protected constructor(private val loadingItemCount: Int) : RecyclerView.Adapter<VH>() {

    internal val items = ArrayList<I>()
    internal var isLoading = true

    abstract class ItemViewHolder<in T> protected constructor(itemView: View) :
        RecyclerView.ViewHolder(itemView) {
        abstract fun setItem(item: T?)
        abstract fun onRecycled()
    }

    fun replaceItems(newItems: List<I>) {
        if (newItems.isEmpty()) {
            val oldItemCount = items.size
            items.clear()
            notifyItemRangeRemoved(0, oldItemCount)

        } else {
            items.clear()
            items.addAll(newItems)
            notifyItemRangeChanged(0, newItems.size)
        }

        if (isLoading && BuildConfig.DEBUG)
            Log.w(javaClass.simpleName, "replaceItems() called whilst still in loading state.")
    }

    protected fun getItems(): List<I> = items

    override fun getItemCount() = if (isLoading) loadingItemCount else items.size

    override fun onBindViewHolder(viewHolder: VH, position: Int) {
        viewHolder.setItem(if (isLoading) null else items[position])
    }

    override fun onViewRecycled(viewHolder: VH) {
        viewHolder.onRecycled()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): VH {
        throw RuntimeException("Stub! Override this method.")
    }
}