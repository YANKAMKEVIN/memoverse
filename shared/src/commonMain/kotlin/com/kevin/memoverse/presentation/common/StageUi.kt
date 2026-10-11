package com.kevin.memoverse.presentation.common

import com.kevin.memoverse.domain.model.MasteryStage

enum class StageUi { SEED, ROOT, TREE, FRUIT }

fun MasteryStage.toUi(): StageUi = when (this) {
    MasteryStage.SEED -> StageUi.SEED
    MasteryStage.ROOT -> StageUi.ROOT
    MasteryStage.TREE -> StageUi.TREE
    MasteryStage.FRUIT -> StageUi.FRUIT
}
