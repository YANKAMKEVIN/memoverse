# MEMOVERSE

Application mobile (Android + iOS, Kotlin Multiplatform / Compose Multiplatform) pour mémoriser des versets bibliques, dans la direction visuelle « Jardin vivant » : chaque verset planté pousse de Graine à Fruit au fil des séances d’arrosage.

- 45 thèmes en 7 familles, 422 versets en Louis Segond 1910 (domaine public)
- Séances de 8 types d’exercices (référence, galets, mots manquants, partie floutée, récitation écrite et orale)
- Répétition espacée et rappels locaux ; tout reste sur l’appareil, sans compte

## Structure

- `shared/` : code commun en Clean Architecture (`domain`, `data`, `presentation`, `ui`), ressources Compose (`composeResources` : banque de versets, illustrations, polices)
- `androidApp/`, `iosApp/` : points d’entrée des plateformes
- `data/themes_source.txt` + `tools/build_verses.py` : source éditable de la banque de versets
- `design/` : concepts et illustrations sources

## Commandes

Gradle choisit tout seul un JDK 21 (`gradle/gradle-daemon-jvm.properties`), ou le télécharge.

- Tests unitaires (Android host + simulateur iOS) : `./gradlew :shared:allTests`
- Tests d’acceptance (appareil branché) : `ANDROID_SERIAL=<serial> ./gradlew :androidApp:connectedDebugAndroidTest`
- APK debug : `./gradlew :androidApp:assembleDebug`
- iOS : ouvrir `iosApp/iosApp.xcodeproj`, ou `xcodebuild -project iosApp/iosApp.xcodeproj -scheme iosApp -destination "id=<simulateur>" build`
- CI : GitHub Actions (`.github/workflows/ci.yml`) à chaque push sur `main` et chaque pull request
- Régénérer la banque de versets : `python3 tools/build_verses.py <export ls1910.json de getbible>`

État du projet et prochaines étapes : [`docs/tech/etat-projet.md`](docs/tech/etat-projet.md).
