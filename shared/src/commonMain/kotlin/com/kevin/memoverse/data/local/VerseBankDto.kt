package com.kevin.memoverse.data.local

import kotlinx.serialization.Serializable

/** Format de `composeResources/files/verses.json`, produit par `tools/build_verses.py`. */
@Serializable
data class VerseBankDto(
    val translation: String,
    val themes: List<ThemeDto>,
    val verses: List<VerseDto>,
)

@Serializable
data class ThemeDto(
    val id: String,
    val name: String,
    val family: String,
    val verseIds: List<String>,
)

@Serializable
data class VerseDto(
    val id: String,
    val reference: String,
    val text: String,
    val context: String,
    val wordCount: Int,
    val difficulty: String,
    val themes: List<String>,
)
