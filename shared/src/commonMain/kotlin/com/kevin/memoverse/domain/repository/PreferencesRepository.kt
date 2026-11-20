package com.kevin.memoverse.domain.repository

import com.kevin.memoverse.domain.model.DataResult
import com.kevin.memoverse.domain.model.WateringStats
import kotlinx.coroutines.flow.Flow

/** Réglages de l'utilisateur, gardés sur l'appareil. */
interface PreferencesRepository {
    suspend fun hasAskedNotificationPermission(): Boolean

    suspend fun setNotificationPermissionAsked(): DataResult<Unit>

    /** L'accueil du premier lancement a déjà été vu (ou passé). */
    suspend fun hasSeenOnboarding(): Boolean

    suspend fun setOnboardingSeen(): DataResult<Unit>

    /** XP cumulée et série de jours d'arrosage ; des stats vides si rien n'est encore enregistré. */
    fun observeWateringStats(): Flow<WateringStats>

    suspend fun saveWateringStats(stats: WateringStats): DataResult<Unit>
}
