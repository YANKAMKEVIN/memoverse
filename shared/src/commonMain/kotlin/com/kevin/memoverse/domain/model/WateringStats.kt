package com.kevin.memoverse.domain.model

/**
 * Ce que les séances d'arrosage ont rapporté : l'XP cumulée et la série de jours d'arrosage
 * (jours calendaires de l'utilisateur, consécutifs, avec au moins une séance terminée).
 */
data class WateringStats(
    val totalXp: Int = 0,
    val streakDays: Int = 0,
    val lastSessionEpochDay: Long? = null,
) {
    /** Après une séance terminée [today] : l'XP s'ajoute, la série continue (veille), reste (même jour) ou repart à 1. */
    fun afterSession(xp: Int, today: Long): WateringStats = WateringStats(
        totalXp = totalXp + xp,
        streakDays = when (lastSessionEpochDay) {
            today -> streakDays.coerceAtLeast(1)
            today - 1 -> streakDays + 1
            else -> 1
        },
        lastSessionEpochDay = today,
    )

    /** La série telle qu'elle se lit [today] : encore vivante si la dernière séance date d'hier ou d'aujourd'hui. */
    fun currentStreak(today: Long): Int {
        val last = lastSessionEpochDay ?: return 0
        return if (today - last <= 1) streakDays else 0
    }
}
