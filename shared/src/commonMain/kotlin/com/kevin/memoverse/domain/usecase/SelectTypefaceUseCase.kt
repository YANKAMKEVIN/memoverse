package com.kevin.memoverse.domain.usecase

import com.kevin.memoverse.domain.model.DataResult
import com.kevin.memoverse.domain.model.TypefaceChoice
import com.kevin.memoverse.domain.repository.PreferencesRepository

/** Enregistre la police choisie ; toute l'app l'adopte aussitôt. */
class SelectTypefaceUseCase(
    private val repository: PreferencesRepository,
) {
    suspend operator fun invoke(typeface: TypefaceChoice): DataResult<Unit> = repository.setTypeface(typeface)
}
