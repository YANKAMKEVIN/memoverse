package com.kevin.memoverse.navigation

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.navigation.NavDestination
import androidx.navigation.NavDestination.Companion.hasRoute
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.toRoute
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.kevin.memoverse.data.notification.NotificationEntry
import com.kevin.memoverse.domain.usecase.ClaimNotificationPermissionRequestUseCase
import com.kevin.memoverse.presentation.onboarding.OnboardingViewModel
import com.kevin.memoverse.resources.Res
import com.kevin.memoverse.resources.reminder_prompt_confirm
import com.kevin.memoverse.resources.reminder_prompt_later
import com.kevin.memoverse.resources.reminder_prompt_message
import com.kevin.memoverse.resources.reminder_prompt_title
import com.kevin.memoverse.ui.components.AppTab
import com.kevin.memoverse.ui.components.AppTabBar
import com.kevin.memoverse.ui.components.GardenDialog
import com.kevin.memoverse.ui.explorer.ExplorerScreen
import com.kevin.memoverse.ui.garden.GardenScreen
import com.kevin.memoverse.ui.onboarding.OnboardingScreen
import com.kevin.memoverse.ui.permission.rememberNotificationPermissionRequest
import com.kevin.memoverse.ui.profile.ProfileScreen
import com.kevin.memoverse.ui.progression.ProgressionScreen
import com.kevin.memoverse.ui.session.SessionScreen
import com.kevin.memoverse.ui.themedetail.ThemeDetailScreen
import kotlinx.coroutines.launch
import org.jetbrains.compose.resources.stringResource
import org.koin.compose.koinInject
import org.koin.compose.viewmodel.koinViewModel

@Composable
fun AppNavHost() {
    // Premier lancement : l'accueil en trois volets passe avant le jardin, une seule fois.
    val onboarding = koinViewModel<OnboardingViewModel>()
    val showOnboarding by onboarding.showOnboarding.collectAsStateWithLifecycle()
    when (showOnboarding) {
        null -> Box(Modifier.fillMaxSize().background(MaterialTheme.colorScheme.background))
        true -> OnboardingScreen(onFinished = onboarding::onFinished)
        false -> MainNavHost()
    }
}

@Composable
private fun MainNavHost() {
    val navController = rememberNavController()
    val backStackEntry by navController.currentBackStackEntryAsState()
    val currentTab = backStackEntry?.destination?.toTab()
    val requestNotificationPermission = rememberNotificationPermissionRequest()
    val claimNotificationPermissionRequest = koinInject<ClaimNotificationPermissionRequestUseCase>()
    val scope = rememberCoroutineScope()
    // Explication avant la demande système : sans elle, la question tombait sans contexte.
    var isReminderPromptShown by rememberSaveable { mutableStateOf(false) }
    val pendingSessionOpen by NotificationEntry.pendingSessionOpen.collectAsState()
    LaunchedEffect(pendingSessionOpen) {
        if (pendingSessionOpen) {
            NotificationEntry.consumeSessionOpen()
            navController.navigate(SessionRoute) { launchSingleTop = true }
        }
    }
    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        contentWindowInsets = WindowInsets(0),
        bottomBar = {
            if (currentTab != null) AppTabBar(selected = currentTab, onSelect = { navController.navigateToTab(it) })
        },
    ) { innerPadding ->
        NavHost(navController = navController, startDestination = GardenRoute, modifier = Modifier.fillMaxSize()) {
            composable<GardenRoute> {
                GardenScreen(
                    contentPadding = innerPadding,
                    onPlantClick = { navController.navigateToTab(AppTab.EXPLORER) },
                    onWaterClick = { navController.navigate(SessionRoute) },
                )
            }
            composable<ExplorerRoute> {
                ExplorerScreen(contentPadding = innerPadding, onThemeClick = { navController.navigate(ThemeDetailRoute(it)) })
            }
            composable<ProgressionRoute> {
                ProgressionScreen(contentPadding = innerPadding, onPlantClick = { navController.navigateToTab(AppTab.EXPLORER) })
            }
            composable<ProfileRoute> { ProfileScreen(contentPadding = innerPadding) }
            composable<SessionRoute> { SessionScreen(onClose = { navController.popBackStack() }) }
            composable<ThemeDetailRoute> { entry ->
                ThemeDetailScreen(
                    themeId = entry.toRoute<ThemeDetailRoute>().themeId,
                    onBack = { navController.popBackStack() },
                    onGardenClick = {
                        navController.popBackStack<ExplorerRoute>(inclusive = false)
                        navController.navigateToTab(AppTab.GARDEN)
                    },
                    // Une fois planté, on montre le jardin : c'est là que les versets vivent. On retire d'abord
                    // l'écran du thème, sinon l'onglet Explorer le restaurerait (état « planté ») et
                    // renverrait aussitôt au jardin.
                    onLearningStarted = {
                        navController.popBackStack<ExplorerRoute>(inclusive = false)
                        navController.navigateToTab(AppTab.GARDEN)
                        // Les rappels prennent leur sens maintenant que des versets sont plantés ; on ne demande qu'une fois.
                        scope.launch { if (claimNotificationPermissionRequest()) isReminderPromptShown = true }
                    },
                )
            }
        }
    }
    if (isReminderPromptShown) {
        GardenDialog(
            title = stringResource(Res.string.reminder_prompt_title),
            message = stringResource(Res.string.reminder_prompt_message),
            confirmText = stringResource(Res.string.reminder_prompt_confirm),
            onConfirm = {
                isReminderPromptShown = false
                requestNotificationPermission()
            },
            dismissText = stringResource(Res.string.reminder_prompt_later),
            onDismiss = { isReminderPromptShown = false },
        )
    }
}

private fun NavDestination.toTab(): AppTab? = when {
    hasRoute(GardenRoute::class) -> AppTab.GARDEN
    hasRoute(ExplorerRoute::class) -> AppTab.EXPLORER
    hasRoute(ProgressionRoute::class) -> AppTab.PROGRESSION
    hasRoute(ProfileRoute::class) -> AppTab.PROFILE
    else -> null
}

private fun NavHostController.navigateToTab(tab: AppTab) {
    val route: Any = when (tab) {
        AppTab.GARDEN -> GardenRoute
        AppTab.EXPLORER -> ExplorerRoute
        AppTab.PROGRESSION -> ProgressionRoute
        AppTab.PROFILE -> ProfileRoute
    }
    navigate(route) {
        popUpTo<GardenRoute> { saveState = true }
        launchSingleTop = true
        restoreState = true
    }
}
