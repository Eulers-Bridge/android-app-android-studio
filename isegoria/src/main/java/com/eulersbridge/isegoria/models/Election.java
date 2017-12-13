package com.eulersbridge.isegoria.models;

import com.eulersbridge.isegoria.network.Timestamp;
import com.squareup.moshi.Json;

@SuppressWarnings("unused")
public class Election {

    @Json(name = "electionId")
    public long id;

    @Json(name = "start")
    @Timestamp
    public long startTimestamp;

    @Json(name = "end")
    @Timestamp
    public long endTimestamp;

    @Json(name = "startVoting")
    @Timestamp
    public long startVotingTimestamp;

    @Json(name = "endVoting")
    @Timestamp
    public long endVotingTimestamp;

    public String title;
    public String introduction;
    public String process;

    @Json(name = "institutionDomain")
    private ElectionInstitution electionInstitution;
}
