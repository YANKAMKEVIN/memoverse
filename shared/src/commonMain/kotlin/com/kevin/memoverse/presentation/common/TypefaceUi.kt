package com.kevin.memoverse.presentation.common

import com.kevin.memoverse.domain.model.TypefaceChoice

/** Association de polices, côté écran (voir [TypefaceChoice]). */
enum class TypefaceUi { JARDIN, FRAUNCES, CORMORANT, GLOOCK, GARAMOND }

fun TypefaceChoice.toUi(): TypefaceUi = when (this) {
    TypefaceChoice.JARDIN -> TypefaceUi.JARDIN
    TypefaceChoice.FRAUNCES -> TypefaceUi.FRAUNCES
    TypefaceChoice.CORMORANT -> TypefaceUi.CORMORANT
    TypefaceChoice.GLOOCK -> TypefaceUi.GLOOCK
    TypefaceChoice.GARAMOND -> TypefaceUi.GARAMOND
}

fun TypefaceUi.toDomain(): TypefaceChoice = when (this) {
    TypefaceUi.JARDIN -> TypefaceChoice.JARDIN
    TypefaceUi.FRAUNCES -> TypefaceChoice.FRAUNCES
    TypefaceUi.CORMORANT -> TypefaceChoice.CORMORANT
    TypefaceUi.GLOOCK -> TypefaceChoice.GLOOCK
    TypefaceUi.GARAMOND -> TypefaceChoice.GARAMOND
}
