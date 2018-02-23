package com.eulersbridge.isegoria.profile.badges;

import android.content.res.Resources;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.util.DisplayMetrics;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.bumptech.glide.RequestManager;
import com.eulersbridge.isegoria.R;
import com.eulersbridge.isegoria.network.api.API;
import com.eulersbridge.isegoria.network.api.models.Badge;
import com.eulersbridge.isegoria.network.api.responses.PhotosResponse;
import com.eulersbridge.isegoria.util.network.SimpleCallback;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.List;

import retrofit2.Response;

class BadgeAdapter extends RecyclerView.Adapter<BadgeViewHolder> {

    final private List<Badge> completedItems = new ArrayList<>();
    final private List<Badge> remainingItems = new ArrayList<>();

    final private RequestManager glide;
    final private API api;

    BadgeAdapter(@NonNull RequestManager glide, @NonNull API api) {
        this.glide = glide;
        this.api = api;
    }

    void replaceCompletedItems(@NonNull List<Badge> newItems) {
        completedItems.clear();
        completedItems.addAll(newItems);
        notifyItemRangeChanged(0, newItems.size());
    }

    void replaceRemainingItems(@NonNull List<Badge> newItems) {
        remainingItems.clear();
        remainingItems.addAll(newItems);

        int remainingItemsStartIndex = remainingItems.size() - 1;

        // remainingItemsStartIndex < 0 if remainingItems.size() == 0
        if (remainingItemsStartIndex < 0)
                remainingItemsStartIndex = 0;

        // Remaining items show after completed items
        notifyItemRangeChanged(remainingItemsStartIndex, newItems.size());
    }

    @Override
    public int getItemCount() {
        return completedItems.size() + remainingItems.size();
    }

    private int getImageIndex() {
        switch (Resources.getSystem().getDisplayMetrics().densityDpi) {
            case DisplayMetrics.DENSITY_LOW:
                return 5;
            case DisplayMetrics.DENSITY_MEDIUM:
                return 4;
            default:
                return 3;
        }
    }

    @Override
    public void onBindViewHolder(BadgeViewHolder viewHolder, int index) {

        Badge item;
        boolean completed = false;

        if (index < completedItems.size()) {
            item = completedItems.get(index);
            completed = true;

        } else if (index < remainingItems.size()) {
            item = remainingItems.get(index);

        } else {
            viewHolder.setItem(null, false);
            return;
        }

        viewHolder.setItem(item, completed);

        final int imageIndex = getImageIndex();
        final long itemId = item.id;

        WeakReference<BadgeViewHolder> weakViewHolder = new WeakReference<>(viewHolder);

        api.getPhotos(itemId).enqueue(new SimpleCallback<PhotosResponse>() {
            @Override
            protected void handleResponse(Response<PhotosResponse> response) {
                PhotosResponse body = response.body();

                if (body != null
                        && body.totalPhotos > (imageIndex + 1)) {
                    BadgeViewHolder innerViewHolder = weakViewHolder.get();

                    if (innerViewHolder != null) {
                        String imageUrl = body.photos.get(imageIndex).thumbnailUrl;

                        if (!TextUtils.isEmpty(imageUrl))
                            innerViewHolder.setImageUrl(glide, itemId, imageUrl);
                    }
                }
            }
        });
    }

    @Override
    public BadgeViewHolder onCreateViewHolder(ViewGroup viewGroup, int viewType) {
        View itemView = LayoutInflater.from(viewGroup.getContext()).
                inflate(R.layout.profile_badges_list_item, viewGroup, false);
        return new BadgeViewHolder(itemView);
    }

    @Override
    public void onViewRecycled(BadgeViewHolder holder) {
        holder.onRecycled();

        super.onViewRecycled(holder);
    }
}