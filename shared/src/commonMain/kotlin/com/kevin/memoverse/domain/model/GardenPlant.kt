package com.kevin.memoverse.domain.model

/** Un verset planté dans le jardin, avec sa référence lisible. */
data class GardenPlant(
    val verseId: String,
    val reference: String,
    val stage: MasteryStage,
    val isThirsty: Boolean = false,
    val text: String = "",
)
