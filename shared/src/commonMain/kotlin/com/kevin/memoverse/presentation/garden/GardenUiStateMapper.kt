package com.kevin.memoverse.presentation.garden

import com.kevin.memoverse.domain.model.Garden
import com.kevin.memoverse.domain.model.LearningVerse
import com.kevin.memoverse.presentation.common.toUi

private const val MINUTE_MILLIS = 60_000L
private const val HOUR_MILLIS = 3_600_000L
private const val HOURS_PER_DAY = 24

fun Garden.toGardenUiState(): GardenUiState = GardenUiState(
    isLoading = false,
    plants = plants.map { GardenPlantUi(it.verseId, it.reference, it.stage.toUi(), it.isThirsty) },
    activeCount = plants.count { it.stage.isActive },
    maxActive = LearningVerse.MAX_ACTIVE_VERSES,
    thirstyCount = thirstyCount,
    nextWatering = millisUntilNextWatering?.toNextWatering(),
)

/** En minutes sous une heure, puis en heures (arrondi supérieur), puis en jours au-delà de 24 h. */
internal fun Long.toNextWatering(): NextWateringUi {
    if (this < HOUR_MILLIS) return NextWateringUi.InMinutes(((this + MINUTE_MILLIS - 1) / MINUTE_MILLIS).toInt().coerceAtLeast(1))
    val hours = ((this + HOUR_MILLIS - 1) / HOUR_MILLIS).toInt().coerceAtLeast(1)
    return if (hours < HOURS_PER_DAY) NextWateringUi.InHours(hours) else NextWateringUi.InDays((hours + HOURS_PER_DAY - 1) / HOURS_PER_DAY)
}
