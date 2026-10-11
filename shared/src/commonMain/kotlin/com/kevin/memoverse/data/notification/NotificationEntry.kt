package com.kevin.memoverse.data.notification

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

/**
 * Pont entre un tap sur un rappel (côté plateforme) et la navigation partagée : l'activité Android
 * ou le délégué iOS signale l'ouverture, l'app ouvre la séance puis consomme le signal.
 */
object NotificationEntry {
    private val _pendingSessionOpen = MutableStateFlow(false)
    val pendingSessionOpen: StateFlow<Boolean> = _pendingSessionOpen.asStateFlow()

    fun requestSessionOpen() {
        _pendingSessionOpen.value = true
    }

    fun consumeSessionOpen() {
        _pendingSessionOpen.value = false
    }
}
