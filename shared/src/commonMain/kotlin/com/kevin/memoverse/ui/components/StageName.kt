package com.kevin.memoverse.ui.components

import androidx.compose.runtime.Composable
import com.kevin.memoverse.presentation.common.StageUi
import com.kevin.memoverse.resources.Res
import com.kevin.memoverse.resources.stage_fruit
import com.kevin.memoverse.resources.stage_root
import com.kevin.memoverse.resources.stage_seed
import com.kevin.memoverse.resources.stage_tree
import org.jetbrains.compose.resources.stringResource

@Composable
fun stageName(stage: StageUi): String = stringResource(
    when (stage) {
        StageUi.SEED -> Res.string.stage_seed
        StageUi.ROOT -> Res.string.stage_root
        StageUi.TREE -> Res.string.stage_tree
        StageUi.FRUIT -> Res.string.stage_fruit
    },
)
