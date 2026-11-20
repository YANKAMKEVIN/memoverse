package com.kevin.memoverse.presentation.onboarding

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.kevin.memoverse.domain.repository.PreferencesRepository
import com.kevin.memoverse.domain.usecase.ShouldShowOnboardingUseCase
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class OnboardingViewModel(
    shouldShowOnboarding: ShouldShowOnboardingUseCase,
    private val preferences: PreferencesRepository,
) : ViewModel() {

    private val _showOnboarding = MutableStateFlow<Boolean?>(null)

    /** null tant que la réponse n'est pas lue (rien n'est affiché pour éviter un éclair du jardin). */
    val showOnboarding: StateFlow<Boolean?> = _showOnboarding.asStateFlow()

    init {
        viewModelScope.launch { _showOnboarding.value = shouldShowOnboarding() }
    }

    /** Terminé ou passé : l'accueil ne reviendra plus. */
    fun onFinished() {
        _showOnboarding.value = false
        viewModelScope.launch { preferences.setOnboardingSeen() }
    }
}
