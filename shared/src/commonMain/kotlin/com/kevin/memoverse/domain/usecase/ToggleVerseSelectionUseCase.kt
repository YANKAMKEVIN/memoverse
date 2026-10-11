package com.kevin.memoverse.domain.usecase

import com.kevin.memoverse.domain.model.VerseSelection
import com.kevin.memoverse.domain.model.VerseSelection.Companion.MAX_VERSES

class ToggleVerseSelectionUseCase {
    operator fun invoke(selection: VerseSelection, verseId: String): VerseSelection = when {
        verseId in selection.selectedIds ->
            VerseSelection(selectedIds = selection.selectedIds - verseId)
        selection.selectedIds.size >= MAX_VERSES ->
            selection.copy(isLimitReached = true)
        else ->
            VerseSelection(selectedIds = selection.selectedIds + verseId)
    }
}
