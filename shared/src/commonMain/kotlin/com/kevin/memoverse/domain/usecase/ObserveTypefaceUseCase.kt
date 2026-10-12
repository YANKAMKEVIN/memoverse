package com.kevin.memoverse.domain.usecase

import com.kevin.memoverse.domain.model.TypefaceChoice
import com.kevin.memoverse.domain.repository.PreferencesRepository
import kotlinx.coroutines.flow.Flow

/** La police choisie par l'utilisateur (celle d'origine tant qu'il n'a rien choisi). */
class ObserveTypefaceUseCase(
    private val repository: PreferencesRepository,
) {
    operator fun invoke(): Flow<TypefaceChoice> = repository.observeTypeface()
}
