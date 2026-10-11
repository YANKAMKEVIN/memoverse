package com.kevin.memoverse.domain.exercise

import com.kevin.memoverse.domain.model.LearningVerse
import com.kevin.memoverse.domain.model.MasteryStage

/** Choisit les niveaux d'exercice d'un verset selon son stade (cadrage, section Progression). */
object ExercisePlanner {
    const val EXERCISES_PER_VERSE = 2

    private val levelsByStage = mapOf(
        // Jour 1 : reconnaître, ordonner, choisir. Jour 2+ : écrire soi-même. Puis réciter, à l'écrit et à l'oral.
        MasteryStage.SEED to listOf(1, 2, 3),
        MasteryStage.ROOT to listOf(4, 5, 6),
        MasteryStage.TREE to listOf(6, 7, 8),
        MasteryStage.FRUIT to listOf(7, 8),
    )

    /** Deux niveaux successifs, qui avancent avec les réussites du stade. */
    fun levelsFor(verse: LearningVerse): List<Int> {
        val levels = levelsByStage.getValue(verse.stage)
        return List(EXERCISES_PER_VERSE) { i -> levels[(verse.successCount + i) % levels.size] }
    }
}
