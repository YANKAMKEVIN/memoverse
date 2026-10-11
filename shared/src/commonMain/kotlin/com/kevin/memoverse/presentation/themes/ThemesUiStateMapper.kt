package com.kevin.memoverse.presentation.themes

internal fun deriveThemesUiState(load: ThemesLoad, learningVerseIds: Set<String>): ThemesUiState = when (load) {
    ThemesLoad.Loading -> ThemesUiState(isLoading = true)
    ThemesLoad.Failed -> ThemesUiState(isLoading = false, hasError = true)
    is ThemesLoad.Loaded -> ThemesUiState(
        isLoading = false,
        families = load.families.map { family ->
            ThemeFamilyUi(
                name = family.name,
                themes = family.themes.map { theme ->
                    ThemeItemUi(
                        id = theme.id,
                        name = theme.name,
                        verseCount = theme.verseIds.size,
                        learningCount = theme.verseIds.count { it in learningVerseIds },
                    )
                },
            )
        },
    )
}
