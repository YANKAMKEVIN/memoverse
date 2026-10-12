package com.kevin.memoverse.domain.usecase

import com.kevin.memoverse.fake.FakePreferencesRepository
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class ClaimNotificationPermissionRequestUseCaseTest {

    @Test
    fun `la permission n'est demandée qu'une seule fois`() = runTest {
        val claim = ClaimNotificationPermissionRequestUseCase(FakePreferencesRepository())

        assertTrue(claim())
        assertFalse(claim())
        assertFalse(claim())
    }
}
