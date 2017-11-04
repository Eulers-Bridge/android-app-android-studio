package com.eulersbridge.isegoria.models;

import android.support.annotation.Nullable;

import org.json.JSONException;
import org.json.JSONObject;

/**
 * Created by Seb on 29/10/2017.
 */

public class FriendRequest {

    public enum Type {
        RECEIVED, SENT, UNKNOWN
    }

    private long id;

    private boolean accepted;
    private boolean rejected;

    private User requester;
    private User requestReceiver;

    private Type type = Type.UNKNOWN;

    public FriendRequest(JSONObject jsonObject, Type type) {
        try {
            id = jsonObject.getLong("id");

            accepted = jsonObject.optBoolean("accepted");
            rejected = jsonObject.optBoolean("rejected");

            requester = new User(jsonObject.getJSONObject("requesterProfile"));
            requestReceiver = new User(jsonObject.getJSONObject("requestReceiverProfile"));

        } catch (JSONException e) {
            e.printStackTrace();
        }

        this.type = type;
    }

    public long getId() {
        return id;
    }

    private User getRequester() {
        return requester;
    }

    private User getRequestReceiver() {
        return requestReceiver;
    }

    public boolean isAccepted() {
        return accepted;
    }

    public boolean isRejected() {
        return rejected;
    }

    public Type getType() {
        return type;
    }

    public void setType(Type type) {
        this.type = type;
    }

    public @Nullable User getUser() {
        if (type == Type.SENT) {
            return getRequestReceiver();

        } else if (type == Type.RECEIVED) {
            return getRequester();

        } else {
            return null;
        }
    }
}
