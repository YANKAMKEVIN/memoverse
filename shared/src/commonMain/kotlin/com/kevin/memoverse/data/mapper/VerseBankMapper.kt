package com.kevin.memoverse.data.mapper

import com.kevin.memoverse.data.local.ThemeDto
import com.kevin.memoverse.data.local.VerseDto
import com.kevin.memoverse.domain.model.Difficulty
import com.kevin.memoverse.domain.model.Theme
import com.kevin.memoverse.domain.model.Verse

fun ThemeDto.toDomain(): Theme = Theme(
    id = id,
    name = name,
    family = family,
    verseIds = verseIds,
)

fun VerseDto.toDomain(): Verse = Verse(
    id = id,
    reference = reference,
    text = text,
    context = context,
    wordCount = wordCount,
    difficulty = difficulty.toDifficulty(),
    themeIds = themes,
)

private fun String.toDifficulty(): Difficulty = when (this) {
    "facile" -> Difficulty.EASY
    "difficile" -> Difficulty.HARD
    else -> Difficulty.MEDIUM
}
