package com.eulersbridge.isegoria.profile;

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
import com.eulersbridge.isegoria.network.api.models.Task;
import com.eulersbridge.isegoria.util.network.SimpleCallback;
import com.eulersbridge.isegoria.R;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.List;

import retrofit2.Response;

class TaskAdapter extends RecyclerView.Adapter<TaskViewHolder> {
    private final WeakReference<Fragment> weakFragment;
    final private List<Task> items = new ArrayList<>();

    TaskAdapter(@NonNull Fragment fragment) {
        weakFragment = new WeakReference<>(fragment);
    }

    public void replaceItems(@NonNull List<Task> newItems) {
        items.clear();
        items.addAll(newItems);
        notifyItemRangeChanged(0, newItems.size());
    }

    @Override
    public int getItemCount() { return items.size(); }

    private boolean isValidFragment(@Nullable Fragment fragment) {
        return (fragment != null
                && fragment.getActivity() != null
                && !fragment.isDetached()
                && fragment.isAdded());
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

    @Override
    public void onBindViewHolder(TaskViewHolder viewHolder, int index) {
        Task item = items.get(index);

        viewHolder.setItem(item);

        Fragment fragment = weakFragment.get();

        if (isValidFragment(fragment)) {
            //noinspection ConstantConditions
            IsegoriaApp isegoriaApp = (IsegoriaApp)fragment.getActivity().getApplication();

            if (isegoriaApp != null) {
                int imageIndex = getImageIndex(fragment);

                final long itemId = item.id;

                WeakReference<TaskViewHolder> weakViewHolder = new WeakReference<>(viewHolder);

                isegoriaApp.getAPI().getPhotos(item.id).enqueue(new SimpleCallback<PhotosResponse>() {
                    @Override
                    protected void handleResponse(Response<PhotosResponse> response) {
                        PhotosResponse body = response.body();

                        if (body != null
                                && body.totalPhotos > (imageIndex + 1)) {
                            TaskViewHolder innerViewHolder = weakViewHolder.get();

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
    public TaskViewHolder onCreateViewHolder(ViewGroup viewGroup, int viewType) {
        View itemView = LayoutInflater.from(viewGroup.getContext()).
                inflate(R.layout.profile_tasks_list_item, viewGroup, false);
        return new TaskViewHolder(itemView);
    }
}