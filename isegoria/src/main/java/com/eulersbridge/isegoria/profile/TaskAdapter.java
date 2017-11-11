package com.eulersbridge.isegoria.profile;

import android.support.v4.app.Fragment;
import android.support.v7.widget.RecyclerView;
import android.util.DisplayMetrics;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.bumptech.glide.signature.ObjectKey;
import com.eulersbridge.isegoria.GlideApp;
import com.eulersbridge.isegoria.Isegoria;
import com.eulersbridge.isegoria.R;
import com.eulersbridge.isegoria.models.Task;
import com.eulersbridge.isegoria.network.PhotosResponse;
import com.eulersbridge.isegoria.network.SimpleCallback;

import java.util.ArrayList;
import java.util.List;

import retrofit2.Response;

public class TaskAdapter extends RecyclerView.Adapter<TaskViewHolder> {
    private final Fragment fragment;
    final private List<Task> items = new ArrayList<>();

    public TaskAdapter(Fragment fragment) {
        this.fragment = fragment;
    }

    public void replaceItems(List<Task> newItems) {
        items.clear();
        items.addAll(newItems);
    }

    @Override
    public int getItemCount() { return items.size(); }

    private int getImageIndex() {
        DisplayMetrics dm = fragment.getResources().getDisplayMetrics();

        int dpi = dm.densityDpi;
        if (dpi == DisplayMetrics.DENSITY_LOW) {
            return 11;
        } else if (dpi == DisplayMetrics.DENSITY_MEDIUM) {
            return 10;
        } else {
            return 9;
        }
    }

    @Override
    public void onBindViewHolder(TaskViewHolder viewHolder, int index) {
        final Task item = items.get(index);

        viewHolder.nameTextView.setText(item.action);
        viewHolder.xpTextView.setText(fragment.getContext().getString(R.string.profile_tasks_task_xp, item.xpValue));

        CharSequence oldContentDescription = viewHolder.imageView.getContentDescription();

        boolean newImageRequired = (oldContentDescription != null  && !oldContentDescription.toString().equals(item.action)
                || oldContentDescription == null);

        if (fragment != null
                && fragment.getActivity() != null
                && newImageRequired) {
            Isegoria isegoria = (Isegoria)fragment.getActivity().getApplication();

            if (isegoria != null) {
                isegoria.getAPI().getPhotos(item.id).enqueue(new SimpleCallback<PhotosResponse>() {
                    @Override
                    protected void handleResponse(Response<PhotosResponse> response) {
                        PhotosResponse body = response.body();
                        if (body != null && body.totalPhotos > (getImageIndex() + 1)) {

                            String url = body.photos.get(getImageIndex()).thumbnailUrl;

                            GlideApp.with(fragment)
                                    .load(url)
                                    .diskCacheStrategy(DiskCacheStrategy.RESOURCE)
                                    .signature(new ObjectKey(url))
                                    .into(viewHolder.imageView);
                        }
                    }
                });
            }
        }

        viewHolder.imageView.setContentDescription(item.action);
    }

    @Override
    public TaskViewHolder onCreateViewHolder(ViewGroup viewGroup, int viewType) {
        View itemView = LayoutInflater.from(viewGroup.getContext()).
                inflate(R.layout.profile_tasks_partial_list_item, viewGroup, false);
        return new TaskViewHolder(itemView);
    }
}