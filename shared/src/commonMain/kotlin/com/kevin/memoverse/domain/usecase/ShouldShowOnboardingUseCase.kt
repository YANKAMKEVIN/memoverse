package com.kevin.memoverse.domain.usecase

import com.kevin.memoverse.domain.repository.LearningRepository
import com.kevin.memoverse.domain.repository.PreferencesRepository
import kotlinx.coroutines.flow.first

/**
 * L'accueil en trois volets ne s'affiche qu'au tout premier lancement. Une personne qui a déjà des
 * versets plantés (installée avant l'accueil) connaît la boucle : on le note comme vu, sans le montrer.
 */
class ShouldShowOnboardingUseCase(
    private val preferences: PreferencesRepository,
    private val learningRepository: LearningRepository,
) {
    suspend operator fun invoke(): Boolean {
        if (preferences.hasSeenOnboarding()) return false
        val hasVerses = runCatching { learningRepository.observeLearningVerses().first().isNotEmpty() }.getOrDefault(false)
        if (hasVerses) {
            preferences.setOnboardingSeen()
            return false
        }
        return true
    }
}
