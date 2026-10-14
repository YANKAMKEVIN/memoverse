package com.kevin.memoverse.data.repository

import com.kevin.memoverse.data.local.db.MemoverseDatabase
import com.kevin.memoverse.domain.model.DataResult
import com.kevin.memoverse.domain.repository.PreferencesRepository
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.IO
import kotlinx.coroutines.withContext
import kotlin.coroutines.cancellation.CancellationException

class PreferencesRepositoryImpl(
    private val database: MemoverseDatabase,
    private val ioDispatcher: CoroutineDispatcher = Dispatchers.IO,
) : PreferencesRepository {

    private val queries get() = database.preferenceQueries

    override suspend fun hasAskedNotificationPermission(): Boolean = try {
        withContext(ioDispatcher) { queries.selectValue(NOTIFICATION_ASKED_KEY).executeAsOneOrNull() == TRUE }
    } catch (e: CancellationException) {
        throw e
    } catch (e: Exception) {
        // Base illisible : mieux vaut ne pas redemander que harceler.
        true
    }

    override suspend fun setNotificationPermissionAsked(): DataResult<Unit> = write(NOTIFICATION_ASKED_KEY, TRUE)

    private suspend fun write(key: String, value: String): DataResult<Unit> = try {
        withContext(ioDispatcher) { queries.upsert(key, value) }
        DataResult.Success(Unit)
    } catch (e: CancellationException) {
        throw e
    } catch (e: Exception) {
        DataResult.Error(e)
    }

    private companion object {
        const val NOTIFICATION_ASKED_KEY = "notification_permission_asked"
        const val TRUE = "true"
    }
}
