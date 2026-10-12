package com.kevin.memoverse.fake

import com.kevin.memoverse.domain.model.DataResult
import com.kevin.memoverse.domain.model.TypefaceChoice
import com.kevin.memoverse.domain.repository.PreferencesRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow

class FakePreferencesRepository(
    initial: TypefaceChoice = TypefaceChoice.DEFAULT,
    var saveError: Throwable? = null,
) : PreferencesRepository {

    val typeface = MutableStateFlow(initial)
    var notificationPermissionAsked = false

    override fun observeTypeface(): Flow<TypefaceChoice> = typeface

    override suspend fun setTypeface(typeface: TypefaceChoice): DataResult<Unit> {
        saveError?.let { return DataResult.Error(it) }
        this.typeface.value = typeface
        return DataResult.Success(Unit)
    }

    override suspend fun hasAskedNotificationPermission(): Boolean = notificationPermissionAsked

    override suspend fun setNotificationPermissionAsked(): DataResult<Unit> {
        notificationPermissionAsked = true
        return DataResult.Success(Unit)
    }
}
