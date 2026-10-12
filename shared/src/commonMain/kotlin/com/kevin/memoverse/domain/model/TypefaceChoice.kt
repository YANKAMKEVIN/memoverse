package com.kevin.memoverse.domain.model

/** Les associations de polices proposées ; chacun choisit la sienne dans le Profil. */
enum class TypefaceChoice {
    /** Instrument Serif · Newsreader · Instrument Sans (police d'origine). */
    JARDIN,
    /** Fraunces · Figtree : organique, chaleureuse. */
    FRAUNCES,
    /** Cormorant Garamond · Manrope : littéraire, élancée. */
    CORMORANT,
    /** Gloock · Source Serif 4 · Geist : éditoriale, affirmée. */
    GLOOCK,
    /** EB Garamond · Geist : classique, « Bible imprimée ». */
    GARAMOND;

    companion object {
        val DEFAULT = JARDIN
    }
}
