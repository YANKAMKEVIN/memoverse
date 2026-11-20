package com.kevin.memoverse.data.repository

import app.cash.sqldelight.driver.jdbc.sqlite.JdbcSqliteDriver
import com.kevin.memoverse.data.local.db.MemoverseDatabase
import com.kevin.memoverse.domain.model.DataResult
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
    fun `la migration crée la table des réglages sur une base existante`() = runTest(dispatcher) {
        val old = JdbcSqliteDriver(JdbcSqliteDriver.IN_MEMORY)
        MemoverseDatabase.Schema.create(old)
        old.execute(null, "DROP TABLE PreferenceEntity", 0)
        MemoverseDatabase.Schema.migrate(old, 3, 4)
        val migrated = PreferencesRepositoryImpl(MemoverseDatabase(old), dispatcher)

        assertIs<DataResult.Success<Unit>>(migrated.setNotificationPermissionAsked())

        assertEquals(true, migrated.hasAskedNotificationPermission())
    }

    @Test
    fun `retient que la permission de notification a été demandée`() = runTest(dispatcher) {
        assertEquals(false, repository.hasAskedNotificationPermission())

        repository.setNotificationPermissionAsked()

        assertEquals(true, repository.hasAskedNotificationPermission())
    }

    @Test
    fun `une ancienne ligne de police ne compte pas comme permission demandée`() = runTest(dispatcher) {
        database.preferenceQueries.upsert("typeface", "FRAUNCES")

        assertEquals(false, repository.hasAskedNotificationPermission())
    }

    @Test
    fun `retient que l'accueil du premier lancement a été vu`() = runTest(dispatcher) {
        assertEquals(false, repository.hasSeenOnboarding())

        repository.setOnboardingSeen()

        assertEquals(true, repository.hasSeenOnboarding())
        assertEquals(false, repository.hasAskedNotificationPermission())
    }
}
