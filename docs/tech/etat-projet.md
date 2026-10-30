# État du projet MEMOVERSE

Mis à jour le 30 octobre 2026. À lire en premier pour reprendre le travail dans une session neuve.

## Où en est le code

- `main` (`3be4cb8`) : app stable, poussée sur GitHub (dépôt privé `YANKAMKEVIN/memoverse`).
- `chantier/validation` : **tout le travail en cours, réuni en une seule branche**, en attente des derniers contrôles sur téléphone avant fusion dans `main` par la pull request en brouillon https://github.com/YANKAMKEVIN/memoverse/pull/1.
- `qa/ui-ux` : corrections de la QA UI/UX de `main` (rapport `docs/qa/2026-10-30-qa-ui-ux-main.md`), **fusionnées dans `chantier/validation` le 30 octobre** (décision de Kevin : tout passe par la PR #1, `main` ne bouge qu'une fois tout vérifié). La PR #2 (`qa/ui-ux` → `main`) est fermée au profit de la PR #1.

Contenu de `chantier/validation` :

| Chantier | Résumé |
|---|---|
| CI | `.github/workflows/ci.yml` : Android sur ubuntu-latest, iOS sur macos-26 / Xcode 26.6 (arm64 uniquement, le projet ne cible pas x86_64). Environ 8 min. |
| Police | Fraunces (titres, versets) · Figtree (interface), seule police de l'app. Le sélecteur du Profil et 23 fichiers .ttf ont été retirés. |
| Animations | « Jardin de ton cœur » en Compose Canvas (`ui/ceremony/`, `presentation/ceremony/`) : plantation (graines qui tombent dans un cœur de terre) et arrosage en fin de session, juste avant le bilan (cruche, pousse jusqu'au stade, variante sobre quand un verset fane). 3 à 5 s, passable d'un tap. |
| Notifications iOS | Demandées après la première plantation, comme sur Android ; logique extraite dans `AuthorizationGatedNotifier` (commonMain), testée. |
| Correctifs QA | Chaque exercice repart en haut de l'écran (le défilement de l'exercice précédent était repris, titre caché). Jardin plein : le rond de sélection des versets pâlit. |
| QA UI/UX (`qa/ui-ux`) | Les 7 points majeurs du rapport : en-tête de séance réduit (supprimé en grande police) ; panneau de correction sous l'exercice, plafonné à 55 %, réponse choisie ramenée à l'écran ; grande police (onglets, slogan, cartes de Progression) ; étiquettes du jardin cliquables (fiche « Tes versets ») ; explication avant la demande de notifications et ligne « Rappels d'arrosage » dans le Profil ; jardin plein : « Aller arroser ». Points 8 à 15 (accessibilité, contrastes, micro…) non traités. |
| Xcode | Phase « Compile Kotlin Framework » marquée « exécutée à chaque build » (`alwaysOutOfDate`), comme le recommande la doc Kotlin. |

Tests : `./gradlew :shared:allTests` → iOS 147/147, Android 153/153 (au 30 octobre, après la fusion de `qa/ui-ux`).

## Validation sur appareil

QA du 30 octobre sur Galaxy S24 (SM-S921B, version debug), pilotée par adb :

- ✅ Séance d'arrosage complète (3 versets, 6 exercices, une erreur volontaire) jusqu'à la scène et au bilan : aucun plantage, bilan juste (+50 XP, 83 %, 3 versets Graine → Racine).
- ✅ Scène d'arrosage : cruche, gouttes et pousse bien dessinées, environ 3 s, fluide (232 images, 1,3 % de saccades, médiane 17 ms).
- ✅ Démarrage à froid en 0,5 s ; la toute première image met 250 à 300 ms (normal en debug).
- 🐛→✅ Défilement repris d'un exercice à l'autre : corrigé (`key(state.step)` dans `SessionScreen`).
- 🐛→✅ Jardin plein : les ronds de sélection paraissaient actifs alors que la liste est verrouillée : corrigé (`VerseRow`).

Corrections `qa/ui-ux` vérifiées sur le S24 (police ×1,0) : étiquettes et fiche du jardin, thème avec jardin plein, ligne « Rappels », séance avec une mauvaise réponse (voir le rapport de QA). **Branche fusionnée pas encore revue sur le S24** (téléphone pris par une autre QA le 30 octobre en fin d'après-midi) : à faire avant de pousser.

Reste à faire par Kevin (impossible à piloter à distance ou sans changer un réglage du téléphone) :

1. Scène de **plantation** : jardin plein (3/5 et 3 versets minimum par plantation), donc non testée. À faire quand une place se libère ; c'est aussi là qu'apparaît la boîte « Un rappel pour arroser ? » (une seule fois).
2. Exercice oral « goutte » avec le vrai micro.
3. Grande police (×1,5) : onglets, en-tête du jardin, cartes de Progression, séance.
4. iOS : ligne « Rappels » et boîte d'explication (simulateur).
5. Avis à l'œil sur le feuillage de l'olivier.

Si ces points vont : fusionner la PR #1 dans `main`.

## Petits sujets en attente

- La boîte « Réinitialiser ton jardin ? » a un fond lavande (couleur Material par défaut), hors palette sable.
- Avertissement existant : `createAndroidComposeRule` déprécié dans `GardenJourneyTest` (androidTest).
- Avertissements Xcode connus, sans correction simple : objet ICU « built for newer iOS-simulator version » (il faudrait monter la cible iOS minimale, décision produit) et `appintentsmetadataprocessor` (informatif).
- Pas d'option « animations réduites » (aucune API commune pour la détecter).

## Pièges connus

- Noms de tests entre backticks dans `commonTest` : ni `,` ni `%` ni `. ; : / < > [ ] \`, sinon les tests iOS ne compilent plus (Kotlin/Native).
- Lancer `./gradlew :shared:allTests`, pas seulement `testAndroidHostTest` : seul le premier compile pour iOS.
- Sur iOS, iOS retient la réponse à la demande de notifications : désinstaller l'app du simulateur pour rejouer le parcours d'un nouvel utilisateur.
