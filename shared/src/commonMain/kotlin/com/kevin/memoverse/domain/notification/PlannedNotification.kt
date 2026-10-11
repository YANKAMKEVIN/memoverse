package com.kevin.memoverse.domain.notification

import com.kevin.memoverse.domain.model.HiddenPosition

/** Un rappel d'arrosage à afficher à [atEpochMillis] pour les versets [references]. */
data class PlannedNotification(
    val atEpochMillis: Long,
    val references: List<String>,
    /** Partie du verset évoquée dans le texte (« la fin du verset »), variée d'un rappel à l'autre. */
    val part: HiddenPosition,
)
