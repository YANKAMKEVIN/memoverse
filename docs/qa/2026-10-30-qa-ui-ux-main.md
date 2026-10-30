# QA UI/UX : MEMOVERSE, branche `main` (3be4cb8), 30/10/2026

Méthode : lecture du code (séance et autres écrans) + parcours réel sur le Galaxy S24 (SM-S921B, 1080×2340, police ×1,0, ×1,5 et ×2,0), arbre d'accessibilité lu via uiautomator.
Légende : **[S24]** = vu sur l'appareil ; **[code]** = relevé dans le code, non reproduit.
Chemins relatifs à `shared/src/commonMain/kotlin/com/kevin/memoverse/`.

## Suivi des corrections (branche `qa/ui-ux`, partie de `main`)

Points majeurs 1 à 7 corrigés ; tests `:shared:allTests` 274/274 (iOS et Android).

Vérifié sur le S24 le 30/10 vers 16 h 40 (police ×1,0) : étiquettes du jardin et fiche « Tes versets », marge au bord, thème avec jardin plein (« Jardin plein · Aller arroser » mène au jardin), ligne « Rappels » (état lu, « Activer » ouvre les réglages de notifications de l'app), séance « Retrouve la référence » avec un verset long et une mauvaise réponse. Cette vérification a fait corriger deux défauts : la réponse choisie restait hors de l'écran (elle y revient désormais) et un blanc restait sous le panneau (hauteur plafonnée à 55 %).
Non vérifié sur appareil : grande police (réglage système non modifié), demande « Un rappel pour arroser ? » (elle ne s'affiche qu'une fois, après la première plantation), retrait du fondu après validation, iOS.

| # | Correction |
|---|---|
| 1 | En-tête illustré de séance réduit (260 → 190 dp) et supprimé en grande police (≥ ×1,3). |
| 2 | `key(state.step) { rememberScrollState() }` : chaque exercice repart en haut. |
| 3 | Panneau de correction placé sous l'exercice (plus par-dessus), au plus 55 % de l'écran, réponse choisie ramenée à l'écran, fondu retiré après validation, texte défilant et « Continuer » toujours visible ; le verset n'est plus répété quand l'exercice l'affiche déjà. |
| 4 | Libellés d'onglets sur une ligne avec taille automatique ; slogan du jardin qui passe à la ligne au lieu de chevaucher ; cartes de Progression à hauteur minimale, image à côté du texte. |
| 5 | Étiquettes du jardin cliquables (fiche « Tes versets » : référence, stade, soif, texte) et gardées à 12 dp des bords. |
| 6 | Explication avant la demande système (« Un rappel pour arroser ? ») ; ligne « Rappels d'arrosage » dans le Profil (état + Activer / Réglages, relue au retour dans l'app). |
| 7 | Jardin plein : ronds de sélection pâlis, barre du bas « Jardin plein · Aller arroser » vers le jardin. |

## Majeurs

1. **[S24] Les réponses de « Retrouve la référence » sont sous la ligne de flottaison.** À ×1,0, 2 propositions sur 4 sont visibles ; à ×1,5, aucune. La bonne réponse était la 4e, cachée sous « Valider », sans indice de défilement. Réduire l'en-tête illustré pendant la séance, ou placer les propositions avant le verset.
2. **[S24] Le défilement est conservé d'un exercice à l'autre** : le titre de l'exercice suivant est coupé en haut. `ui/session/SessionScreen.kt:151`, utiliser `key(step) { rememberScrollState() }`.
3. **[S24] Panneau de correction : ta réponse disparaît.** Il recouvre les propositions et les galets, donc on ne voit plus ce qu'on a choisi ni quels galets étaient mal placés. Le verset est affiché deux fois. À ×2,0, le panneau occupe presque tout l'écran ; « Continuer » reste visible sur le S24 mais sera coupé avec un verset plus long ou un écran plus petit (pas de défilement : `ui/session/FeedbackPanel.kt:47-86`).
4. **[S24] Grande police (×1,5) : mise en page cassée.**
   - Onglets coupés au milieu du mot (« Progressio / n »).
   - Jardin : « MEMOVERSE » chevauche « Le Jardin vivant ».
   - Progression : le chiffre recouvre la légende, les images recouvrent le texte (cartes à hauteur fixe, `ui/progression/ProgressionScreen.kt:111-131`).
5. **[S24] Étiquettes du jardin non cliquables et au bord de l'écran.** Aucun moyen de voir ses versets plantés hors séance. « Éphésiens 2:8-9 · a soif » finit à 14 px du bord sur le S24 ; débordera sur un écran plus étroit (`ui/garden/GardenScreen.kt:214-253`).
6. **[S24] Notifications : demande système sans explication préalable, et aucun réglage « Rappels » dans le Profil.** Après un refus, rien ne permet de les réactiver (`navigation/AppNavHost.kt:82`, `ProfileUiState.kt`).
7. **[S24] Jardin plein (4/5) : impasse dans les thèmes.** Lignes désactivées sans changement visuel, pas de bouton « Arroser ». Avec 4/5 et un minimum de 3 versets, il est impossible de planter tant que rien n'a mûri ; le message l'explique mais n'offre pas d'action.
8. **[code] Erreur de base = jardin vide.** `presentation/garden/GardenViewModel.kt:18` émet un état vide : l'utilisateur croit avoir perdu ses versets.
9. **[code] Lecteurs d'écran.**
   - Résultat « Juste. / Presque. » jamais annoncé : aucun `liveRegion` dans le projet (`FeedbackPanel.kt:47`).
   - Verdict des propositions lisible seulement à la couleur (`ui/components/AnswerOption.kt:34-48`).
   - Mots cachés sans description (`VerseWithHiddenWords.kt:34`).
   - Trous et champs de saisie sans libellé (`FillBlanksExercise.kt:49-62`, `TypeBlanksExercise.kt:52-70`).
   - Barre de progression de séance muette (`SessionScreen.kt:227-236`).
   - [S24] Police choisie dans Profil non annoncée comme sélectionnée : la carte cochée n'est ni cliquable ni « checked ».
10. **[code] Contrastes insuffisants (WCAG AA).**
    - Texte secondaire InkMuted #7C7568 : 3,7 à 4,3:1 partout ; foncer vers #6A6457.
    - Onglets inactifs : 2,55:1 (`AppTabBar.kt:60`).
    - Option sélectionnée, blanc sur Sage : 2,99:1 (`AnswerOption.kt:37,43`).
    - Bouton désactivé : 1,43:1 (`PillButton.kt:37-38`).
    - Textes du panneau d'erreur : 3,5 à 4,4:1.
11. **[code] Micro refusé = impasse.** Pas de bouton « Ouvrir les réglages » ; sur iOS, le message parle du micro quand c'est la reconnaissance vocale qui est refusée (`MicrophonePermission.android.kt:18-25`, `ReciteAloudExercise.kt:85-87`).
12. **[code] « Je t'écoute… » reste affiché après « Valider »** : `stopListening()` annule le job avant `isListening = false` (`presentation/session/SessionViewModel.kt:129-132`).
13. **[code] Clavier ouvert sur petit écran** : en-tête fixe de 164 dp + barres, il reste environ 30 dp pour l'exercice (`SessionScreen.kt:143-150`). Non testé : les exercices à saisie n'apparaissent pas dans une première séance.
14. **[code] Écrans de chargement et d'erreur de séance sans sortie visible** (seulement le retour système ; impasse probable sur iOS) (`SessionScreen.kt:117-118`).
15. **[code] iOS mode sombre : barre de statut probablement blanche sur fond clair** (pas de `UIUserInterfaceStyle = Light` dans `Info.plist`).

## Mineurs

- **[S24]** Profil : « Traduction » collé à sa valeur (« TraductionLouis Segond… »).
- **[S24]** Première séance : 4 exercices « Retrouve la référence » d'affilée puis 4 « Remets les mots dans l'ordre », sentiment de répétition.
- **[S24]** L'anneau « 4/5 en apprentissage » et la notion d'XP (« +60 XP ») ne sont expliqués nulle part.
- **[S24]** « Sélectionné » annoncé en plus de l'état coché sur les versets (`VerseRow.kt:96`).
- **[code]**
  - Cibles tactiles < 48 dp et `clickable` sans rôle bouton : retour du thème, puces de thème, « Planter de nouveaux versets », « Réinitialiser », « Écrire plutôt », galets, trous à 34 dp.
  - Animations en boucle sans respect de « réduire les animations » (`WaterDrop.kt:53-56`).
  - Séance perdue si le processus est tué (pas de `SavedStateHandle`).
  - Dernier champ à trous en `ImeAction.Next` au lieu de `Done`.
  - « Réciter à voix haute plutôt » efface le texte saisi sans confirmation.
  - Progression : pas d'état chargement, erreur ou vide.
  - « dans 2 jours » pour une échéance de demain (`GardenUiStateMapper.kt:24`).
  - Contenu de Progression qui défile sous la barre de statut.

## Cosmétiques

- Aucune espace insécable (U+202F) avant `: ; ! ? %` ni dans les « » : le « ? » peut se retrouver seul en début de ligne.
- « place(s) » à passer en `<plurals>`.
- Version « 0.1.0 » codée en dur.
- Couleurs, espacements et tailles en dur hors thème (Color.White, #FBF7F0 dupliqué, 5/6/34 dp, `TypefaceOption` 26/13/12 sp) ; pas de thème sombre possible en l'état.
- Seuil de défilement du détail de thème en pixels (`ThemeDetailScreen.kt:259`).

## Ce qui fonctionne bien

- Sélection des versets : cases à cocher accessibles, limite de 4 refusée avec un message clair.
- Confirmation « Quitter l'arrosage ? », également sur le retour système.
- Double soumission empêchée.
- Autocorrection désactivée sur les saisies.
- Onglets avec état sauvegardé.
- Lien des notifications vers la séance.
- Pluriels « 1 verset / 2 versets ».
- Bilan de séance clair.

## Non couvert

- Exercices à saisie, mots manquants, partie floutée et récitation orale : absents d'une première séance.
- iOS.
- Paysage et tablette.
- TalkBack réellement activé : l'arbre d'accessibilité a été lu, pas écouté.
