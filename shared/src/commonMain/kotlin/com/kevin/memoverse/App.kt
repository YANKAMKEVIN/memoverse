package com.kevin.memoverse

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import com.kevin.memoverse.domain.usecase.SyncReviewNotificationsUseCase
import com.kevin.memoverse.navigation.AppNavHost
import com.kevin.memoverse.ui.theme.AppTheme
import org.koin.compose.koinInject

@Composable
fun App() {
    val syncReviewNotifications = koinInject<SyncReviewNotificationsUseCase>()
    // Tant que l'app est ouverte, chaque plantation ou arrosage reprogramme les rappels.
    LaunchedEffect(Unit) { syncReviewNotifications() }
    AppTheme {
        AppNavHost()
    }
}
