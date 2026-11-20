package com.kevin.memoverse.data.repository

import app.cash.sqldelight.coroutines.asFlow
import app.cash.sqldelight.coroutines.mapToOneOrNull
import com.kevin.memoverse.data.local.db.MemoverseDatabase
import com.kevin.memoverse.domain.model.DataResult
import com.kevin.memoverse.domain.model.WateringStats
import com.kevin.memoverse.domain.repository.PreferencesRepository
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.IO
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.map
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

    override suspend fun hasSeenOnboarding(): Boolean = try {
        withContext(ioDispatcher) { queries.selectValue(ONBOARDING_SEEN_KEY).executeAsOneOrNull() == TRUE }
    } catch (e: CancellationException) {
        throw e
    } catch (e: Exception) {
        // Base illisible : on ne bloque pas l'entrée dans l'app derrière l'accueil.
        true
    }

    override suspend fun setOnboardingSeen(): DataResult<Unit> = write(ONBOARDING_SEEN_KEY, TRUE)

    override fun observeWateringStats(): Flow<WateringStats> = queries.selectValue(WATERING_STATS_KEY)
        .asFlow()
        .mapToOneOrNull(ioDispatcher)
        .map { it?.toWateringStats() ?: WateringStats() }
        // Base illisible : on affiche des compteurs vides plutôt qu'une erreur.
        .catch { emit(WateringStats()) }

    override suspend fun saveWateringStats(stats: WateringStats): DataResult<Unit> =
        write(WATERING_STATS_KEY, listOf(stats.totalXp, stats.streakDays, stats.lastSessionEpochDay ?: "").joinToString(SEPARATOR))

    /** « xp;série;jour » ; une valeur abîmée compte pour rien. */
    private fun String.toWateringStats(): WateringStats {
        val parts = split(SEPARATOR)
        return WateringStats(
            totalXp = parts.getOrNull(0)?.toIntOrNull() ?: 0,
            streakDays = parts.getOrNull(1)?.toIntOrNull() ?: 0,
            lastSessionEpochDay = parts.getOrNull(2)?.toLongOrNull(),
        )
    }

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
        const val ONBOARDING_SEEN_KEY = "onboarding_seen"
        const val WATERING_STATS_KEY = "watering_stats"
        const val SEPARATOR = ";"
        const val TRUE = "true"
    }
}
