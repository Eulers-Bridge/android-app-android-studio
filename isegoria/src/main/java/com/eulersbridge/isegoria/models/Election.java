package com.eulersbridge.isegoria.models;

import com.google.gson.annotations.SerializedName;

/**
 * Created by Seb on 04/11/2017.
 */

public class Election {

    @SerializedName("electionId")
    public long id;

    @SerializedName("start")
    public long startTimestamp;

    @SerializedName("end")
    public long endTimestamp;

    @SerializedName("startVoting")
    public long startVotingTimestamp;

    @SerializedName("endVoting")
    public long endVotingTimestamp;

    public String title;
    public String introduction;
    public String process;

    @SerializedName("institutionDomain")
    private ElectionInstitution electionInstitution;
}
