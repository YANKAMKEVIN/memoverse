# État du projet MEMOVERSE

Mis à jour le 30 octobre 2026. À lire en premier pour reprendre le travail dans une session neuve.

## Où en est le code

- `main` (`3be4cb8`) : app stable, poussée sur GitHub (dépôt privé `YANKAMKEVIN/memoverse`).
- `chantier/validation` (`9d89063` puis ce fichier) : **en attente de validation sur téléphone** avant fusion dans `main`. Pull request en brouillon : https://github.com/YANKAMKEVIN/memoverse/pull/1 (CI verte, fusion sans conflit).

Contenu de `chantier/validation` :

| Chantier | Résumé |
|---|---|
| CI | `.github/workflows/ci.yml` : Android sur ubuntu-latest, iOS sur macos-26 / Xcode 26.6 (arm64 uniquement, le projet ne cible pas x86_64). Environ 8 min. |
| Police | Fraunces (titres, versets) · Figtree (interface), seule police de l'app. Le sélecteur du Profil et 23 fichiers .ttf ont été retirés. |
| Animations | « Jardin de ton cœur » en Compose Canvas (`ui/ceremony/`, `presentation/ceremony/`) : plantation (graines qui tombent dans un cœur de terre) et arrosage en fin de session, juste avant le bilan (cruche, pousse jusqu'au stade, variante sobre quand un verset fane). 3 à 5 s, passable d'un tap. |
| Notifications iOS | Demandées après la première plantation, comme sur Android ; logique extraite dans `AuthorizationGatedNotifier` (commonMain), testée. |
| Xcode | Phase « Compile Kotlin Framework » marquée « exécutée à chaque build » (`alwaysOutOfDate`), comme le recommande la doc Kotlin. |

Tests : `./gradlew :shared:allTests` → iOS 147/147, Android 153/153 (au 14 octobre).

## Validation sur appareil (à faire par Kevin)

1. Une séance d'arrosage complète jusqu'à la scène finale et au bilan (jamais jouée de bout en bout, seulement vue via un écran de test).
2. Fluidité de la première animation après le lancement (saccades vues sur simulateur).
3. Durée (3 à 5 s) et dessin des scènes ; le feuillage de l'olivier est un peu rond.
4. Exercice oral « goutte » avec le vrai micro (jamais testé sur appareil).

Si tout va : fusionner la PR #1 dans `main`. Sinon : corriger sur `chantier/validation`.

## Petits sujets en attente

- La boîte « Réinitialiser ton jardin ? » a un fond lavande (couleur Material par défaut), hors palette sable.
- Avertissement existant : `createAndroidComposeRule` déprécié dans `GardenJourneyTest` (androidTest).
- Avertissements Xcode connus, sans correction simple : objet ICU « built for newer iOS-simulator version » (il faudrait monter la cible iOS minimale, décision produit) et `appintentsmetadataprocessor` (informatif).
- Pas d'option « animations réduites » (aucune API commune pour la détecter).

## Pièges connus

- Noms de tests entre backticks dans `commonTest` : ni `,` ni `%` ni `. ; : / < > [ ] \`, sinon les tests iOS ne compilent plus (Kotlin/Native).
- Lancer `./gradlew :shared:allTests`, pas seulement `testAndroidHostTest` : seul le premier compile pour iOS.
- Sur iOS, iOS retient la réponse à la demande de notifications : désinstaller l'app du simulateur pour rejouer le parcours d'un nouvel utilisateur.
