package com.kevin.memoverse.data.repository

import app.cash.sqldelight.driver.jdbc.sqlite.JdbcSqliteDriver
import com.kevin.memoverse.data.local.db.MemoverseDatabase
import com.kevin.memoverse.domain.model.DataResult
import com.kevin.memoverse.domain.model.TypefaceChoice
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertIs

class PreferencesRepositoryImplTest {

    private val dispatcher = StandardTestDispatcher()
    private val driver = JdbcSqliteDriver(JdbcSqliteDriver.IN_MEMORY).also { MemoverseDatabase.Schema.create(it) }
    private val database = MemoverseDatabase(driver)
    private val repository = PreferencesRepositoryImpl(database, dispatcher)

    @Test
    fun `sans réglage, renvoie la police d'origine`() = runTest(dispatcher) {
        assertEquals(TypefaceChoice.JARDIN, repository.observeTypeface().first())
    }

    @Test
    fun `garde la dernière police choisie`() = runTest(dispatcher) {
        assertIs<DataResult.Success<Unit>>(repository.setTypeface(TypefaceChoice.CORMORANT))
        repository.setTypeface(TypefaceChoice.GARAMOND)

        assertEquals(TypefaceChoice.GARAMOND, repository.observeTypeface().first())
    }

    @Test
    fun `une valeur inconnue retombe sur la police d'origine`() = runTest(dispatcher) {
        database.preferenceQueries.upsert("typeface", "POLICE_RETIREE")

        assertEquals(TypefaceChoice.JARDIN, repository.observeTypeface().first())
    }

    @Test
    fun `la migration crée la table des réglages sur une base existante`() = runTest(dispatcher) {
        val old = JdbcSqliteDriver(JdbcSqliteDriver.IN_MEMORY)
        MemoverseDatabase.Schema.create(old)
        old.execute(null, "DROP TABLE PreferenceEntity", 0)
        MemoverseDatabase.Schema.migrate(old, 3, 4)
        val migrated = PreferencesRepositoryImpl(MemoverseDatabase(old), dispatcher)

        migrated.setTypeface(TypefaceChoice.FRAUNCES)

        assertEquals(TypefaceChoice.FRAUNCES, migrated.observeTypeface().first())
    }

    @Test
    fun `retient que la permission de notification a été demandée`() = runTest(dispatcher) {
        assertEquals(false, repository.hasAskedNotificationPermission())

        repository.setNotificationPermissionAsked()

        assertEquals(true, repository.hasAskedNotificationPermission())
    }
}
