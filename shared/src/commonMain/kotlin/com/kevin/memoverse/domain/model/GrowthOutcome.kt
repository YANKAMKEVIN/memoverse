package com.kevin.memoverse.domain.model

/** Effet d'un exercice sur un verset : il peut grandir, rester ou faner d'un stade. */
data class GrowthOutcome(
    val verse: LearningVerse,
    val previousStage: MasteryStage,
) {
    val hasGrown: Boolean get() = verse.stage.ordinal > previousStage.ordinal
    val hasWilted: Boolean get() = verse.stage.ordinal < previousStage.ordinal
}
