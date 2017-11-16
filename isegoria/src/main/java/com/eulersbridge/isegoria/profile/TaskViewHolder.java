package com.eulersbridge.isegoria.profile;

import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.eulersbridge.isegoria.R;

class TaskViewHolder extends RecyclerView.ViewHolder {

    final ImageView imageView;
    final TextView nameTextView;
    final TextView xpTextView;

    TaskViewHolder(View view) {
        super(view);

        imageView = view.findViewById(R.id.task_list_image_view);
        nameTextView = view.findViewById(R.id.task_list_name_text_view);
        xpTextView = view.findViewById(R.id.task_list_xp_text_view);
    }
}
