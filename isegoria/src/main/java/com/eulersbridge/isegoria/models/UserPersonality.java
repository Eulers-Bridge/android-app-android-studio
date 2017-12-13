package com.eulersbridge.isegoria.models;

@SuppressWarnings({"CanBeFinal", "WeakerAccess"})
public class UserPersonality {

    public float agreeableness;
    public float conscientiousness;
    public float emotionalStability;
    public float extroversion;

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
