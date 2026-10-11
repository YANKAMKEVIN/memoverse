package com.kevin.memoverse.data.mapper

import com.kevin.memoverse.data.local.db.LearningVerseEntity
import com.kevin.memoverse.domain.model.LearningVerse
import com.kevin.memoverse.domain.model.MasteryStage

fun LearningVerseEntity.toDomain(): LearningVerse = LearningVerse(
    verseId = verseId,
    themeId = themeId,
    stage = MasteryStage.entries.firstOrNull { it.name == stage } ?: MasteryStage.SEED,
    addedAtEpochMillis = addedAtEpochMillis,
    successCount = successCount.toInt(),
    successDays = successDays.toInt(),
    lastSuccessEpochDay = lastSuccessEpochDay,
    consecutiveFailures = consecutiveFailures.toInt(),
    reviewStep = reviewStep.toInt(),
    dueAtEpochMillis = dueAtEpochMillis,
)
