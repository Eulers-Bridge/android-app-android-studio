package com.eulersbridge.isegoria.network.api.model

data class UserPersonality(
    val agreeableness: Float,
    val conscientiousness: Float,
    val emotionalStability: Float,
    val extroversion: Float,

    // Mis-spelled, matching API mistake
    val openess: Float
)
