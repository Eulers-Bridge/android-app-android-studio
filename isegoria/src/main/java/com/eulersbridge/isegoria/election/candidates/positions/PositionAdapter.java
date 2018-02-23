package com.eulersbridge.isegoria.election.candidates.positions;

import android.support.annotation.NonNull;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.bumptech.glide.RequestManager;
import com.eulersbridge.isegoria.R;
import com.eulersbridge.isegoria.network.api.API;
import com.eulersbridge.isegoria.network.api.models.Position;
import com.eulersbridge.isegoria.network.api.responses.PhotosResponse;
import com.eulersbridge.isegoria.util.network.SimpleCallback;
import com.eulersbridge.isegoria.util.ui.LoadingAdapter;

import java.lang.ref.WeakReference;

import retrofit2.Response;

class PositionAdapter extends LoadingAdapter<Position, PositionViewHolder> implements PositionViewHolder.PositionItemListener {

    interface PositionClickListener {
        void onClick(Position item);
    }

    final private PositionClickListener clickListener;
    final private RequestManager glide;
    final private API api;

    PositionAdapter(@NonNull RequestManager glide, API api, @NonNull PositionClickListener clickListener) {
        super(1);

        this.glide = glide;
        this.api = api;
        this.clickListener = clickListener;
    }

    @Override
    public void onViewDetachedFromWindow(PositionViewHolder holder) {
        super.onViewDetachedFromWindow(holder);
    }

    @Override
    public PositionViewHolder onCreateViewHolder(ViewGroup viewGroup, int viewType) {
        View itemView = LayoutInflater.from(viewGroup.getContext()).
                inflate(R.layout.election_position_grid_item, viewGroup, false);
        return new PositionViewHolder(itemView, this);
    }

    @Override
    public void onClick(Position item) {
        clickListener.onClick(item);
    }

    @Override
    public void getPhoto(PositionViewHolder viewHolder, final long itemId) {
        if (api != null) {

            WeakReference<PositionViewHolder> wrViewHolder = new WeakReference<>(viewHolder);

            api.getPhotos(itemId).enqueue(new SimpleCallback<PhotosResponse>() {
                @Override
                protected void handleResponse(Response<PhotosResponse> response) {
                    PhotosResponse body = response.body();

                    if (body != null && body.totalPhotos > 0 && wrViewHolder.get() != null) {
                        wrViewHolder.get().setImageUrl(glide, body.photos.get(0).thumbnailUrl, itemId);
                    }
                }
            });
        }
    }
}