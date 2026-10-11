package com.kevin.memoverse.ui.components

import com.kevin.memoverse.resources.Res
import com.kevin.memoverse.resources.ile_mission
import com.kevin.memoverse.resources.ile_moments_de_vie
import com.kevin.memoverse.resources.ile_promesses_de_dieu
import com.kevin.memoverse.resources.ile_relation_avec_dieu
import com.kevin.memoverse.resources.ile_relations
import com.kevin.memoverse.resources.ile_vie_chretienne
import com.kevin.memoverse.resources.ile_vie_interieure
import org.jetbrains.compose.resources.DrawableResource

/** Chaque famille de thèmes a son île ; le nom vient de la banque de versets. */
fun familyIsland(familyName: String): DrawableResource = when (familyName) {
    "Relation avec Dieu" -> Res.drawable.ile_relation_avec_dieu
    "Promesses de Dieu" -> Res.drawable.ile_promesses_de_dieu
    "Vie intérieure" -> Res.drawable.ile_vie_interieure
    "Vie chrétienne" -> Res.drawable.ile_vie_chretienne
    "Relations" -> Res.drawable.ile_relations
    "Moments de vie" -> Res.drawable.ile_moments_de_vie
    else -> Res.drawable.ile_mission
}
