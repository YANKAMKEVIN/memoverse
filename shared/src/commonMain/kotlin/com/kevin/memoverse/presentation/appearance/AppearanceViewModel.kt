package com.kevin.memoverse.presentation.appearance

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.kevin.memoverse.domain.usecase.ObserveTypefaceUseCase
import com.kevin.memoverse.presentation.common.TypefaceUi
import com.kevin.memoverse.presentation.common.toUi
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn

/** Apparence de toute l'app : la police choisie, `null` le temps de la lire (évite un éclair de l'ancienne). */
class AppearanceViewModel(
    observeTypeface: ObserveTypefaceUseCase,
) : ViewModel() {

    val typeface: StateFlow<TypefaceUi?> = observeTypeface()
        .map { it.toUi() }
        .stateIn(viewModelScope, SharingStarted.Eagerly, null)
}
