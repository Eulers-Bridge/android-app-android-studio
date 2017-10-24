package com.eulersbridge.isegoria.models;

/**
 * Created by Anthony on 24/03/2015.
 */
public class VoteLocation {
    private String ownerId;
    private String votingLocationId;
    private String name;
    private String information;

    public VoteLocation(String ownerId, String votingLocationId, String name, String information) {
        this.ownerId = ownerId;
        this.votingLocationId = votingLocationId;
        this.name = name;
        this.information = information;
    }

    public String getOwnerId() {
        return ownerId;
    }

    public void setOwnerId(String ownerId) {
        this.ownerId = ownerId;
    }

    public String getVotingLocationId() {
        return votingLocationId;
    }

    public void setVotingLocationId(String votingLocationId) {
        this.votingLocationId = votingLocationId;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getInformation() {
        return information;
    }

    public void setInformation(String information) {
        this.information = information;
    }
}
