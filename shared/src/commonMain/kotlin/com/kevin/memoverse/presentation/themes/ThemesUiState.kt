package com.kevin.memoverse.presentation.themes

data class ThemesUiState(
    val isLoading: Boolean = true,
    val hasError: Boolean = false,
    val families: List<ThemeFamilyUi> = emptyList(),
)

data class ThemeFamilyUi(
    val name: String,
    val themes: List<ThemeItemUi>,
)

data class ThemeItemUi(
    val id: String,
    val name: String,
    val verseCount: Int,
    val learningCount: Int = 0,
)
