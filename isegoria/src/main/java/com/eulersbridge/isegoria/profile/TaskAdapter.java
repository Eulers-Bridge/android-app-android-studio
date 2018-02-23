package com.eulersbridge.isegoria.profile;

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
import com.eulersbridge.isegoria.network.api.models.Task;
import com.eulersbridge.isegoria.network.api.responses.PhotosResponse;
import com.eulersbridge.isegoria.util.network.SimpleCallback;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.List;

import retrofit2.Response;

class TaskAdapter extends RecyclerView.Adapter<TaskViewHolder> {
    final private RequestManager glide;
    final private API api;
    final private List<Task> items = new ArrayList<>();

    TaskAdapter(@NonNull RequestManager glide, @NonNull API api) {
        this.glide = glide;
        this.api = api;
    }

    void setItems(@NonNull List<Task> newItems) {
        items.clear();
        items.addAll(newItems);
        notifyItemRangeChanged(0, newItems.size());
    }

    @Override
    public int getItemCount() { return items.size(); }

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
    public void onBindViewHolder(TaskViewHolder viewHolder, int index) {
        Task item = items.get(index);

        viewHolder.setItem(item);

        final int imageIndex = getImageIndex();
        final long itemId = item.id;

        WeakReference<TaskViewHolder> weakViewHolder = new WeakReference<>(viewHolder);

        api.getPhotos(itemId).enqueue(new SimpleCallback<PhotosResponse>() {
            @Override
            protected void handleResponse(Response<PhotosResponse> response) {
                PhotosResponse body = response.body();

                if (body != null
                        && body.totalPhotos > (imageIndex + 1)) {
                    TaskViewHolder innerViewHolder = weakViewHolder.get();

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
    public TaskViewHolder onCreateViewHolder(ViewGroup viewGroup, int viewType) {
        View itemView = LayoutInflater.from(viewGroup.getContext()).
                inflate(R.layout.profile_tasks_list_item, viewGroup, false);
        return new TaskViewHolder(itemView);
    }

    @Override
    public void onViewRecycled(TaskViewHolder holder) {
        holder.onRecycled();

        super.onViewRecycled(holder);
    }
}