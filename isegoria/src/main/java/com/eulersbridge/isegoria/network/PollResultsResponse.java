package com.eulersbridge.isegoria.network;

import com.eulersbridge.isegoria.models.PollResult;
import com.google.gson.annotations.SerializedName;

import java.util.List;

/**
 * Created by Seb on 07/11/2017.
 */

public class PollResultsResponse {

    @SerializedName("nodeId")
    public long id;

    public long pollId;

    @SerializedName("answers")
    public List<PollResult> results;

}
