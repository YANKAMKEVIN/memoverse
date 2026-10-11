package com.kevin.memoverse.domain.model

/** Versets cochés avant une première leçon : le cadrage demande d'en choisir 3 ou 4. */
data class VerseSelection(
    val selectedIds: Set<String> = emptySet(),
    val isLimitReached: Boolean = false,
) {
    val canStart: Boolean get() = selectedIds.size in MIN_VERSES..MAX_VERSES

    companion object {
        const val MIN_VERSES = 3
        const val MAX_VERSES = 4
    }
}
