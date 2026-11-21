# État du projet MEMOVERSE

Mis à jour le 21 novembre 2026. À lire en premier pour reprendre le travail dans une session neuve.

## Où en est le code

- `main` (`ec91523`, 20 novembre) : app stable, dépôt privé `YANKAMKEVIN/memoverse` (la CI GitHub ne démarre pas : vérifier en local).
- `claude/suite` : `main` plus l'intégration des deux branches restantes, le test instrumenté sur l'API v2 et ce fichier. À relire par Kevin avant fusion.

Arrivé dans `main` depuis le 31 octobre :

| Quand | Contenu |
|---|---|
| 3 nov. | PR #1 (`chantier/validation`, avec `qa/ui-ux`) en fast-forward jusqu'à `748bbd4` : CI, police Fraunces · Figtree, scènes du jardin (plantation, arrosage), notifications iOS, 7 points majeurs de la QA du 30 octobre, correctif du plantage sur « Continuer ». |
| 15 nov. | Jours de croissance dans le fuseau de l'utilisateur ; « Je t'écoute… » remis à zéro à l'arrêt ; une réponse mal enregistrée ne compte pas. |
| 19 nov. | Verdict annoncé au lecteur d'écran et pas seulement par la couleur ; contrastes WCAG AA ; erreur de lecture du jardin avec « Réessayer » ; « Ouvrir les réglages » après un refus du micro. |
| 20 nov. (`claude/ux15`) | Plantes qui poussent dans la scène, accueil en 3 étapes, retours haptiques, Progression qui liste les versets, XP et série d'arrosages, transition entre exercices, prochain arrosage dans la fiche du jardin, sorties des écrans de chargement et d'erreur de séance, croix et « 2 / 6 », boîtes de dialogue crème (dont « Réinitialiser ton jardin ? »), îles des thèmes, icône et écran de lancement olive, jardin de nuit le soir, bilan animé, typographie française (espaces fines insécables). |

Apporté par `claude/suite` (en plus de `main`) :

- Portrait verrouillé sur téléphone (décision de Kevin du 8 novembre, commit `a883ee2`) : manifeste Android et `Info.plist` côté iPhone ; l'iPad garde ses 4 orientations.
- La part des 30 améliorations de `ui/ameliorations-qa` (`6e06d17`) qui manquait encore à `main` : erreur de lecture de Progression avec « Réessayer », carte « Comment pousse ton jardin », tours d'exercices alternés (1 1 2 1 2 2), texte écrit retrouvé après un passage par l'oral, en-tête masqué clavier ouvert, « Terminé » sur le dernier trou, réglage « réduire les animations » respecté (scènes, goutte, pulsation), mots cachés et trous nommés pour le lecteur d'écran, liens texte en boutons de 48 dp, iOS forcé en clair.
- Non repris : « 25 h = demain » (la fiche du jardin de `main` arrondit les jours au-dessus, testé). Le reste de `6e06d17` est déjà dans `main` sous une autre forme.
- `GardenJourneyTest` sur `androidx.compose.ui.test.junit4.v2.createAndroidComposeRule` (l'ancienne fonction est dépréciée).
- **iOS 18.5 minimum** (décision de Kevin du 21 novembre, **non compilé**) : `IPHONEOS_DEPLOYMENT_TARGET` passe de 15.3 à 18.5 dans les 4 configurations de `iosApp.xcodeproj`, pour supprimer l'avertissement Xcode « object file (…libicu.icudtl_dat.o) was built for newer iOS-simulator version (18.5) than being linked (15.3) ». Origine : Skiko 0.144.6, tiré par Compose Multiplatform 1.11.0. Lu dans les en-têtes Mach-O (`LC_BUILD_VERSION`) de `skiko-iosSimulatorArm64Main-0.144.6.klib` : `icudtl_dat.o` (données ICU, dans `libicu.a` et `libskunicode_icu.a`) a `minos 18.5`, tous les autres objets `14.0` ; côté appareil (`skiko-iosarm64`), tout est en 12.0 ou 14.0. Aucun réglage KMP / Gradle de cible iOS n'existait (le framework statique garde le minimum par défaut de Kotlin/Native, plus bas, sans avertissement) : rien d'autre à changer. Conséquence : les iPhone sous iOS 15.3 à 18.4 ne pourront plus installer l'app. Si Kevin préfère les garder, l'alternative est de ne monter que le simulateur (`IPHONEOS_DEPLOYMENT_TARGET[sdk=iphonesimulator*] = 18.5`) et de laisser 15.3 aux appareils.

Tests : `./gradlew :shared:testAndroidHostTest` → 181/181 sur `claude/suite` (177 sur `main`). `:androidApp:assembleDebug` et `:androidApp:compileDebugAndroidTestKotlin` passent. Côté iOS, non compilé depuis Linux : `ReduceMotion.ios.kt` (repris tel quel de `6e06d17`) et `Info.plist`.

## Branches restantes

- `chantier/validation` (2 commits hors `main`) et `ui/ameliorations-qa` (3 commits) : leur contenu utile est dans `claude/suite`. On peut les supprimer une fois `claude/suite` fusionnée.
- `qa/ui-ux` et les branches `claude/*` sont entièrement dans `main` : on peut les supprimer.

## Reste à faire par Kevin

1. Sur téléphone : `connectedDebugAndroidTest` (9 tests, désormais sur l'API v2), grande police (×1,5), TalkBack, réglage « supprimer les animations », exercice oral avec le vrai micro.
2. Sur iPhone ou simulateur : `./gradlew :shared:allTests`, compilation Xcode, VoiceOver, « Réduire les animations », portrait seul sur iPhone.
3. ~~Décision produit : avertissement Xcode ICU~~ : cible iOS montée à 18.5 le 21 novembre (voir plus haut), **non compilée** : builder dans Xcode (simulateur et iPhone) et vérifier que l'avertissement ICU a disparu. `appintentsmetadataprocessor` est seulement informatif.

## Pièges connus

- Noms de tests entre backticks dans `commonTest` : ni `,` ni `%` ni `. ; : / < > [ ] \`, sinon les tests iOS ne compilent plus (Kotlin/Native).
- Lancer `./gradlew :shared:allTests` sur Mac, pas seulement `testAndroidHostTest` : seul le premier compile pour iOS.
- Textes : espace fine insécable (U+202F) avant `? ! : ; %`.
- Sur iOS, iOS retient la réponse à la demande de notifications : désinstaller l'app du simulateur pour rejouer le parcours d'un nouvel utilisateur.
- Débogage Compose : `Composer.setDiagnosticStackTraceEnabled(true)` dans `MainActivity` (ligne temporaire, à ne pas commiter) ajoute aux plantages la pile des composables.
