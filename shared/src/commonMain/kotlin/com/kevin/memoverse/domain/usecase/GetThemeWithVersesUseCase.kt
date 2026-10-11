package com.kevin.memoverse.domain.usecase

import com.kevin.memoverse.domain.model.DataResult
import com.kevin.memoverse.domain.model.ThemeWithVerses
import com.kevin.memoverse.domain.repository.VerseBankRepository

class GetThemeWithVersesUseCase(
    private val repository: VerseBankRepository,
) {
    suspend operator fun invoke(themeId: String): DataResult<ThemeWithVerses> {
        val themes = when (val result = repository.getThemes()) {
            is DataResult.Success -> result.data
            is DataResult.Error -> return result
        }
        val theme = themes.firstOrNull { it.id == themeId }
            ?: return DataResult.Error(NoSuchElementException("Thème inconnu : $themeId"))
        return when (val result = repository.getVerses(theme.verseIds)) {
            is DataResult.Success -> DataResult.Success(ThemeWithVerses(theme, result.data))
            is DataResult.Error -> result
        }
    }
}
