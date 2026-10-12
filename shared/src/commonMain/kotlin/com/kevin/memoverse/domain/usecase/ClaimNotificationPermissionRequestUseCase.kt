package com.kevin.memoverse.domain.usecase

import com.kevin.memoverse.domain.repository.PreferencesRepository

/**
 * La permission de notification ne se demande qu'une fois, après la première plantation :
 * renvoie `true` la première fois (et le retient), `false` ensuite, même si la question est restée sans réponse.
 */
class ClaimNotificationPermissionRequestUseCase(
    private val repository: PreferencesRepository,
) {
    suspend operator fun invoke(): Boolean {
        if (repository.hasAskedNotificationPermission()) return false
        repository.setNotificationPermissionAsked()
        return true
    }
}
