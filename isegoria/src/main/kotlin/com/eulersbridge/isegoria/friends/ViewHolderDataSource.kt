package com.eulersbridge.isegoria.friends

import android.support.v7.widget.RecyclerView

import com.eulersbridge.isegoria.network.api.model.FriendRequest

import java.lang.ref.WeakReference

internal interface ViewHolderDataSource {
    fun onClick(friendRequest: FriendRequest?)
    fun onActionClick(friendRequest: FriendRequest?)
}