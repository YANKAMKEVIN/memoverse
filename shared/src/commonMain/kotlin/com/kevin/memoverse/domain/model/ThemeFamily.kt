package com.kevin.memoverse.domain.model

/** Thèmes regroupés par famille, dans l'ordre de la banque de versets. */
data class ThemeFamily(
    val name: String,
    val themes: List<Theme>,
)
