package com.kevin.memoverse.data.repository

import app.cash.sqldelight.coroutines.asFlow
import app.cash.sqldelight.coroutines.mapToOneOrNull
import com.kevin.memoverse.data.local.db.MemoverseDatabase
import com.kevin.memoverse.domain.model.DataResult
import com.kevin.memoverse.domain.model.TypefaceChoice
import com.kevin.memoverse.domain.repository.PreferencesRepository
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.IO
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext
import kotlin.coroutines.cancellation.CancellationException

class PreferencesRepositoryImpl(
    private val database: MemoverseDatabase,
    private val ioDispatcher: CoroutineDispatcher = Dispatchers.IO,
) : PreferencesRepository {

    private val queries get() = database.preferenceQueries

    override fun observeTypeface(): Flow<TypefaceChoice> =
        queries.selectValue(TYPEFACE_KEY).asFlow().mapToOneOrNull(ioDispatcher)
            // Une valeur inconnue (police retirée d'une version future) retombe sur celle d'origine.
            .map { value -> TypefaceChoice.entries.firstOrNull { it.name == value } ?: TypefaceChoice.DEFAULT }
            .distinctUntilChanged()

    override suspend fun setTypeface(typeface: TypefaceChoice): DataResult<Unit> = write(TYPEFACE_KEY, typeface.name)

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
        const val TYPEFACE_KEY = "typeface"
        const val NOTIFICATION_ASKED_KEY = "notification_permission_asked"
        const val TRUE = "true"
    }
}
