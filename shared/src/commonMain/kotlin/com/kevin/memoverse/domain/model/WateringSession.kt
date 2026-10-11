package com.kevin.memoverse.domain.model

/** Une séance d'arrosage : les exercices à faire et l'état des versets concernés au départ. */
data class WateringSession(
    val exercises: List<Exercise>,
    val learningVerses: List<LearningVerse>,
)
