# MEMOVERSE — Trois animations pour le jardin de mon cœur

Trois propositions pour une app adulte, ludique et apaisée. Ma préférence va aux **Terrasses du cœur** pour leur continuité avec le Jardin actuel. **La vie sous la terre** rend la progression la plus compréhensible ; **La source intérieure** propose le rituel le plus singulier.

| Concept | Idée | Plantation | Arrosage | Point fort |
| --- | --- | --- | --- | --- |
| 1. Les terrasses du cœur | Un lieu accueille la semence | 3,60 s | 4,20 s | Prolonger le jardin méditerranéen existant |
| 2. La vie sous la terre | La croissance cachée devient visible | 3,40 s | 4,00 s | Faire comprendre le stade Racine |
| 3. La source intérieure | L'eau circule dans un jardin intérieur | 3,20 s | 4,40 s | Installer une signature gestuelle mémorable |

## Direction commune

Le cœur représente un lieu que l'on cultive : architecture, terre, circulation de l'eau ou racines. La graine a la forme d'une petite amande brune, sans visage. Les fruits sont de petites olives à reflet doré. Aucun rose, battement cardiaque, trophée, confetti ou rebond de jouet.

Palette : sable `#F3ECE0`, crème `#FBF7F0`, olive `#4E5A43`, sauge `#8D9A79`, or doux `#E9C987`, terre `#8E6748`, eau `#6F9FAE`. L'or accompagne une vie qui commence ; le bleu est réservé à l'eau. La pierre utilise le sable et le crème, avec une ombre chaude sobre.

Le Jardin actuel fournit la lumière de fin de journée, les oliviers, la lavande et les volumes de pierre. L'animation en extrait des silhouettes simples : son décor n'a pas besoin du niveau de détail de l'illustration de fond. Le storyboard fourni sert de référence narrative ; ces propositions ajoutent un lieu, une vie souterraine ou un parcours d'eau.

### Présentation dans l'app

Après la plantation ou la validation d'une révision, afficher une scène centrée sur fond crème, avec la référence du verset au-dessus. Zone illustrée d'environ 280 à 320 dp de large dans une vue portrait, marges latérales de 24 dp. La scène reste entièrement visible avec les textes agrandis ; ajuster sa hauteur plutôt que réduire le texte. La caméra reste fixe pendant chaque séquence.

Un seul titre, « Une parole plantée » ou « Ton jardin grandit », et l'indication « Toucher pour passer ». Au terme de l'arrosage, afficher le libellé **Graine**, **Racine**, **Arbre** ou **Fruit**, qui rend le stade explicite sans dépendre de la couleur. Si le stade ne change pas, préférer « Ta parole prend force » à une annonce de nouveau niveau.

Un tap dès la première image termine la mise en scène et affiche son résultat final. Prévoir aussi un contrôle accessible « Passer l'animation ». Ni le tap ni un retour arrière ne rejouent l'enregistrement de la réussite : le résultat est acquis avant la mise en scène. Le son et les vibrations sont facultatifs et désactivables séparément.

### Règle de progression

**La première plantation se termine toujours à Graine.** Elle ne montre ni germe ni racine. L'arrosage commence ensuite par la semence et restitue sa croissance jusqu'au stade actuel renvoyé par l'app. Pour un verset déjà Arbre ou Fruit, il s'agit d'une courte évocation de sa croissance ; le jardin principal ne régresse pas à Graine.

Les planches illustrent la variante la plus complète, **Fruit**. Les variantes ci-dessous décrivent ce qui remplace les images de croissance quand le stade est inférieur. Elles conservent la durée totale annoncée : les étapes inutiles deviennent absorption, déploiement modeste et stabilisation, sans apparition d'un stade non acquis.

Les deux colonnes de chaque planche se lisent **de haut en bas**, plantation à gauche, arrosage à droite. Les repères des cinq premières images marquent le début de leur phase ; le sixième indique l'état final à la fin de la séquence. Les tableaux donnent tous les intervalles, y compris la dernière phase de stabilisation. Les PNG sont des planches de direction artistique, pas des textures à embarquer dans l'app ; les timings et limites de formes ci-dessous définissent la réalisation vectorielle.

## 1. Les terrasses du cœur

[Ouvrir la planche portrait](concept-1.png)

### Le cœur

Une petite île calcaire suspendue, en perspective fixe. Deux terrasses courbes forment des épaules légèrement différentes ; leur convergence vers l'avant suggère une pointe émoussée. Le cœur se lit dans le plan du jardin et son lit de terre central. Une petite arche et un cyprès servent de repères fixes, avec quelques lavandes. Aucun olivier adulte dans le décor : l'olivier central est toujours le verset.

L'intérêt est de faire entrer la Parole dans un lieu accueillant. La silhouette architecturale, l'asymétrie et les ombres géométriques évitent l'effet de pictogramme romantique.

### Première plantation — 6 images, 3,60 s

| Image | Intervalle | Durée | Action visible |
| --- | --- | --- | --- |
| P1 — L'accueil | 0,00–0,45 s | 0,45 s | L'île apparaît par un fondu ; une petite graine est suspendue au-dessus du lit de terre. |
| P2 — La descente | 0,45–1,10 s | 0,65 s | La graine descend en légère diagonale. Son ombre se resserre ; deux traits d'or très courts accompagnent la chute. |
| P3 — Le contact | 1,10–1,65 s | 0,55 s | Contact avec la terre : enfoncement de 2 à 3 dp et une onde ovale mate. La graine s'incline de quelques degrés. |
| P4 — L'accueil de la terre | 1,65–2,35 s | 0,70 s | Deux petites formes de terre ferment la fente. La graine disparaît partiellement dans un léger monticule. |
| P5 — La présence | 2,35–3,00 s | 0,65 s | Un seul point d'or diffuse sous la surface puis s'éteint. Aucune germination. |
| P6 — La parole plantée | 3,00–3,60 s | 0,60 s | La terre se stabilise ; le message « Une parole plantée » devient lisible. État final : Graine. |

### Arrosage réussi — 6 images, 4,20 s

| Image | Intervalle | Durée | Action visible — variante Fruit |
| --- | --- | --- | --- |
| A1 — Le rendez-vous | 0,00–0,35 s | 0,35 s | L'île et le repère de la semence sont présents. Une petite cruche de terre entre dans la scène. |
| A2 — Le geste | 0,35–1,10 s | 0,75 s | La cruche pivote autour de son anse ; un filet bleu courbe se dirige sur la graine. |
| A3 — L'absorption | 1,10–1,85 s | 0,75 s | Cinq à huit gouttes terminent le filet. La terre fonce depuis le point de contact ; la cruche se retire en fondu. |
| A4 — Les racines | 1,85–3,15 s | 1,30 s | Une coupe locale du lit de terre révèle trois à cinq racines crème ; une tige puis deux feuilles émergent. |
| A5 — L'olivier | 3,15–3,70 s | 0,55 s | Le tronc se prolonge depuis sa base ; les branches et groupes de feuilles s'ouvrent. La silhouette adulte est atteinte en fin de phase. |
| A6 — Le fruit | 3,70–4,20 s | 0,50 s | Quatre à six olives dorées apparaissent en léger décalage. La coupe souterraine se referme, les feuilles se posent. |

### Mouvement, son et vibration

Chute accélérée, sans ricochet. Onde du contact en décélération. Cruche sur une accélération puis décélération douces ; rotation totale d'environ 22°. Croissance par dévoilement depuis la base, jamais par agrandissement uniforme de tout l'arbre. Les feuilles pivotent à leur attache, avec un ressort presque amorti : dépassement maximal de 3°, une seule correction. Deux petites feuilles peuvent dériver de 8 dp au stade Fruit, sans pluie de feuilles.

Sons : frottement de terre très bref au contact, eau feutrée pendant 0,8 s, puis une note de bois douce de 0,25 s au résultat. Une impulsion haptique légère au contact de la plantation ; une autre à la confirmation de l'arrosage, sans vibration pour chaque goutte.

### Variantes et flétrissement

| Stade actuel | A4 | A5 et A6 |
| --- | --- | --- |
| Graine | L'humidité rejoint la graine, sans racine ni tige. | La terre absorbe l'eau ; un reflet de présence s'atténue, puis confirmation Graine. |
| Racine | Trois à cinq racines et deux feuilles. | Les deux feuilles se déplient puis se stabilisent ; aucun tronc ligneux ni fruit. |
| Arbre | Racines et germe, puis croissance. | Olivier adulte à couronne légère ; les feuilles se posent, aucun fruit. |
| Fruit | Même germe et mêmes racines. | Couronne adulte un peu plus dense, quatre à six olives or mat. |

Si le verset fane, la lavande et l'arche restent paisibles. Le lit de terre perd son humidité ; les feuilles du verset s'inclinent de 8 à 12°. Au stade Fruit, les olives deviennent mates. Ne pas faire tomber l'arbre. Lors du prochain arrosage réussi, l'eau redresse ces feuilles avant de confirmer le stade actuel.

### Traduction vectorielle

Trois silhouettes de terrasses, huit à dix blocs de pierre principaux, une arche simplifiée, une ombre fixe. Cruche : deux chemins et une anse. Plante : cinq chemins de racines au maximum, un tronc, six branches et douze groupes de feuilles. Les petites feuilles illustrées sur la planche seront réunies en groupes à la réalisation. Le seul masque mobile supplémentaire sert à la coupe locale du sol.

## 2. La vie sous la terre

[Ouvrir la planche portrait](concept-2.png)

### Le cœur

Une tranche de terre flottante en coupe : une surface presque horizontale, deux épaules souples et une base qui se resserre. La graine reste visible sous la surface. À partir de Racine, deux racines latérales dessinent des courbes qui entourent un vide central ; la forme du cœur est surtout suggérée par cet espace vivant. Le halo de plantation est local à la graine, jamais un contour néon du cœur.

Cette piste montre ce qui se construit avant d'être visible. Le stade Racine devient une réussite en soi, et non un petit arbre incomplet. Un muret et une lavande suffisent à relier la coupe au jardin méditerranéen.

### Première plantation — 6 images, 3,40 s

| Image | Intervalle | Durée | Action visible |
| --- | --- | --- | --- |
| P1 — La semence | 0,00–0,40 s | 0,40 s | Coupe de terre immobile ; la graine apparaît au-dessus et son ombre marque le lieu de plantation. |
| P2 — La chute | 0,40–0,95 s | 0,55 s | Descente verticale, légèrement accélérée ; l'ombre rétrécit. |
| P3 — Le passage | 0,95–1,55 s | 0,60 s | Une fente s'ouvre dans la ligne de surface. La graine glisse à travers sans rebond. |
| P4 — L'enfouissement | 1,55–2,20 s | 0,65 s | La graine se pose sous la surface, encore visible en coupe ; la fente se ferme au-dessus. |
| P5 — La chaleur | 2,20–2,85 s | 0,65 s | Un disque d'or très transparent s'étend autour de la graine, puis décroît. Aucune racine. |
| P6 — Le repos | 2,85–3,40 s | 0,55 s | La graine demeure en terre, le halo s'éteint. « Une parole plantée ». |

### Arrosage réussi — 6 images, 4,00 s

| Image | Intervalle | Durée | Action visible — variante Fruit |
| --- | --- | --- | --- |
| A1 — Sous la surface | 0,00–0,30 s | 0,30 s | Graine en coupe ; le lieu d'arrivée de l'eau est visible au-dessus. |
| A2 — L'eau | 0,30–1,00 s | 0,70 s | Le bec d'une petite cruche verse un filet sur la terre à l'aplomb de la graine. |
| A3 — La pénétration | 1,00–1,65 s | 0,65 s | Quatre gouttes complètent le geste ; une zone sombre descend de la surface à la semence. Le bleu s'efface à mesure que l'eau est absorbée. |
| A4 — Le réseau vivant | 1,65–2,75 s | 1,10 s | Les racines se révèlent du centre vers les extrémités, en léger décalage ; une tige et exactement deux feuilles percent la surface. |
| A5 — La croissance | 2,75–3,45 s | 0,70 s | L'olivier se ramifie, de jeune à adulte ; les racines restent visibles en bas, sans bouger comme des tentacules. |
| A6 — La maturité | 3,45–4,00 s | 0,55 s | Cinq petites olives prennent place. Un reflet chaud parcourt une seule fois deux racines ; le tout s'immobilise. |

### Mouvement, son et vibration

Tout demeure dans le même repère, sans basculement de caméra. Les racines sont des courbes dévoilées sur leur longueur ; décalage de 50 à 80 ms entre les branches, avec une décélération douce. Un masque descend pour l'humidité. Le halo utilise deux disques transparents, sans flou animé. Les deux feuilles s'ouvrent comme des mains très lentes : rotation de 18° autour de leur base, sans ressort. Les olives entrent par opacité et une croissance très limitée, de 95 % à 100 %.

Sons : petit son mat au passage de la terre, eau proche et discrète, puis résonance de bois grave lorsque le réseau se stabilise. Aucune voix, aucun tintement pour chaque racine. Haptique : une impulsion légère à l'enfouissement ou à la confirmation finale, selon le moment joué.

### Variantes et flétrissement

| Stade actuel | A4 | A5 et A6 |
| --- | --- | --- |
| Graine | L'humidité enveloppe seulement la graine. | Le halo de présence s'atténue ; la semence reste intacte, aucune racine. |
| Racine | Cinq racines au maximum et deux feuilles. | Les racines finissent leur dévoilement ; tige basse, deux feuilles, aucun bois ni fruit. |
| Arbre | Racines et germe. | Tronc ramifié et couronne adulte ; reflet dans les racines, aucun fruit. |
| Fruit | Racines et germe. | Même structure adulte, cinq olives or mat ; racines toujours lisibles. |

En cas de flétrissement, le sol redevient plus clair, les racines perdent un peu de contraste et les feuilles baissent. Les racines ne se rétractent pas : le travail déjà accompli reste perceptible. Au prochain arrosage réussi, le front humide les rejoint et leur contraste revient avant la confirmation.

### Traduction vectorielle

Une silhouette de terre, une surface, un masque d'humidité, cinq chemins de racines, une graine et un arbre en groupes. Les cailloux de la planche deviennent six marques fixes au maximum, pas une texture. Cette piste demande peu de volumes, mais une bonne qualité des chemins et une coupe lisible à petite taille. Elle constitue un bon premier prototype pour valider la métaphore.

## 3. La source intérieure

[Ouvrir la planche portrait](concept-3.png)

### Le cœur

Un jardin de pierre vu presque du dessus. Deux rigoles courbes occupent ses deux lobes asymétriques et se rejoignent vers une pointe arrondie. Au centre, un lit de terre accueille la semence. Dans l'encoche supérieure, un petit bec de pierre verse l'eau directement sur ce lit ; l'eau rejoint ensuite les rigoles. Le cœur est une architecture d'irrigation, présente dès la plantation mais sèche.

La lecture du cœur est plus explicite que dans les deux autres pistes. La pierre, la dissymétrie et la fonction des canaux l'éloignent du symbole sentimental. Le passage de rigoles sèches à un parcours bleu constitue la signature du rituel.

### Première plantation — 6 images, 3,20 s

| Image | Intervalle | Durée | Action visible |
| --- | --- | --- | --- |
| P1 — Le lieu intérieur | 0,00–0,35 s | 0,35 s | Jardin sec, lit de terre vide ; une graine apparaît au-dessus de l'encoche. |
| P2 — La trajectoire | 0,35–0,90 s | 0,55 s | La graine rejoint le centre sur un trajet légèrement courbe ; deux points d'or suffisent à le suggérer. |
| P3 — Le contact | 0,90–1,40 s | 0,50 s | La semence touche la terre ; une onde de poussière très fine se dissipe. Les rigoles restent sèches. |
| P4 — L'ancrage | 1,40–2,05 s | 0,65 s | La terre recouvre presque la graine. La source demeure silencieuse. |
| P5 — La présence | 2,05–2,65 s | 0,60 s | Une brève chaleur dorée traverse le seul lit de terre. Aucune plante ni racine. |
| P6 — Le calme | 2,65–3,20 s | 0,55 s | Lit de terre planté, eau absente. « Une parole plantée ». |

### Arrosage réussi — 6 images, 4,40 s

| Image | Intervalle | Durée | Action visible — variante Fruit |
| --- | --- | --- | --- |
| A1 — La source | 0,00–0,40 s | 0,40 s | Jardin sec et semence plantée ; une goutte se forme au bec de pierre. |
| A2 — Le versement | 0,40–1,20 s | 0,80 s | Un filet puis quatre gouttes tombent directement sur la graine. |
| A3 — La circulation | 1,20–2,05 s | 0,85 s | La terre s'assombrit. L'eau entre dans les deux arcs des rigoles et progresse vers leur jonction ; le filet cesse. |
| A4 — L'émergence | 2,05–3,25 s | 1,20 s | Un germe à deux feuilles apparaît ; une petite coupe locale révèle les racines sous le lit de terre. |
| A5 — La couronne | 3,25–3,90 s | 0,65 s | Tronc et branches grandissent ; huit groupes de feuilles s'ouvrent en éventail jusqu'à la silhouette adulte. |
| A6 — Le reflet | 3,90–4,40 s | 0,50 s | Cinq petites olives apparaissent ; un reflet doré court glisse dans l'eau, puis s'arrête. La coupe du sol se referme. |

### Mouvement, son et vibration

Chute courbe accélérée, source et île immobiles. Eau dévoilée sur deux chemins fixes à vitesse régulière, pour permettre à l'œil de suivre son trajet. Aucun moteur de fluide. Une seule onde ovale au contact du filet. La couronne s'ouvre par rotations successives de 6 à 10°, en décélération, sans rebond ; un seul groupe de feuilles garde un retard de 60 ms. Le reflet final est un segment court animé le long d'une rigole, pas une série de paillettes.

Sons : première goutte isolée, eau fine pendant 0,9 s, puis souffle léger de feuilles. Une note finale optionnelle très discrète peut reprendre celle des Terrasses. Haptique : une impulsion légère à la confirmation ; aucune pulsation au parcours de l'eau.

### Variantes et flétrissement

| Stade actuel | A4 | A5 et A6 |
| --- | --- | --- |
| Graine | Lit de terre humide, semence inchangée, canaux bleus. | L'eau se calme et un reflet bref confirme la présence de la graine ; aucune pousse. |
| Racine | Germe à deux feuilles et trois racines en coupe locale. | Deux feuilles se déplient ; la coupe reste visible jusqu'à la confirmation, puis se ferme. |
| Arbre | Germe et racines puis croissance. | Couronne adulte en éventail ; un reflet d'eau termine le rituel, aucun fruit. |
| Fruit | Même croissance initiale. | Couronne adulte et cinq petites olives ; reflet d'or bref dans l'eau. |

Quand le verset fane, le niveau visible dans les rigoles baisse et les feuilles s'inclinent. La source ne se fissure pas, le jardin reste accessible. Le prochain succès remplit les rigoles et redresse le feuillage. Les canaux pleins appartiennent à la scène de réussite : ils ne remplacent pas l'indicateur de révision due du jardin principal.

### Traduction vectorielle

Un contour architectural fixe, douze blocs de pierre au maximum, deux chemins pour les rigoles, un chemin de filet, quatre gouttes et un arbre. Pour l'eau, révéler un trait coloré à l'intérieur du tracé préétabli. Le reflet utilise le même parcours. La géométrie presque vue du dessus supprime tout besoin de rotation 3D. À tester en priorité sur un petit écran : les canaux doivent rester lisibles derrière l'arbre.

## Quand un verset fane : un échec sans punition

Proposition commune : séparer **le stade mémorisé** et **le besoin d'eau**. Une réponse incorrecte ne suffit pas à détruire ou rétrograder le verset. Si le produit considère cet échec comme un flétrissement, animer seulement l'état de soif et laisser le stade fourni par la logique métier décider de la silhouette finale. Cette distinction est une proposition UX, pas une modification des règles de mémorisation existantes.

Si le modèle actuel impose un changement de stade, afficher ensuite ce stade réel et son libellé ; ne pas présenter une croissance fictive pour rassurer l'utilisateur. Une période de révision due peut utiliser le même signe discret de soif, sans animation de défaite répétée à chaque ouverture.

La réaction à un échec dure **0,90 s** : c'est un retour bref, distinct des deux cérémonies de 3 à 5 secondes demandées.

| Temps | Réaction |
| --- | --- |
| 0,00–0,25 s | Le mouvement s'apaise ; ni eau ni croissance ne démarrent. |
| 0,25–0,70 s | Terre légèrement plus claire ; au stade Racine ou supérieur, feuilles inclinées. Le signe spécifique du concept s'ajoute : sol sec, racines moins contrastées ou rigoles basses. |
| 0,70–0,90 s | Message « Cette parole a encore besoin d'eau » ; la correction et l'action « Réessayer » restent prioritaires. |

| Stade | Signe de flétrissement |
| --- | --- |
| Graine | Sol sec, chaleur dorée absente ; la semence reste entière. |
| Racine | Deux feuilles baissées, racines intactes et un peu moins contrastées. |
| Arbre | Feuillage légèrement incliné et moins vif ; tronc intact. |
| Fruit | Même feuillage, olives devenues mates ; aucun fruit arraché. |

Aucun son grave de défaite, aucune vibration négative, aucune secousse rouge. La correction du verset est plus importante que la scène. Un tap passe ce retour immédiatement. Un succès suivant restaure les signes de vitalité pendant l'arrosage ; il ne joue pas une seconde cérémonie de récupération.

## Spécification de mouvement pour Compose Multiplatform

Ces indications définissent une cible de réalisation, pas une performance déjà mesurée. Aucun code de l'app n'est modifié dans cette livraison.

### Courbes proposées

Les quatre nombres des courbes ci-dessous sont les points de contrôle d'une Bézier cubique normalisée ; ils permettent d'obtenir le même caractère de mouvement dans les trois pistes.

| Usage | Courbe / comportement |
| --- | --- |
| Chute de la graine | `(0.45, 0, 1, 1)` : départ calme, accélération vers le sol ; rotation limitée à 12°. |
| Entrée de la cruche, inclinaison | `(0.4, 0, 0.2, 1)` : accélération et arrêt progressifs. |
| Onde de terre, déploiement de racines et branches | `(0.16, 1, 0.3, 1)` : avancée rapide, fin douce. |
| Eau dans les rigoles | Linéaire sur la longueur du chemin ; début et fin du filet en fondu de 100 ms. |
| Fondus | Opacité linéaire sur 120 à 220 ms ; un seul halo, sans pulsation continue. |
| Feuilles des Terrasses | Ressort fortement amorti, réglé pour un dépassement ≤ 3° et une seule correction. |
| Feuilles des deux autres concepts | Bézier de décélération, sans dépassement. |

Éviter les ressorts sur la graine, le tronc, la caméra et les racines. Le ludique vient de la transformation visible et du rythme ; la tranquillité vient de la stabilité du lieu.

### Architecture de dessin

Dessiner la scène dans un Canvas commun, avec coordonnées normalisées et mise à l'échelle selon l'espace disponible. Utiliser une seule horloge de progression par séquence ; les six phases calculent leurs paramètres depuis le temps écoulé. Ne pas lancer une coroutine par feuille ou par goutte.

Séparer les chemins statiques du décor et les valeurs animées : déplacement de la semence, inclinaison de la cruche, longueur révélée des racines, hauteur du tronc, angle des feuilles, opacité des fruits. Construire et mettre en cache la géométrie hors de la boucle de dessin. Animer dans la zone illustrée sans déclencher une remise en page de tout l'écran.

Préférer des chemins préétablis révélés par masque ou par segments, des formes superposées et des rotations autour d'attaches fixes. Aucun morphing entre deux chemins de topologies différentes. La croissance du tronc révèle sa forme depuis le sol ; les branches suivent ensuite, ce qui évite l'effet d'un arbre entier gonflé comme un ballon.

Son et haptique passent par des adaptateurs de plateforme Android/iOS ; le dessin et la chronologie restent partagés. Déclencher les événements une seule fois au passage de leur repère temporel, en évitant leur répétition lors d'une recomposition, d'une reprise ou d'un tap de passage.

### Budget proposé pour un téléphone de milieu de gamme

| Élément | Limite de départ |
| --- | --- |
| Zone animée | Une seule scène ; décor immobile, caméra fixe |
| Gouttes | 8 simultanées au maximum, positions déterministes |
| Racines | 5 chemins courbes |
| Feuillage | 12 groupes animés au maximum, plutôt que chaque feuille illustrée |
| Fruits | 6 formes au maximum |
| Autres particules | 2 traces d'or ou 2 feuilles, selon la phase |
| Effets | 2 disques transparents pour le halo ; pas de flou animé ni d'effet de fluide |
| Masques | 2 actifs au maximum, limités au sol ou à l'eau |
| Objectif | 60 images/s, soit environ 16,7 ms par image, à vérifier sur appareil |

Aucune vidéo, aucune séquence PNG, aucune simulation physique. Les textures minérales et les nombreux détails visibles dans les planches se traduisent en quelques formes fixes. Réutiliser les objets de dessin et les tableaux de positions ; éviter les allocations de chemins ou de particules à chaque image.

### Passage, interruptions et mouvement réduit

Au tap de passage, annuler les sons et événements restants puis afficher l'état final dans un fondu de 100 ms. Cette action garde le stade et le résultat déjà enregistrés, et ne joue pas une rafale de vibrations ou de sons sautés. Arrêter l'horloge lorsque la scène sort de l'écran ; au retour, afficher le résultat acquis plutôt que relancer une plantation.

Avec la préférence de mouvement réduit, remplacer la cérémonie par un fondu de 200 ms vers la silhouette finale et son libellé. Supprimer chutes, rotations, particules, halos et feuilles en mouvement ; ne pas imposer une attente de 3 à 5 secondes. Annoncer le résultat une seule fois aux technologies d'assistance, sans décrire chacune des douze images.

### Vérification lors de l'implémentation

Vérifier les quatre stades pour chaque concept, notamment **aucun germe pendant la plantation**, **aucune racine au stade Graine** et **aucun fruit au stade Arbre**. Tester le passage pendant chacune des six phases, puis une interruption et une reprise, pour garantir un seul enregistrement du résultat.

Mesurer une séquence complète sur Android et iOS de milieu de gamme : temps de dessin, images manquées et allocations. Si nécessaire, réduire d'abord les détails fixes et les groupes de feuilles, puis les particules. Conserver la lisibilité des racines et du stade. Vérifier également le texte agrandi, le mode silencieux, le mouvement réduit et la lecture du résultat sans couleur.

## Fichiers et génération

- `concepts.md` : scénarios, timings, variantes, flétrissement et spécification vectorielle.
- `concept-1.png` : Les terrasses du cœur.
- `concept-2.png` : La vie sous la terre.
- `concept-3.png` : La source intérieure.
- `prompts.md` : consignes exactes de génération et retouches, conservées pour permettre les itérations.

Les images ont été réalisées avec l'outil imagegen intégré, puis copiées dans ce dossier. Chaque planche est un PNG portrait au ratio téléphone 9:16 et comporte six images de plantation et six d'arrosage. Les variantes de stade et d'échec sont spécifiées dans le texte ; elles ne font pas l'objet de planches supplémentaires.
