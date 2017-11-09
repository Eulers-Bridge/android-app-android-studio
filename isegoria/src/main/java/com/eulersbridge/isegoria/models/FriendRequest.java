package com.eulersbridge.isegoria.models;

import android.support.annotation.Nullable;

import com.squareup.moshi.Json;

public class FriendRequest {

    public long id;

    @Nullable
    public Boolean accepted;

    @Nullable
    public Boolean rejected;

    @Json(name = "requesterProfile")
    public User requester;

    @Json(name = "requestReceiverProfile")
    public User requestReceiver;
}
