package com.eulersbridge.isegoria.profile;

import android.graphics.Color;
import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
import android.support.v7.widget.RecyclerView;
import android.util.DisplayMetrics;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.bumptech.glide.load.resource.drawable.DrawableTransitionOptions;
import com.eulersbridge.isegoria.GlideApp;
import com.eulersbridge.isegoria.Isegoria;
import com.eulersbridge.isegoria.R;
import com.eulersbridge.isegoria.models.Badge;
import com.eulersbridge.isegoria.network.PhotosResponse;
import com.eulersbridge.isegoria.network.SimpleCallback;

import java.util.ArrayList;
import java.util.List;

import retrofit2.Response;

class BadgeAdapter extends RecyclerView.Adapter<BadgeViewHolder> {
    private final Fragment fragment;

    final private List<Badge> completedItems = new ArrayList<>();
    final private List<Badge> remainingItems = new ArrayList<>();


    BadgeAdapter(Fragment fragment) {
        this.fragment = fragment;
    }

    void replaceCompletedItems(@NonNull List<Badge> newItems) {
        completedItems.clear();
        completedItems.addAll(newItems);
    }

    void replaceRemainingItems(@NonNull List<Badge> newItems) {
        remainingItems.clear();
        remainingItems.addAll(newItems);
    }

    @Override
    public int getItemCount() {
        return completedItems.size() + remainingItems.size();
    }

    private int getImageIndex() {
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
    public void onBindViewHolder(BadgeViewHolder viewHolder, int index) {

        Badge item;
        boolean completed = false;

        if (index < completedItems.size()) {
            item = completedItems.get(index);
            completed = true;

        } else if (index < remainingItems.size()) {
            item = remainingItems.get(index);

        } else {
            viewHolder.nameTextView.setText(null);
            viewHolder.descriptionTextView.setText(null);
            viewHolder.imageView.setImageDrawable(null);
            viewHolder.imageView.setContentDescription(null);
            return;
        }

        if (!completed) {
            viewHolder.imageView.setColorFilter(Color.argb(125, 35, 35, 35));
        } else {
            viewHolder.imageView.clearColorFilter();
        }

        viewHolder.nameTextView.setText(item.name);
        viewHolder.descriptionTextView.setText(item.description);

        CharSequence oldContentDescription = viewHolder.imageView.getContentDescription();

        boolean newImageRequired = (oldContentDescription != null  && !oldContentDescription.toString().equals(item.name)
                || oldContentDescription == null);

        if (fragment != null
                && fragment.getActivity() != null
                && newImageRequired) {
            Isegoria isegoria = (Isegoria)fragment.getActivity().getApplication();

            if (isegoria != null) {
                viewHolder.imageView.setImageDrawable(null);

                int imageIndex = getImageIndex();

                isegoria.getAPI().getPhotos(item.id).enqueue(new SimpleCallback<PhotosResponse>() {
                    @Override
                    protected void handleResponse(Response<PhotosResponse> response) {
                        PhotosResponse body = response.body();
                        if (body != null && body.totalPhotos > (imageIndex + 1)) {

                            String url = body.photos.get(imageIndex).thumbnailUrl;

                            GlideApp.with(fragment)
                                    .load(url)
                                    .diskCacheStrategy(DiskCacheStrategy.RESOURCE)
                                    .transition(DrawableTransitionOptions.withCrossFade())
                                    .into(viewHolder.imageView);
                        }
                    }
                });
            }
        }

        viewHolder.imageView.setContentDescription(item.name);
    }

    @Override
    public BadgeViewHolder onCreateViewHolder(ViewGroup viewGroup, int viewType) {
        View itemView = LayoutInflater.from(viewGroup.getContext()).
                inflate(R.layout.profile_badges_list_item, viewGroup, false);
        return new BadgeViewHolder(itemView);
    }
}