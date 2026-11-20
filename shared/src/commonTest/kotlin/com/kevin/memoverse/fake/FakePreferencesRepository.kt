package com.kevin.memoverse.fake

import com.kevin.memoverse.domain.model.DataResult
import com.kevin.memoverse.domain.model.WateringStats
import com.kevin.memoverse.domain.repository.PreferencesRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow

class FakePreferencesRepository : PreferencesRepository {

    var notificationPermissionAsked = false

    override suspend fun hasAskedNotificationPermission(): Boolean = notificationPermissionAsked

    override suspend fun setNotificationPermissionAsked(): DataResult<Unit> {
        notificationPermissionAsked = true
        return DataResult.Success(Unit)
    }

    var onboardingSeen = false

    override suspend fun hasSeenOnboarding(): Boolean = onboardingSeen

    override suspend fun setOnboardingSeen(): DataResult<Unit> {
        onboardingSeen = true
        return DataResult.Success(Unit)
    }

    val wateringStats = MutableStateFlow(WateringStats())

    override fun observeWateringStats(): Flow<WateringStats> = wateringStats

    override suspend fun saveWateringStats(stats: WateringStats): DataResult<Unit> {
        wateringStats.value = stats
        return DataResult.Success(Unit)
    }
}
