package com.eulersbridge.isegoria.election;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.eulersbridge.isegoria.R;
import com.eulersbridge.isegoria.common.Constant;
import com.eulersbridge.isegoria.common.LoadingAdapter;
import com.eulersbridge.isegoria.models.Position;
import com.eulersbridge.isegoria.network.API;
import com.eulersbridge.isegoria.network.PhotosResponse;
import com.eulersbridge.isegoria.network.SimpleCallback;

import org.parceler.Parcels;

import java.lang.ref.WeakReference;

import retrofit2.Response;

class PositionAdapter extends LoadingAdapter<Position, PositionViewHolder> implements PositionViewHolder.PositionItemListener {

    final private WeakReference<Fragment> weakFragment;
    final private API api;

    PositionAdapter(@NonNull Fragment fragment, API api) {
        super(1);

        weakFragment = new WeakReference<>(fragment);
        this.api = api;
    }

    private boolean isValidFragment(@Nullable Fragment fragment) {
        return (fragment != null
                && fragment.getActivity() != null
                && !fragment.isDetached()
                && fragment.isAdded());
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
        Fragment fragment = weakFragment.get();
        if (!isValidFragment(fragment)) return;

        Bundle arguments = new Bundle();
        arguments.putParcelable(Constant.FRAGMENT_EXTRA_CANDIDATE_POSITION, Parcels.wrap(item));

        CandidatePositionFragment detailFragment = new CandidatePositionFragment();
        detailFragment.setArguments(arguments);

        fragment.getChildFragmentManager()
                .beginTransaction()
                .addToBackStack(null)
                .add(R.id.election_candidate_frame, detailFragment)
                .commit();
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
                        wrViewHolder.get().setImageURL(body.photos.get(0).thumbnailUrl, itemId);
                    }
                }
            });
        }
    }
}