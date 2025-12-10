# Système de Gestion de Stock pour Pharmacie

## 📋 Description

Application complète en Java pour la gestion du stock d'une pharmacie, incluant la gestion des médicaments, des fournisseurs, des ventes, la surveillance des dates d'expiration, et la consultation du stock à distance via un module client-serveur.

## 🎯 Fonctionnalités Principales

### 1. Gestion des Médicaments
- ✅ Ajout, modification, suppression de médicaments
- ✅ Consultation et recherche (par nom, catégorie, fournisseur)
- ✅ Affichage des quantités en temps réel
- ✅ Alertes automatiques pour stock faible ou nul

### 2. Gestion des Dates d'Expiration
- ✅ Suivi automatique des dates d'expiration
- ✅ Filtrage des médicaments expirés avec Java Streams
- ✅ Thread de surveillance automatique des expirations
- ✅ Alertes visuelles pour médicaments à éliminer

### 3. Gestion des Ventes
- ✅ Enregistrement de ventes avec réduction automatique du stock
- ✅ Prévention des ventes (stock insuffisant, médicament expiré)
- ✅ Génération de statistiques (meilleures ventes, total journalier)
- ✅ Graphiques de visualisation (PieChart, LineChart)

### 4. Gestion des Fournisseurs
- ✅ CRUD complet des fournisseurs
- ✅ Association fournisseur-médicament
- ✅ Historique et statistiques par fournisseur

### 5. Import/Export CSV
- ✅ Export du stock complet en CSV
- ✅ Import de stock depuis fichier CSV
- ✅ Format compatible Excel

### 6. Interface Graphique JavaFX
- ✅ Tableaux interactifs avec alertes visuelles
- ✅ Formulaires de saisie ergonomiques
- ✅ Graphiques statistiques
- ✅ Design moderne et intuitif


## 🏗️ Architecture

```
src/main/java/com/pharmacie/
├── controller/          # Contrôleurs JavaFX
│   ├── MainController.java
│   ├── MedicamentController.java
│   ├── VenteController.java
│   ├── FournisseurController.java
│   └── AlertController.java
│
├── model/              # Modèles de données
│   ├── Medicament.java
│   ├── Vente.java
│   └── Fournisseur.java
│
├── service/            # Logique métier
│   ├── StocksService.java
│   ├── VentesService.java
│   ├── FournisseurService.java
│   ├── StatistiquesService.java
│   ├── CSVService.java
│   └── ExpirationMonitor.java
│
├── dao/                # Accès aux données
│   ├── DatabaseManager.java
│   ├── MedicamentDAO.java
│   ├── VenteDAO.java
│   └── FournisseurDAO.java
│
├── exception/          # Exceptions personnalisées
│   ├── DonneeInvalideException.java
│   ├── MedicamentExpireException.java
│   └── StockInsuffisantException.java
│
└── util/               # Utilitaires
    ├── DateUtil.java
    └── MainApp.java

src/main/resources/
├── Fxml/              # Fichiers FXML (interfaces)
├── css/               # Feuilles de style
└── database.properties # Configuration BDD
```

## 🔧 Technologies Utilisées

- **Java 11+** - Langage principal
- **JavaFX** - Interface graphique
- **MySQL** - Base de données
- **JDBC** - Connectivité base de données
- **Java Streams** - Traitement de données
- **Multithreading** - Surveillance automatique
- **CSV** - Import/Export de données

## 📦 Installation

### Prérequis

1. **Java JDK 11 ou supérieur**
2. **MySQL 8.0 ou supérieur**
3. **Maven** (pour la gestion des dépendances)
4. **JavaFX SDK** (si non inclus dans le JDK)

### Étapes d'installation

1. **Cloner le projet**
```bash
git clone <repository-url>
cd Java_pfa
```

2. **Configurer la base de données**
- Modifier `src/main/resources/database.properties`
- Ajuster l'URL, username et password selon votre configuration MySQL

```properties
db.url=jdbc:mysql://localhost:3306/pharmacie_db?createDatabaseIfNotExist=true&useSSL=false&serverTimezone=UTC
db.username=root
db.password=votre_mot_de_passe
```

3. **Ajouter les dépendances Maven**

Ajouter dans `pom.xml`:

```xml
<dependencies>
    <!-- JavaFX -->
    <dependency>
        <groupId>org.openjfx</groupId>
        <artifactId>javafx-controls</artifactId>
        <version>17.0.2</version>
    </dependency>
    <dependency>
        <groupId>org.openjfx</groupId>
        <artifactId>javafx-fxml</artifactId>
        <version>17.0.2</version>
    </dependency>
    
    <!-- MySQL Connector -->
    <dependency>
        <groupId>mysql</groupId>
        <artifactId>mysql-connector-java</artifactId>
        <version>8.0.33</version>
    </dependency>
</dependencies>
```

4. **Compiler le projet**
```bash
mvn clean install
```

5. **Lancer l'application**
```bash
mvn javafx:run
```

Ou avec Java directement:
```bash
java -jar target/pharmacie-1.0.jar
```

## 🚀 Utilisation

### Démarrage de l'application

1. Lancer `MainApp.java`
2. L'application créera automatiquement la base de données et les tables
3. Le serveur réseau démarre automatiquement sur le port 8888

### Modules principaux

#### 1. Tableau de bord
- Vue d'ensemble des statistiques
- Alertes importantes
- Navigation vers les modules

#### 2. Gestion des médicaments
- Ajouter un nouveau médicament avec fournisseur
- Modifier/Supprimer des médicaments
- Rechercher par nom, catégorie
- Filtrer par fournisseur

#### 3. Gestion des ventes
- Enregistrer une vente
- Consulter l'historique
- Voir les statistiques et graphiques
- Filtrer par période

#### 4. Gestion des fournisseurs
- Ajouter/Modifier des fournisseurs
- Voir les médicaments par fournisseur
- Exporter la liste

#### 5. Alertes
- Médicaments expirés
- Stock faible
- Proche expiration
- Actions de réapprovisionnement

## 📊 Fonctionnalités Avancées

### Java Streams
Utilisés pour:
- Filtrer les médicaments expirés
- Rechercher et trier les données
- Calculer les statistiques
- Générer les rapports

### Multithreading
- Thread de surveillance des expirations (ExpirationMonitor)
- Synchronisation pour éviter les accès concurrents au stock

### Gestion des Exceptions
- `DonneeInvalideException` - Validation des données
- `MedicamentExpireException` - Prévention vente expirés
- `StockInsuffisantException` - Gestion stock insuffisant

## 🔒 Sécurité

- ✅ Validation de toutes les entrées utilisateur
- ✅ Prévention de la corruption des données CSV
- ✅ Synchronisation des accès concurrents au stock

## 📈 Statistiques Disponibles

- Total des ventes (jour/mois)
- Meilleures ventes
- Évolution des ventes (graphique)
- Répartition par médicament (PieChart)
- Taux de rotation du stock
- Prédiction des besoins de réapprovisionnement

## 🛠️ Développement

### Structure du code
- **MVC Pattern** - Séparation Model-View-Controller
- **DAO Pattern** - Accès aux données
- **Service Layer** - Logique métier
- **Singleton** - DatabaseManager
- **Observer Pattern** - Monitoring des expirations

### Bonnes pratiques
- Code commenté et documenté (JavaDoc)
- Gestion correcte des exceptions
- Validation des données
- Code modulaire et réutilisable

## 📝 TODO / Améliorations futures

- [ ] Créer les fichiers FXML pour les interfaces
- [ ] Ajouter l'authentification utilisateur
- [ ] Implémenter la gestion des rôles (admin/employé)
- [ ] Ajouter des tests unitaires
- [ ] Créer un système de backup automatique
- [ ] Ajouter l'impression de factures
- [ ] Améliorer les graphiques statistiques
- [ ] Ajouter la gestion des ordonnances

## 🐛 Dépannage

### Problème de connexion à la base de données
- Vérifier que MySQL est démarré
- Vérifier les credentials dans `database.properties`
- Vérifier que le port 3306 est disponible

### Erreur JavaFX
- Vérifier que JavaFX SDK est installé
- Ajouter les modules JavaFX au runtime

## 📄 Licence

Ce projet est développé dans un cadre éducatif.

## 👥 Auteurs

Projet de gestion de stock pour pharmacie - Version 1.0

---

**Note**: Ce projet démontre l'utilisation complète de Java avec:
- Collections (List, Map)
- Streams
- Multithreading
- JDBC
- JavaFX
- Gestion d'exceptions
- Patterns de conception
- Import/Export CSV

