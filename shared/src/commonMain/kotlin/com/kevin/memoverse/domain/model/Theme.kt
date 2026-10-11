package com.kevin.memoverse.domain.model

data class Theme(
    val id: String,
    val name: String,
    val family: String,
    val verseIds: List<String>,
)
