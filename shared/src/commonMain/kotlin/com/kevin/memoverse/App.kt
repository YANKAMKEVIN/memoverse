package com.kevin.memoverse

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.kevin.memoverse.presentation.appearance.AppearanceViewModel
import com.kevin.memoverse.domain.usecase.SyncReviewNotificationsUseCase
import com.kevin.memoverse.navigation.AppNavHost
import com.kevin.memoverse.ui.theme.AppTheme
import org.koin.compose.koinInject
import org.koin.compose.viewmodel.koinViewModel

@Composable
fun App() {
    val syncReviewNotifications = koinInject<SyncReviewNotificationsUseCase>()
    // Tant que l'app est ouverte, chaque plantation ou arrosage reprogramme les rappels.
    LaunchedEffect(Unit) { syncReviewNotifications() }
    val appearance = koinViewModel<AppearanceViewModel>()
    // La police est lue en base avant le premier rendu : pas d'éclair de l'ancienne police.
    val typeface by appearance.typeface.collectAsStateWithLifecycle()
    typeface?.let {
        AppTheme(typeface = it) {
            AppNavHost()
        }
    }
}
