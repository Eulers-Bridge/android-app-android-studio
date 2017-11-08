package com.eulersbridge.isegoria.models;

import com.google.gson.annotations.SerializedName;

/**
 * Created by Seb on 29/10/2017.
 */

public class FriendRequest {

    public long id;

    public boolean accepted;
    public boolean rejected;

    @SerializedName("requesterProfile")
    public UserProfile requester;

    @SerializedName("requestReceiverProfile")
    public UserProfile requestReceiver;
}
