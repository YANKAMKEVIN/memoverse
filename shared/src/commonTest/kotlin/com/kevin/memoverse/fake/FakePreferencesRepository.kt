package com.kevin.memoverse.fake

import com.kevin.memoverse.domain.model.DataResult
import com.kevin.memoverse.domain.repository.PreferencesRepository

class FakePreferencesRepository : PreferencesRepository {

    var notificationPermissionAsked = false

    override suspend fun hasAskedNotificationPermission(): Boolean = notificationPermissionAsked

    override suspend fun setNotificationPermissionAsked(): DataResult<Unit> {
        notificationPermissionAsked = true
        return DataResult.Success(Unit)
    }
}
