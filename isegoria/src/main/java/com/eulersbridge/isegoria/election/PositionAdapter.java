package com.eulersbridge.isegoria.election;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.bumptech.glide.load.resource.bitmap.CenterCrop;
import com.bumptech.glide.load.resource.drawable.DrawableTransitionOptions;
import com.eulersbridge.isegoria.Constant;
import com.eulersbridge.isegoria.GlideApp;
import com.eulersbridge.isegoria.R;
import com.eulersbridge.isegoria.models.Position;
import com.eulersbridge.isegoria.network.API;
import com.eulersbridge.isegoria.network.PhotosResponse;
import com.eulersbridge.isegoria.network.SimpleCallback;
import com.eulersbridge.isegoria.utilities.ClickableViewHolder;
import com.eulersbridge.isegoria.utilities.TintTransformation;

import org.parceler.Parcels;

import java.util.ArrayList;
import java.util.List;

import retrofit2.Response;

class PositionAdapter extends RecyclerView.Adapter<PositionViewHolder> implements ClickableViewHolder.ClickListener {

    final private Fragment fragment;
    final private API api;

    final private List<Position> items = new ArrayList<>();

    PositionAdapter(Fragment fragment, API api) {
        this.fragment = fragment;
        this.api = api;
    }

    void replaceItems(@NonNull List<Position> newItems) {
        items.clear();
        items.addAll(newItems);
    }

    @Override
    public int getItemCount() { return items.size(); }

    private boolean isValidFragment() {
        return (fragment != null
                && fragment.getActivity() != null
                && !fragment.isDetached()
                && fragment.isAdded());
    }

    @Override
    public void onBindViewHolder(PositionViewHolder viewHolder, int index) {
        Position item = items.get(index);

        viewHolder.imageView.setContentDescription(item.name);
        viewHolder.imageView.setImageResource(R.color.grey);

        viewHolder.titleTextView.setText(item.name);

        api.getPhotos(item.id).enqueue(new SimpleCallback<PhotosResponse>() {
            @Override
            protected void handleResponse(Response<PhotosResponse> response) {
                PhotosResponse body = response.body();

                if (body != null && body.totalPhotos > 0 && isValidFragment()) {
                    GlideApp.with(fragment)
                            .load(body.photos.get(0).thumbnailUrl)
                            .placeholder(R.color.grey)
                            .transforms(new CenterCrop(), new TintTransformation())
                            .transition(DrawableTransitionOptions.withCrossFade())
                            .into(viewHolder.imageView);
                }
            }
        });
    }

    @Override
    public void onItemClick(RecyclerView.ViewHolder viewHolder, int position) {
        final Position item = items.get(position);

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
    public PositionViewHolder onCreateViewHolder(ViewGroup viewGroup, int viewType) {
        View itemView = LayoutInflater.from(viewGroup.getContext()).
                inflate(R.layout.election_position_grid_item, viewGroup, false);
        return new PositionViewHolder(itemView, this);
    }
}