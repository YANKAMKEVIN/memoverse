package com.kevin.memoverse.presentation.themedetail

import com.kevin.memoverse.presentation.ceremony.CeremonyUi

data class ThemeDetailUiState(
    val isLoading: Boolean = true,
    val hasError: Boolean = false,
    val themeName: String = "",
    val familyName: String = "",
    val verses: List<VerseItemUi> = emptyList(),
    val selectedCount: Int = 0,
    val minSelection: Int = 0,
    val maxSelection: Int = 0,
    val isLimitReached: Boolean = false,
    val canStart: Boolean = false,
    val isStarting: Boolean = false,
    /** Plantation enregistrée et scène terminée : direction le jardin. */
    val isStarted: Boolean = false,
    /** Scène de plantation à jouer, entre l'enregistrement et le retour au jardin. */
    val ceremony: CeremonyUi.Planting? = null,
    val startError: StartErrorUi? = null,
    /** Versets encore plantables avant d'atteindre le plafond de versets actifs. */
    val freeSlots: Int = Int.MAX_VALUE,
    val activeCount: Int = 0,
    val maxActive: Int = 0,
) {
    /** Moins de place que le minimum d'une plantation : on le dit avant que la personne coche. */
    val isGardenFull: Boolean get() = !isLoading && freeSlots < minSelection
}

data class VerseItemUi(
    val id: String,
    val reference: String,
    val text: String,
    val context: String,
    val difficulty: DifficultyUi,
    val isSelected: Boolean,
    val isLearning: Boolean = false,
)

enum class DifficultyUi { EASY, MEDIUM, HARD }

sealed interface StartErrorUi {
    data class TooManyActiveVerses(val activeCount: Int, val maxActive: Int) : StartErrorUi {
        val freeSlots: Int get() = (maxActive - activeCount).coerceAtLeast(0)
    }
    data object SaveFailed : StartErrorUi
}
