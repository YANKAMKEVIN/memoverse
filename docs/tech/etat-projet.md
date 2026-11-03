# État du projet MEMOVERSE

Mis à jour le 3 novembre 2026. À lire en premier pour reprendre le travail dans une session neuve.

## Où en est le code

- `main` (`748bbd4`) : contient tout le travail ci-dessous, poussée sur GitHub (dépôt privé `YANKAMKEVIN/memoverse`). Avancée le 3 novembre en fast-forward depuis `3be4cb8` (sans commit de fusion), sur accord de Kevin ; la PR #1 https://github.com/YANKAMKEVIN/memoverse/pull/1 est marquée fusionnée.
- `chantier/validation` : `main` plus ce commit de mise à jour de l’état du projet (pas encore dans `main`).
- `qa/ui-ux` : corrections de la QA UI/UX (rapport `docs/qa/2026-10-30-qa-ui-ux-main.md`), fusionnées dans `chantier/validation` le 30 octobre, donc dans `main`. PR #2 fermée. Peut être supprimée.

Contenu arrivé dans `main` le 3 novembre :

| Chantier | Résumé |
|---|---|
| CI | `.github/workflows/ci.yml` : Android sur ubuntu-latest, iOS sur macos-26 / Xcode 26.6 (arm64 uniquement, le projet ne cible pas x86_64). Environ 8 min. |
| Police | Fraunces (titres, versets) · Figtree (interface), seule police de l'app. Le sélecteur du Profil et 23 fichiers .ttf ont été retirés. |
| Animations | « Jardin de ton cœur » en Compose Canvas (`ui/ceremony/`, `presentation/ceremony/`) : plantation (graines qui tombent dans un cœur de terre) et arrosage en fin de session, juste avant le bilan (cruche, pousse jusqu'au stade, variante sobre quand un verset fane). 3 à 5 s, passable d'un tap. |
| Notifications iOS | Demandées après la première plantation, comme sur Android ; logique extraite dans `AuthorizationGatedNotifier` (commonMain), testée. |
| Correctifs QA | Chaque exercice repart en haut de l'écran (le défilement de l'exercice précédent était repris, titre caché). Jardin plein : le rond de sélection des versets pâlit. |
| QA UI/UX (`qa/ui-ux`) | Les 7 points majeurs du rapport : en-tête de séance réduit (supprimé en grande police) ; panneau de correction sous l'exercice, plafonné à 55 %, réponse choisie ramenée à l'écran ; grande police (onglets, slogan, cartes de Progression) ; étiquettes du jardin cliquables (fiche « Tes versets ») ; explication avant la demande de notifications et ligne « Rappels d'arrosage » dans le Profil ; jardin plein : « Aller arroser ». Points 8 à 15 (accessibilité, contrastes, micro…) non traités. |
| Xcode | Phase « Compile Kotlin Framework » marquée « exécutée à chaque build » (`alwaysOutOfDate`), comme le recommande la doc Kotlin. |

Tests : `./gradlew :shared:allTests` → iOS 147/147, Android 153/153 (au 31 octobre, après le correctif du plantage sur « Continuer »).

## Validation sur appareil

QA du 30 octobre sur Galaxy S24 (SM-S921B, version debug), pilotée par adb :

- ✅ Séance d'arrosage complète (3 versets, 6 exercices, une erreur volontaire) jusqu'à la scène et au bilan : aucun plantage, bilan juste (+50 XP, 83 %, 3 versets Graine → Racine).
- ✅ Scène d'arrosage : cruche, gouttes et pousse bien dessinées, environ 3 s, fluide (232 images, 1,3 % de saccades, médiane 17 ms).
- ✅ Démarrage à froid en 0,5 s ; la toute première image met 250 à 300 ms (normal en debug).
- 🐛→✅ Défilement repris d'un exercice à l'autre : corrigé dans `SessionScreen` (un défilement neuf par exercice).
- 🐛→✅ Jardin plein : les ronds de sélection paraissaient actifs alors que la liste est verrouillée : corrigé (`VerseRow`).

Corrections `qa/ui-ux` vérifiées sur le S24 (police ×1,0) : étiquettes et fiche du jardin, thème avec jardin plein, ligne « Rappels », séance avec une mauvaise réponse (voir le rapport de QA). Branche fusionnée (`8c08bfd`) revue le 31 octobre sur un **Galaxy A17** (SM-A175F, police ×1,0, installation neuve) :

- ✅ Scène de **plantation** (3 versets, graines dans le cœur de terre, « 3 paroles plantées »).
- ✅ Boîte « Un rappel pour arroser ? » juste après, puis demande système ; ligne « Rappels d'arrosage » du Profil à « Activés ».
- ✅ Étiquettes du jardin bien placées, fiche « Tes versets » (stade, « a soif », texte).
- ✅ Séance, mauvaise réponse : panneau sous l'exercice, réponse choisie visible, pas de blanc.
- ✅ Séance en **paysage**, bonne réponse : la réponse choisie reste visible au-dessus du panneau.
- 🐛 **Paysage** : l'en-tête de séance (190 dp) et la barre « Valider » ne laissent qu'une bande d'environ 300 px pour l'exercice ; il faut faire défiler pour voir les réponses, et la dernière passe sous la barre. L'app n'est pas verrouillée en portrait (pas de `screenOrientation` dans le manifeste). Décision à prendre : verrouiller en portrait, ou réduire l'en-tête en paysage comme en grande police.
- 🐛→✅ **Plantage sur « Continuer »** après chaque exercice (NullPointerException dans Compose, `LayoutNode.onChildRemoved`). Il n'existait que dans la branche fusionnée : `key(state.step)` (défilement remis à zéro, `chantier/validation`) et l'échange panneau ↔ barre « Valider » (`qa/ui-ux`) dans la même `Column` cassaient le suivi des nœuds de Compose. Corrigé par `remember(state.step) { ScrollState(0) }` dans `SessionScreen`. Le plantage renvoyait à l'app précédente, ce qui avait d'abord fait croire à une autre session qui reprenait le téléphone.
- ✅ Après correctif, `GardenJourneyTest` sur l'A17 : 9/9, dont séance complète jusqu'à la scène d'arrosage et au bilan (+60 XP, 100 %), mauvaise réponse, quitter une séance, clavier ouvert, jardin plein. Le test du jardin plein attend maintenant « Jardin plein » et « Aller arroser » (il cherchait encore un « Planter » grisé).
- Astuce : `Composer.setDiagnosticStackTraceEnabled(true)` dans `MainActivity` (ligne temporaire, à ne pas commiter) ajoute aux plantages Compose la pile des composables en cause.

Reste à faire par Kevin (impossible à piloter à distance ou sans changer un réglage du téléphone) :

1. ~~Scène de plantation~~ : vue sur l'A17 le 31 octobre.
2. Exercice oral « goutte » avec le vrai micro.
3. Grande police (×1,5) : onglets, en-tête du jardin, cartes de Progression, séance.
4. iOS : ligne « Rappels » et boîte d'explication (simulateur).
5. Avis à l'œil sur le feuillage de l'olivier.

Ces points se testent maintenant directement sur `main`. Décision en attente : séance en paysage (verrouiller en portrait, ou réduire l'en-tête).

## Petits sujets en attente

- La boîte « Réinitialiser ton jardin ? » a un fond lavande (couleur Material par défaut), hors palette sable.
- Avertissement existant : `createAndroidComposeRule` déprécié dans `GardenJourneyTest` (androidTest).
- Avertissements Xcode connus, sans correction simple : objet ICU « built for newer iOS-simulator version » (il faudrait monter la cible iOS minimale, décision produit) et `appintentsmetadataprocessor` (informatif).
- Pas d'option « animations réduites » (aucune API commune pour la détecter).

## Pièges connus

- Noms de tests entre backticks dans `commonTest` : ni `,` ni `%` ni `. ; : / < > [ ] \`, sinon les tests iOS ne compilent plus (Kotlin/Native).
- Lancer `./gradlew :shared:allTests`, pas seulement `testAndroidHostTest` : seul le premier compile pour iOS.
- Sur iOS, iOS retient la réponse à la demande de notifications : désinstaller l'app du simulateur pour rejouer le parcours d'un nouvel utilisateur.
