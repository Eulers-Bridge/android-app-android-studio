package com.eulersbridge.isegoria.profile;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.bumptech.glide.load.resource.drawable.DrawableTransitionOptions;
import com.eulersbridge.isegoria.GlideApp;
import com.eulersbridge.isegoria.network.api.models.Task;
import com.eulersbridge.isegoria.R;

class TaskViewHolder extends RecyclerView.ViewHolder {

    private Task item;

    final private ImageView imageView;
    final private TextView nameTextView;
    final private TextView xpTextView;

    TaskViewHolder(View view) {
        super(view);

        imageView = view.findViewById(R.id.task_list_image_view);
        nameTextView = view.findViewById(R.id.task_list_name_text_view);
        xpTextView = view.findViewById(R.id.task_list_xp_text_view);
    }

    void setItem(@Nullable Task item) {
        this.item = item;

        if (item == null) {
            nameTextView.setText(null);
            xpTextView.setText(null);

        } else {
            nameTextView.setText(item.action);
            xpTextView.setText(nameTextView.getContext().getString(R.string.profile_tasks_task_xp, item.xpValue));
        }
    }

    void loadItemImage(long itemId, @NonNull String imageUrl) {
        if (item != null && item.id == itemId) {
            GlideApp.with(imageView.getContext())
                    .load(imageUrl)
                    .diskCacheStrategy(DiskCacheStrategy.RESOURCE)
                    .transition(DrawableTransitionOptions.withCrossFade())
                    .into(imageView);
        }
    }
}
