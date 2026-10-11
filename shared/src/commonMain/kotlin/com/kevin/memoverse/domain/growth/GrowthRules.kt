package com.kevin.memoverse.domain.growth

import com.kevin.memoverse.domain.model.LearningVerse
import com.kevin.memoverse.domain.model.MasteryStage

/**
 * Règles de croissance du cadrage :
 * - Graine → Racine : 3 réussites ;
 * - Racine → Arbre : 4 réussites sur au moins 2 jours différents ;
 * - Arbre → Fruit : 3 réussites espacées d'au moins 3 jours ;
 * - 2 échecs d'affilée font redescendre d'un stade (jamais sous Graine).
 */
object GrowthRules {
    const val SEED_SUCCESSES = 3
    const val ROOT_SUCCESSES = 4
    const val ROOT_DISTINCT_DAYS = 2
    const val TREE_SUCCESSES = 3
    const val TREE_SPACING_DAYS = 3
    const val FAILURES_BEFORE_WILTING = 2

    fun apply(verse: LearningVerse, isCorrect: Boolean, todayEpochDay: Long): LearningVerse =
        if (isCorrect) onSuccess(verse, todayEpochDay) else onFailure(verse)

    private fun onSuccess(verse: LearningVerse, today: Long): LearningVerse {
        val v = verse.copy(consecutiveFailures = 0)
        return when (v.stage) {
            MasteryStage.SEED -> {
                val count = v.successCount + 1
                if (count >= SEED_SUCCESSES) v.enter(MasteryStage.ROOT) else v.copy(successCount = count, lastSuccessEpochDay = today)
            }
            MasteryStage.ROOT -> {
                val count = v.successCount + 1
                val days = if (v.lastSuccessEpochDay == today) v.successDays else v.successDays + 1
                if (count >= ROOT_SUCCESSES && days >= ROOT_DISTINCT_DAYS) v.enter(MasteryStage.TREE)
                else v.copy(successCount = count, successDays = days, lastSuccessEpochDay = today)
            }
            MasteryStage.TREE -> {
                val last = v.lastSuccessEpochDay
                if (last != null && today - last < TREE_SPACING_DAYS) return v
                val count = v.successCount + 1
                if (count >= TREE_SUCCESSES) v.enter(MasteryStage.FRUIT) else v.copy(successCount = count, lastSuccessEpochDay = today)
            }
            MasteryStage.FRUIT -> v.copy(lastSuccessEpochDay = today)
        }
    }

    private fun onFailure(verse: LearningVerse): LearningVerse {
        val failures = verse.consecutiveFailures + 1
        if (failures < FAILURES_BEFORE_WILTING || verse.stage == MasteryStage.SEED) return verse.copy(consecutiveFailures = failures)
        return verse.enter(MasteryStage.entries[verse.stage.ordinal - 1])
    }

    private fun LearningVerse.enter(stage: MasteryStage) =
        copy(stage = stage, successCount = 0, successDays = 0, lastSuccessEpochDay = null, consecutiveFailures = 0)
}
