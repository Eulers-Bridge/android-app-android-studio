package com.eulersbridge.isegoria.friends;

import android.support.annotation.Nullable;
import android.support.v7.widget.RecyclerView;

import com.eulersbridge.isegoria.network.api.models.FriendRequest;

import java.lang.ref.WeakReference;

interface ViewHolderDataSource {
    void getFriendRequestInstitution(@Nullable Long institutionId, WeakReference<RecyclerView.ViewHolder> weakViewHolder);
    void onClick(FriendRequest friendRequest);
    void onActionClick(FriendRequest friendRequest);
}