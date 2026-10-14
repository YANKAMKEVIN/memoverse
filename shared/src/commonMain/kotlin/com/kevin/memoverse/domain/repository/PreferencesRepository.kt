package com.kevin.memoverse.domain.repository

import com.kevin.memoverse.domain.model.DataResult

/** Réglages de l'utilisateur, gardés sur l'appareil. */
interface PreferencesRepository {
    suspend fun hasAskedNotificationPermission(): Boolean

    suspend fun setNotificationPermissionAsked(): DataResult<Unit>
}
