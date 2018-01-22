package com.eulersbridge.isegoria.profile.badges;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.util.DisplayMetrics;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.eulersbridge.isegoria.IsegoriaApp;
import com.eulersbridge.isegoria.network.api.responses.PhotosResponse;
import com.eulersbridge.isegoria.network.api.models.Badge;
import com.eulersbridge.isegoria.util.network.SimpleCallback;
import com.eulersbridge.isegoria.R;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.List;

import retrofit2.Response;

class BadgeAdapter extends RecyclerView.Adapter<BadgeViewHolder> {
    private final WeakReference<Fragment> weakFragment;

    final private List<Badge> completedItems = new ArrayList<>();
    final private List<Badge> remainingItems = new ArrayList<>();

    BadgeAdapter(@NonNull Fragment fragment) {
        weakFragment = new WeakReference<>(fragment);
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

    private int getImageIndex(@NonNull Fragment fragment) {
        DisplayMetrics dm = fragment.getResources().getDisplayMetrics();

        int dpi = dm.densityDpi;
        if (dpi == DisplayMetrics.DENSITY_LOW) {
            return 5;

        } else if (dpi == DisplayMetrics.DENSITY_MEDIUM) {
            return 4;

        } else {
            return 3;
        }
    }

    private boolean isValidFragment(@Nullable Fragment fragment) {
        return (fragment != null
                && fragment.getActivity() != null
                && !fragment.isDetached()
                && fragment.isAdded());
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

        Fragment fragment = weakFragment.get();

        if (isValidFragment(fragment)) {
            //noinspection ConstantConditions
            IsegoriaApp isegoriaApp = (IsegoriaApp)fragment.getActivity().getApplication();

            if (isegoriaApp != null) {
                int imageIndex = getImageIndex(fragment);

                final long itemId = item.id;

                WeakReference<BadgeViewHolder> weakViewHolder = new WeakReference<>(viewHolder);

                isegoriaApp.getAPI().getPhotos(item.id).enqueue(new SimpleCallback<PhotosResponse>() {
                    @Override
                    protected void handleResponse(Response<PhotosResponse> response) {
                        PhotosResponse body = response.body();

                        if (body != null
                                && body.totalPhotos > (imageIndex + 1)) {
                            BadgeViewHolder innerViewHolder = weakViewHolder.get();

                            if (innerViewHolder != null) {
                                String imageUrl = body.photos.get(imageIndex).thumbnailUrl;

                                if (!TextUtils.isEmpty(imageUrl))
                                    innerViewHolder.loadItemImage(itemId, imageUrl);
                            }
                        }
                    }
                });
            }
        }
    }

    @Override
    public BadgeViewHolder onCreateViewHolder(ViewGroup viewGroup, int viewType) {
        View itemView = LayoutInflater.from(viewGroup.getContext()).
                inflate(R.layout.profile_badges_list_item, viewGroup, false);
        return new BadgeViewHolder(itemView);
    }
}