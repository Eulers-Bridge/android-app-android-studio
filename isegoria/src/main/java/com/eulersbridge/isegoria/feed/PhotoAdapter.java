package com.eulersbridge.isegoria.feed;

import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.bumptech.glide.signature.ObjectKey;
import com.eulersbridge.isegoria.GlideApp;
import com.eulersbridge.isegoria.R;
import com.eulersbridge.isegoria.models.Photo;
import com.eulersbridge.isegoria.utilities.RecyclerViewItemClickListener;

import java.util.ArrayList;
import java.util.List;

class PhotoAdapter extends RecyclerView.Adapter<PhotoViewHolder> implements RecyclerViewItemClickListener {
    final private Fragment fragment;
    final private PhotoViewPagerFragment pagerFragment;
    final private List<Photo> items = new ArrayList<>();

    PhotoAdapter(Fragment fragment) {
        this.fragment = fragment;

        this.pagerFragment = new PhotoViewPagerFragment();
        pagerFragment.setPhotoAdapter(fragment, this);
    }

    void replaceItems(List<Photo> newItems) {
        items.clear();
        items.addAll(newItems);
    }

    Photo getItem(int index) {
        return items.get(index);
    }

    @Override
    public int getItemCount() { return items.size(); }

    @Override
    public void onBindViewHolder(PhotoViewHolder viewHolder, int index) {
        final Photo item = items.get(index);

        String photoUrl = item.thumbnailUrl;

        viewHolder.imageView.setBackgroundResource(R.color.grey);
        viewHolder.imageView.setContentDescription(item.title);

        if (!TextUtils.isEmpty(photoUrl)) {
            GlideApp.with(fragment)
                    .load(photoUrl)
                    .centerCrop()
                    .signature(new ObjectKey(photoUrl))
                    .into(viewHolder.imageView);
        }
    }

    @Override
    public void onItemClick(View view, int position) {

        pagerFragment.setPosition(position);

        fragment.getActivity().getSupportFragmentManager()
                .beginTransaction()
                .add(R.id.photosFrameLayout, pagerFragment)
                .setTransition(FragmentTransaction.TRANSIT_FRAGMENT_OPEN)
                .addToBackStack(null)
                .commit();
    }

    @Override
    public PhotoViewHolder onCreateViewHolder(ViewGroup viewGroup, int viewType) {
        View itemView = LayoutInflater.from(viewGroup.getContext()).
                inflate(R.layout.photo_grid_item, viewGroup, false);
        return new PhotoViewHolder(itemView, this);
    }
}