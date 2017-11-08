package com.eulersbridge.isegoria.models;

/**
 * Created by Seb on 07/11/2017.
 */

public class UserPersonality {

    float agreeableness;
    float conscientiousness;
    float emotionalStability;
    float extroversion;

    // Openness to experiences
    float openess;

    public UserPersonality(float agreeableness, float conscientiousness, float emotionalStability,
                           float extroversion, float openess) {
        this.agreeableness = agreeableness;
        this.conscientiousness = conscientiousness;
        this.emotionalStability = emotionalStability;
        this.extroversion = extroversion;
        this.openess = openess;
    }

}
