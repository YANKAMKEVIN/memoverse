package com.kevin.memoverse.domain.repository

import com.kevin.memoverse.domain.model.DataResult
import com.kevin.memoverse.domain.model.TypefaceChoice
import kotlinx.coroutines.flow.Flow

/** Réglages de l'utilisateur, gardés sur l'appareil. */
interface PreferencesRepository {
    fun observeTypeface(): Flow<TypefaceChoice>

    suspend fun setTypeface(typeface: TypefaceChoice): DataResult<Unit>

    suspend fun hasAskedNotificationPermission(): Boolean

    suspend fun setNotificationPermissionAsked(): DataResult<Unit>
}
