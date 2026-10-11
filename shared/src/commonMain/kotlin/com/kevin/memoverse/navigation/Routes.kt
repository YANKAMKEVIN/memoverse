package com.kevin.memoverse.navigation

import kotlinx.serialization.Serializable

@Serializable
data object GardenRoute

@Serializable
data object ExplorerRoute

@Serializable
data object ProgressionRoute

@Serializable
data object ProfileRoute

@Serializable
data class ThemeDetailRoute(val themeId: String)

@Serializable
data object SessionRoute
