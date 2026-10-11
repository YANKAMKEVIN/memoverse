package com.kevin.memoverse.domain.model

/** Jauge de maîtrise d'un verset : Graine → Racine → Arbre → Fruit. */
enum class MasteryStage {
    SEED,
    ROOT,
    TREE,
    FRUIT;

    /** Un verset « actif » est encore en apprentissage : il compte dans le plafond de versets actifs. */
    val isActive: Boolean get() = this == SEED || this == ROOT
}
