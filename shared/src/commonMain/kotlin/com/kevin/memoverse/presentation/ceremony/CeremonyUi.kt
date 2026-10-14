package com.kevin.memoverse.presentation.ceremony

import com.kevin.memoverse.presentation.common.StageUi

/**
 * Courte mise en scène du « jardin de ton cœur », jouée une fois après une plantation ou une séance.
 * Le résultat est déjà enregistré : la passer d'un tap ne change rien.
 */
sealed interface CeremonyUi {

    /** Une graine par verset planté tombe dans le cœur ; elle s'y repose, sans germer. */
    data class Planting(val references: List<String>) : CeremonyUi

    /**
     * Arrosage réussi : l'eau est versée, puis la plante repousse depuis la graine jusqu'à [to].
     * Quand [from] est plus petit que [to], le passage au nouveau stade est mis en valeur.
     */
    data class Watering(
        val reference: String,
        val from: StageUi,
        val to: StageUi,
        /** Autres versets qui ont grandi pendant la séance, cités sous la scène. */
        val otherGrownCount: Int = 0,
    ) : CeremonyUi {
        val hasGrown: Boolean get() = to > from
    }

    /** Verset qui fane : la terre sèche et le feuillage s'incline, puis la plante revient à [to]. */
    data class Wilting(val reference: String, val from: StageUi, val to: StageUi) : CeremonyUi
}
