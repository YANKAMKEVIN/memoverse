package com.kevin.memoverse.ui.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.unit.dp

/** Texte cliquable : annoncé comme un bouton, avec une cible d'au moins 48 dp de haut. */
fun Modifier.textAction(onClick: () -> Unit): Modifier =
    clickable(role = Role.Button, onClick = onClick).heightIn(min = 48.dp).wrapContentHeight()
