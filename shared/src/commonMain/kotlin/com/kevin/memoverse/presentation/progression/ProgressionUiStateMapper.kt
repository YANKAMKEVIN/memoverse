package com.kevin.memoverse.presentation.progression

import com.kevin.memoverse.domain.model.Garden
import com.kevin.memoverse.domain.model.GardenPlant
import com.kevin.memoverse.domain.model.MasteryStage
import com.kevin.memoverse.presentation.common.toUi
import com.kevin.memoverse.presentation.garden.toUi

fun Garden.toProgressionUiState(): ProgressionUiState = ProgressionUiState(
    isLoading = false,
    groups = MasteryStage.entries.mapNotNull { stage ->
        plants
            .filter { it.stage == stage }
            .sortedWith(compareByDescending<GardenPlant> { it.isThirsty }.thenBy { it.millisUntilThirsty })
            .takeIf { it.isNotEmpty() }
            ?.let { StageGroupUi(stage.toUi(), it.map { plant -> plant.toUi() }) }
    },
)
