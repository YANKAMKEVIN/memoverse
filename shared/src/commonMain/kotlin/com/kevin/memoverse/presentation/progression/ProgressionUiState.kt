package com.kevin.memoverse.presentation.progression

data class ProgressionUiState(
    val isLoading: Boolean = true,
    val seedCount: Int = 0,
    val rootCount: Int = 0,
    val treeCount: Int = 0,
    val fruitCount: Int = 0,
) {
    val totalCount: Int get() = seedCount + rootCount + treeCount + fruitCount
}
