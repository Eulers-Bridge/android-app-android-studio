package com.eulersbridge.isegoria.friends;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.util.DiffUtil;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.eulersbridge.isegoria.R;
import com.eulersbridge.isegoria.network.api.models.FriendRequest;
import com.eulersbridge.isegoria.network.api.models.Institution;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.List;

class FriendRequestAdapter extends RecyclerView.Adapter implements ViewHolderDataSource {

    interface Delegate {
        void getFriendRequestInstitution(long institutionId,
                                         @FriendsFragment.FriendRequestType int type,
                                         WeakReference<RecyclerView.ViewHolder> weakViewHolder);
        void performFriendRequestAction(@FriendsFragment.FriendRequestType int type, FriendRequest request);
    }

    final private List<FriendRequest> items = new ArrayList<>();
    final private @FriendsFragment.FriendRequestType int itemType;
    final private Delegate delegate;

    FriendRequestAdapter(@FriendsFragment.FriendRequestType int itemType, Delegate delegate) {
        this.itemType = itemType;
        this.delegate = delegate;
    }

    void setItems(@NonNull List<FriendRequest> newItems) {
        DiffUtil.DiffResult result = DiffUtil.calculateDiff(new DiffUtil.Callback() {
            @Override
            public int getOldListSize() {
                return items.size();
            }

            @Override
            public int getNewListSize() {
                return newItems.size();
            }

            @Override
            public boolean areItemsTheSame(int oldItemPosition, int newItemPosition) {
                FriendRequest oldItem = items.get(oldItemPosition);
                FriendRequest newItem = items.get(newItemPosition);
                return oldItem.id == newItem.id;
            }

            @Override
            public boolean areContentsTheSame(int oldItemPosition, int newItemPosition) {
                FriendRequest oldItem = items.get(oldItemPosition);
                FriendRequest newItem = items.get(newItemPosition);
                return oldItem.equals(newItem);
            }
        });

        items.clear();
        items.addAll(newItems);

        result.dispatchUpdatesTo(this);
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(parent.getContext()).
                inflate(R.layout.friend_partial_list_item, parent, false);

        if (this.itemType == FriendsFragment.RECEIVED) {
            return new ReceivedFriendRequestViewHolder(itemView, this);
        } else {
            // itemType == FriendsFragment.SENT
            return new SentFriendRequestViewHolder(itemView, this);
        }
    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {
        FriendRequest item = items.get(position);

        if (this.itemType == FriendsFragment.RECEIVED) {
            ((ReceivedFriendRequestViewHolder)holder).setItem(item);
        } else {
            // itemType == FriendsFragment.SENT
            ((SentFriendRequestViewHolder)holder).setItem(item);
        }
    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder holder, int position, List payloads) {
        onBindViewHolder(holder, position);
    }

    @Override
    public void getFriendRequestInstitution(@Nullable Long institutionId, WeakReference<RecyclerView.ViewHolder> weakViewHolder) {
        if (institutionId != null)
            delegate.getFriendRequestInstitution(institutionId, itemType, weakViewHolder);
    }

    @Override
    public void onActionClick(@Nullable FriendRequest friendRequest) {
        if (friendRequest != null)
            delegate.performFriendRequestAction(itemType, friendRequest);
    }

    void setInstitution(@Nullable Institution institution, WeakReference<RecyclerView.ViewHolder> weakViewHolder) {
        RecyclerView.ViewHolder viewHolder = weakViewHolder.get();

        if (viewHolder != null && institution != null) {

            if (viewHolder instanceof ReceivedFriendRequestViewHolder) {
                ((ReceivedFriendRequestViewHolder)viewHolder).setInstitution(institution);

            } else if (viewHolder instanceof SentFriendRequestViewHolder) {
                ((SentFriendRequestViewHolder) viewHolder).setInstitution(institution);
            }
        }
    }

    @Override
    public int getItemCount() {
        return items.size();
    }
}
