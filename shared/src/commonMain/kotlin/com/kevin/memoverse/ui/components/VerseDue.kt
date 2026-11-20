package com.kevin.memoverse.ui.components

import androidx.compose.runtime.Composable
import com.kevin.memoverse.presentation.garden.GardenPlantUi
import com.kevin.memoverse.presentation.garden.NextWateringUi
import com.kevin.memoverse.resources.Res
import com.kevin.memoverse.resources.verse_due_days
import com.kevin.memoverse.resources.verse_due_hours
import com.kevin.memoverse.resources.verse_due_minutes
import com.kevin.memoverse.resources.verse_due_now
import org.jetbrains.compose.resources.pluralStringResource
import org.jetbrains.compose.resources.stringResource

/** « à arroser demain », « à arroser dans 3 h »… ou « a soif » quand c'est maintenant (en minuscule, pour suivre un stade). */
@Composable
fun verseDueText(plant: GardenPlantUi): String = when (val next = plant.nextWatering) {
    null -> stringResource(Res.string.verse_due_now)
    is NextWateringUi.InMinutes -> pluralStringResource(Res.plurals.verse_due_minutes, next.minutes, next.minutes)
    is NextWateringUi.InHours -> pluralStringResource(Res.plurals.verse_due_hours, next.hours, next.hours)
    is NextWateringUi.InDays -> pluralStringResource(Res.plurals.verse_due_days, next.days, next.days)
}
