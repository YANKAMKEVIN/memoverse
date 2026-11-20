package com.kevin.memoverse.presentation.garden

/** Le jardin passe en scène de nuit le soir : de 20 h à 6 h, heure locale. */
fun isNightHour(hour: Int): Boolean = hour >= NIGHT_START_HOUR || hour < NIGHT_END_HOUR

private const val NIGHT_START_HOUR = 20
private const val NIGHT_END_HOUR = 6
