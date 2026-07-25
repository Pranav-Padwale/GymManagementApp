package com.gym.model.goal

enum class Gender(val displayName: String) {
    FEMALE("Female"),
    MALE("Male")
}

enum class WeightGoal(val displayName: String) {
    GAIN("Gain"),
    MAINTAIN("Maintain"),
    LOSE("Lose"),
    LOSE_KETO("Lose Keto")
}

enum class ActivityLevel(val displayName: String, val multiplier: Double) {
    SEDENTARY("Sedentary", 1.2),
    LIGHTLY("Lightly", 1.375),
    MODERATELY("Moderately", 1.55),
    VERY("Very", 1.725)
}
