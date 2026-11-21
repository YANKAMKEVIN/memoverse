package com.kevin.memoverse.presentation.progression

import com.kevin.memoverse.presentation.common.StageUi
import com.kevin.memoverse.presentation.garden.GardenPlantUi

data class ProgressionUiState(
    val isLoading: Boolean = true,
    /** Les versets du jardin regroupés par stade, de Graine à Fruit ; les stades vides sont omis. */
    val groups: List<StageGroupUi> = emptyList(),
    /** XP cumulée par les séances d'arrosage. */
    val totalXp: Int = 0,
    /** Jours d'arrosage de suite (0 si la série est rompue). */
    val streakDays: Int = 0,
    /** La lecture du jardin a échoué : à ne pas confondre avec un jardin vide. */
    val hasError: Boolean = false,
) {
    val seedCount: Int get() = countOf(StageUi.SEED)
    val rootCount: Int get() = countOf(StageUi.ROOT)
    val treeCount: Int get() = countOf(StageUi.TREE)
    val fruitCount: Int get() = countOf(StageUi.FRUIT)
    val totalCount: Int get() = groups.sumOf { it.plants.size }

    private fun countOf(stage: StageUi): Int = groups.firstOrNull { it.stage == stage }?.plants?.size ?: 0
}

/** Les versets d'un stade : ceux qui ont soif d'abord, puis par échéance la plus proche. */
data class StageGroupUi(val stage: StageUi, val plants: List<GardenPlantUi>)
