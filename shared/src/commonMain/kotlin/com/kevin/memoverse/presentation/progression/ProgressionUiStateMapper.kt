package com.kevin.memoverse.presentation.progression

import com.kevin.memoverse.domain.model.LearningVerse
import com.kevin.memoverse.domain.model.MasteryStage

fun List<LearningVerse>.toProgressionUiState(): ProgressionUiState {
    val byStage = groupingBy { it.stage }.eachCount()
    return ProgressionUiState(
        isLoading = false,
        seedCount = byStage[MasteryStage.SEED] ?: 0,
        rootCount = byStage[MasteryStage.ROOT] ?: 0,
        treeCount = byStage[MasteryStage.TREE] ?: 0,
        fruitCount = byStage[MasteryStage.FRUIT] ?: 0,
    )
}
