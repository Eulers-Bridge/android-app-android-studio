package com.eulersbridge.isegoria.models;

import org.json.JSONException;
import org.json.JSONObject;

/**
 * Created by Seb on 04/11/2017.
 */

public class Election {

    private long id;

    private long startTimestamp;
    private long endTimestamp;

    private long startVotingTimestamp;
    private long endVotingTimestamp;

    private String title;
    private String introduction;
    private String process;

    private ElectionInstitution electionInstitution;

    public Election(JSONObject jsonObject) {
        try {
            id = jsonObject.getLong("electionId");

            startTimestamp = jsonObject.getLong("start");
            endTimestamp = jsonObject.getLong("end");

            startVotingTimestamp = jsonObject.getLong("startVoting");
            endVotingTimestamp = jsonObject.getLong("endVoting");

            title = jsonObject.getString("title");
            introduction = jsonObject.getString("introduction");
            process = jsonObject.getString("process");

            electionInstitution = new ElectionInstitution(jsonObject.getJSONObject("institutionDomain"));

        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    public long getId() {
        return id;
    }

    public long getStartTimestamp() {
        return startTimestamp;
    }

    public long getEndTimestamp() {
        return endTimestamp;
    }

    public long getStartVotingTimestamp() {
        return startVotingTimestamp;
    }

    public long getEndVotingTimestamp() {
        return endVotingTimestamp;
    }

    public String getTitle() {
        return title;
    }

    public String getIntroduction() {
        return introduction;
    }

    public String getProcess() {
        return process;
    }

    public ElectionInstitution getElectionInstitution() {
        return electionInstitution;
    }
}
