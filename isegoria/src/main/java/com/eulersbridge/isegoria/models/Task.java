package com.eulersbridge.isegoria.models;

import com.google.gson.annotations.SerializedName;

/**
 * Created by Seb on 04/11/2017.
 */

public class Task {

    @SerializedName("taskId")
    public long id;

    public String action;
    public long xpValue;
}
