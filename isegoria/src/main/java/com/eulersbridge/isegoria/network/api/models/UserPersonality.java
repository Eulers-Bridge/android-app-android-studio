package com.eulersbridge.isegoria.network.api.models;

@SuppressWarnings({"CanBeFinal", "WeakerAccess"})
public class UserPersonality {

    public float agreeableness;
    public float conscientiousness;
    public float emotionalStability;
    public float extroversion;

    // Openness to experiences (API mis-spelling, hence attribute name0
    float openess;

    public UserPersonality(float agreeableness, float conscientiousness, float emotionalStability,
                           float extroversion, float openness) {
        this.agreeableness = agreeableness;
        this.conscientiousness = conscientiousness;
        this.emotionalStability = emotionalStability;
        this.extroversion = extroversion;
        this.openess = openness;
    }

}
