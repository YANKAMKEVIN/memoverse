package com.kevin.memoverse.ui.components

import androidx.compose.runtime.Composable

/** Vrai si le téléphone demande de réduire les animations (réglage d'accessibilité). */
@Composable
expect fun rememberReduceMotion(): Boolean
