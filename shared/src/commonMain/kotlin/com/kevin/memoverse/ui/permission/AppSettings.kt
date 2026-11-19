package com.kevin.memoverse.ui.permission

import androidx.compose.runtime.Composable

/**
 * Ouvre la page des réglages de l'app. Après un refus définitif (micro, reconnaissance vocale),
 * le système ne repose plus la question : seuls les réglages permettent de changer d'avis.
 */
@Composable
expect fun rememberOpenAppSettings(): () -> Unit
