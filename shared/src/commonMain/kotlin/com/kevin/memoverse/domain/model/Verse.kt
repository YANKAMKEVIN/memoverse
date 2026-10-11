package com.kevin.memoverse.domain.model

data class Verse(
    val id: String,
    val reference: String,
    val text: String,
    val context: String,
    val wordCount: Int,
    val difficulty: Difficulty,
    val themeIds: List<String>,
)
