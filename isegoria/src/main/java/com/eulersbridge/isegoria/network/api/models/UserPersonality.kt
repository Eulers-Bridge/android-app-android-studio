package com.eulersbridge.isegoria.network.api.models

data class UserPersonality(
    val agreeableness: Float,
    val conscientiousness: Float,
    val emotionalStability: Float,
    val extroversion: Float,

    // Mis-spelled, matching API mistake
    val openess: Float
)
