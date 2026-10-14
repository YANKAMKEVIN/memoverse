package com.kevin.memoverse.domain.usecase

import com.kevin.memoverse.domain.notification.PlannedNotification
import com.kevin.memoverse.domain.repository.NotificationScheduler
import com.kevin.memoverse.fake.FakeLearningRepository
import com.kevin.memoverse.fake.FakeVerseBankRepository
import com.kevin.memoverse.fake.learningVerse
import com.kevin.memoverse.fake.verse
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import kotlinx.datetime.TimeZone
import kotlin.test.Test
import kotlin.test.assertEquals

@OptIn(ExperimentalCoroutinesApi::class)
class SyncReviewNotificationsUseCaseTest {

    private class RecordingScheduler : NotificationScheduler {
        val calls = mutableListOf<List<PlannedNotification>>()
        override suspend fun replaceAll(notifications: List<PlannedNotification>) {
            calls += notifications
        }
    }

    @Test
    fun `reprogramme les rappels à chaque changement du jardin`() = runTest {
        val learning = FakeLearningRepository()
        val scheduler = RecordingScheduler()
        val bank = FakeVerseBankRepository(verses = listOf(verse("v1")))
        // 10 h UTC : en dehors des heures calmes.
        val now = 10 * 3_600_000L
        val job = launch { SyncReviewNotificationsUseCase(learning, bank, scheduler, { now }, { TimeZone.UTC })() }
        advanceUntilIdle()

        learning.verses.value = listOf(learningVerse("v1").copy(dueAtEpochMillis = now))
        advanceUntilIdle()
        job.cancel()

        assertEquals(listOf(emptyList<String>(), listOf("Référence v1")), scheduler.calls.map { calls -> calls.flatMap { it.references } })
    }
}
