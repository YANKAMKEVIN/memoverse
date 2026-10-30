package com.kevin.memoverse.presentation.garden

import com.kevin.memoverse.presentation.common.StageUi

data class GardenUiState(
    val isLoading: Boolean = true,
    val plants: List<GardenPlantUi> = emptyList(),
    val activeCount: Int = 0,
    val maxActive: Int = 0,
    val thirstyCount: Int = 0,
    val nextWatering: NextWateringUi? = null,
) {
    val hasPlants: Boolean get() = plants.isNotEmpty()
}

data class GardenPlantUi(
    val verseId: String,
    val reference: String,
    val stage: StageUi,
    val isThirsty: Boolean = false,
    val text: String = "",
)

/** Délai avant le prochain arrosage, arrondi pour l'affichage. */
sealed interface NextWateringUi {
    data class InMinutes(val minutes: Int) : NextWateringUi
    data class InHours(val hours: Int) : NextWateringUi
    data class InDays(val days: Int) : NextWateringUi
}
