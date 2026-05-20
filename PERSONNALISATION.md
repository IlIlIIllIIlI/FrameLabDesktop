# Personnalisation et Fonctionnalités Avancées

Ce document détaille les fonctionnalités spécifiques et avancées qui ont été développées sur mesure pour l'application **FrameSprite**. L'objectif de ces ajouts est de fournir une expérience utilisateur professionnelle, de garantir la sécurité des données et d'assurer une gestion robuste des projets.

Voici l'explication technique des trois ajouts majeurs : le système d'historique (Undo/Redo), la gestion des métadonnées des calques, et la persistance chiffrée des sessions.

---

## 1. Le Système d'Historique (Undo / Redo)

### Pourquoi l'avoir ajouté ?
Dans toute application de dessin ou de retouche d'image, l'erreur est inévitable. Ne pas pouvoir annuler un coup de pinceau accidentel ou un filtre mal réglé génère une grande frustration. Nous avons donc implémenté un système d'historique complet permettant à l'utilisateur de naviguer dans le temps (Annuler / Refaire), lui offrant ainsi un filet de sécurité indispensable pour la création artistique.

### Comment ça fonctionne (Implémentation) ?
Le système repose sur le **Design Pattern "Command"**.
* **L'interface `Command`** : Chaque action modifiant la toile (un coup de pinceau, l'application d'un filtre) implémente cette interface qui possède deux méthodes : `execute()` et `undo()`.
* **Les classes `Paint` et `FilterCommand`** : Avant qu'une action ne soit appliquée sur un `Canvas`, une capture d'écran (Snapshot) de l'état actuel est sauvegardée (`backup`). Une fois l'action terminée, une nouvelle capture est enregistrée (`present`).
* **Le `HistoryService`** : Il agit comme le chef d'orchestre. Il maintient deux piles (Stacks) en mémoire : une pile pour les actions effectuées, et une pile pour les actions annulées.
    * Lors de l'appel à **Undo** (Ctrl+Z), l'action au sommet de la pile est retirée, sa méthode `undo()` est appelée (qui redessine le `backup`), et l'action est placée dans la pile de Redo.
    * Lors de l'appel à **Redo** (Ctrl+Maj+Z), le processus inverse se produit (redessine le `present`).

---

## 2. Le Fichier de Métadonnées (`metadata.json`)

### Pourquoi l'avoir ajouté ?
Un projet FrameSprite n'est pas qu'une simple image plate : c'est un assemblage complexe de plusieurs calques (`SpriteLayer`), chacun possédant ses propres propriétés (nom, visibilité, niveau d'opacité) et sa propre image. Si l'on sauvegardait uniquement le rendu final, l'utilisateur perdrait la capacité de retravailler ses calques individuellement plus tard. Il fallait donc un moyen de mémoriser l'état exact de l'espace de travail.
### Comment ça fonctionne (Implémentation) ?
Le système de sauvegarde a été séparé en deux parties gérées par le `StorageService` :
1. **Les images brutes** : Chaque calque est exporté individuellement sous forme de fichier `.png` transparent dans le dossier du projet (ex: `projects/{id}/`).
2. **La structure JSON** : Les propriétés de chaque calque (nom, visibilité, opacité, nom du fichier PNG associé) sont converties en objets de transfert (`SpriteLayerDTO`). La librairie **Jackson** (`ObjectMapper`) est ensuite utilisée pour sérialiser cette liste d'objets en un fichier texte : **`metadata.json`**.
* **Au chargement** : L'application lit d'abord le fichier `metadata.json` pour recréer les objets `SpriteLayer` avec les bonnes opacités et visibilités, puis associe à chacun le bon fichier `.png`. L'utilisateur retrouve son espace de travail exactement comme il l'avait laissé.

### L'avantage face à une base de données (Import / Export)
Contrairement à un stockage de l'état des calques dans une base de données locale ou distante, l'utilisation d'un fichier `metadata.json` regroupé avec les images offre une **portabilité totale**.
Grâce à cette approche orientée "fichier", l'importation et l'exportation deviennent triviales : pour exporter un projet, l'application se contente de zipper le dossier contenant les images et le JSON. Pour l'importer, elle le dézippe. Ce fonctionnement autonome, sans dépendance à un schéma de base de données complexe ou à des requêtes SQL d'extraction, rend le partage de projets entre utilisateurs et la création de sauvegardes incroyablement fluides et légers.

---

## 3. Persistance Sécurisée des Cookies (`cookies.enc`)

### Pourquoi l'avoir ajouté ?
Pour le confort de l'utilisateur, il est essentiel qu'il n'ait pas à se reconnecter (entrer son email et mot de passe) à chaque fois qu'il ferme et rouvre l'application. Cependant, stocker les jetons de session (Session IDs) en clair sur le disque dur de l'ordinateur représente une faille de sécurité majeure (risque de vol de session par un logiciel malveillant). Nous avons donc mis en place une persistance **chiffrée**.

### Comment ça fonctionne (Implémentation) ?
Ce système est géré par la classe `CookieUtils` et utilise le chiffrement symétrique.
* **Extraction et Mapping** : Le `CookieManager` de Java gère les cookies de session (`HttpCookie`). Comme ces objets ne sont pas facilement sérialisables, nous les convertissons d'abord en un modèle personnalisé (`CookieParsing`).
* **Chiffrement (Encryption)** : Avant la fermeture de l'application (grâce à un *Shutdown Hook*), la liste des cookies est transformée en chaîne JSON. Cette chaîne passe ensuite dans un module de cryptographie (`EncryptUtils`) qui la chiffre pour la rendre illisible.
* **Sauvegarde** : Le résultat binaire/chiffré est écrit dans un fichier local nommé **`cookies.enc`**.
* **Restauration** : Au lancement suivant, `CookieUtils` lit le fichier `cookies.enc`, le déchiffre, reconstitue les objets `HttpCookie`, et les réinjecte dans le gestionnaire réseau. La communication avec l'API REST reprend instantanément sans nécessiter de nouvelle authentification de la part de l'utilisateur.