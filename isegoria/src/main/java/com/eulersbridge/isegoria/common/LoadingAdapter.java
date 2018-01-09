package com.eulersbridge.isegoria.common;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.view.ViewGroup;

import java.util.ArrayList;
import java.util.List;

/**
 *
 * @param <I> The type of item to be held by the adapter.
 * @param <VH> A view holder class (extending ItemViewHolder) that updates its layout with a given
 *            item of type I.
 */
public class LoadingAdapter<I, VH extends LoadingAdapter.ItemViewHolder<I>> extends RecyclerView.Adapter<VH> {

    abstract public static class ItemViewHolder<T> extends RecyclerView.ViewHolder {
        protected ItemViewHolder(View itemView) {
            super(itemView);
        }

        protected abstract void setItem(@Nullable T item);

        protected abstract void onRecycled();
    }

    private final List<I> items = new ArrayList<>();

    private boolean loading = true;

    /**
     * The number of placeholder items to show while loading
     */
    private int loadingItemCount = 1;

    protected LoadingAdapter(int loadingItemCount) {
        this.loadingItemCount = loadingItemCount;
    }

    public void setLoading(boolean loading) {
        this.loading = loading;
    }

    private boolean isLoading() {
        return loading;
    }

    private int getLoadingItemCount() {
        return loadingItemCount;
    }

    public void replaceItems(@NonNull List<I> newItems) {
        if (newItems.size() == 0) {
            int oldItemCount = items.size();

            items.clear();

            notifyItemRangeRemoved(0, oldItemCount);

        } else {
            items.clear();
            items.addAll(newItems);
            notifyItemRangeChanged(0, newItems.size());
        }
    }

    protected List<I> getItems() {
        return items;
    }

    @Override
    public int getItemCount() {
        return isLoading()? getLoadingItemCount() : items.size();
    }

    @Override
    public void onBindViewHolder(VH viewHolder, int position) {
        viewHolder.setItem(isLoading()? null : items.get(position));
    }

    @Override
    public VH onCreateViewHolder(ViewGroup parent, int viewType) {
        throw new RuntimeException("Stub! Override this method.");
    }
}