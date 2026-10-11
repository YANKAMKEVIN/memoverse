package com.kevin.memoverse.ui.permission

import androidx.compose.runtime.Composable

/**
 * Renvoie une action qui demande l'accès au micro (et, sur iOS, à la reconnaissance vocale),
 * puis appelle [onResult]. Si l'accès est déjà accordé, [onResult] est appelé tout de suite.
 */
@Composable
expect fun rememberMicrophonePermissionRequest(onResult: (granted: Boolean) -> Unit): () -> Unit
