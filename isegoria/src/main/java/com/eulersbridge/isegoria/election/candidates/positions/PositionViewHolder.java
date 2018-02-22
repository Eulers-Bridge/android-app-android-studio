package com.eulersbridge.isegoria.election.candidates.positions;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.text.TextUtils;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.RequestManager;
import com.bumptech.glide.load.resource.drawable.DrawableTransitionOptions;
import com.eulersbridge.isegoria.R;
import com.eulersbridge.isegoria.network.api.models.Position;
import com.eulersbridge.isegoria.util.ui.LoadingAdapter;

class PositionViewHolder extends LoadingAdapter.ItemViewHolder<Position> {

    interface PositionItemListener {
        void onClick(Position item);
        void getPhoto(PositionViewHolder viewHolder, long itemId);
    }

    final private PositionItemListener listener;

    private Position item;
    final private ImageView imageView;
    final private TextView titleTextView;

    private @Nullable RequestManager glide;

    PositionViewHolder(View itemView, PositionItemListener listener) {
        super(itemView);

        this.listener = listener;

        itemView.setOnClickListener(view -> {
            if (listener != null)
                listener.onClick(item);
        });

        imageView = itemView.findViewById(R.id.election_position_grid_item_image_view);
        titleTextView = itemView.findViewById(R.id.election_position_grid_item_title_text_view);
    }

    @Override
    protected void setItem(@Nullable Position item) {
        this.item = item;

        imageView.setImageResource(R.color.lightGrey);

        if (item == null) {
            titleTextView.setText(null);
            imageView.setImageDrawable(null);

        } else {
            titleTextView.setText(item.name);

            if (listener != null)
                listener.getPhoto(this, item.id);
        }
    }

    @Override
    protected void onRecycled() {
        if (glide != null)
            glide.clear(imageView);
    }

    void setImageUrl(@NonNull RequestManager glide, @Nullable String imageURL, long itemId) {
        if (itemId == item.id && !TextUtils.isEmpty(imageURL)) {
            this.glide = glide;

            glide.load(imageURL)
                    .transition(DrawableTransitionOptions.withCrossFade())
                    .into(imageView);
        }
    }
}