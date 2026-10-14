package com.kevin.memoverse.presentation.themedetail

import com.kevin.memoverse.domain.model.Difficulty
import com.kevin.memoverse.domain.model.LearningVerse
import com.kevin.memoverse.domain.model.VerseSelection
import com.kevin.memoverse.presentation.ceremony.CeremonyUi

internal fun deriveThemeDetailUiState(
    load: ThemeLoad,
    selection: VerseSelection,
    learning: List<LearningVerse>,
    startStatus: StartStatus,
): ThemeDetailUiState = when (load) {
    ThemeLoad.Loading -> ThemeDetailUiState(isLoading = true)
    ThemeLoad.Failed -> ThemeDetailUiState(isLoading = false, hasError = true)
    is ThemeLoad.Loaded -> {
        val (theme, verses) = load.themeWithVerses
        val learningVerseIds = learning.map { it.verseId }.toSet()
        val activeCount = learning.count { it.stage.isActive }
        ThemeDetailUiState(
            isLoading = false,
            themeName = theme.name,
            familyName = theme.family,
            verses = verses.map { verse ->
                VerseItemUi(
                    id = verse.id,
                    reference = verse.reference,
                    text = verse.text,
                    context = verse.context,
                    difficulty = verse.difficulty.toUi(),
                    isSelected = verse.id in selection.selectedIds,
                    isLearning = verse.id in learningVerseIds,
                )
            },
            selectedCount = selection.selectedIds.size,
            minSelection = VerseSelection.MIN_VERSES,
            maxSelection = VerseSelection.MAX_VERSES,
            isLimitReached = selection.isLimitReached,
            canStart = selection.canStart && startStatus != StartStatus.InProgress &&
                activeCount + selection.selectedIds.size <= LearningVerse.MAX_ACTIVE_VERSES,
            freeSlots = (LearningVerse.MAX_ACTIVE_VERSES - activeCount).coerceAtLeast(0),
            activeCount = activeCount,
            maxActive = LearningVerse.MAX_ACTIVE_VERSES,
            isStarting = startStatus == StartStatus.InProgress,
            isStarted = startStatus == StartStatus.Planted,
            // Une graine par verset planté, dans l'ordre du thème.
            ceremony = if (startStatus == StartStatus.Started) {
                CeremonyUi.Planting(verses.filter { it.id in selection.selectedIds }.map { it.reference })
            } else {
                null
            },
            startError = (startStatus as? StartStatus.Error)?.error,
        )
    }
}

private fun Difficulty.toUi(): DifficultyUi = when (this) {
    Difficulty.EASY -> DifficultyUi.EASY
    Difficulty.MEDIUM -> DifficultyUi.MEDIUM
    Difficulty.HARD -> DifficultyUi.HARD
}
