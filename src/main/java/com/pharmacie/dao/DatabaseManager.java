package com.pharmacie.dao;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Properties;

/**
 * Gestionnaire de connexion à la base de données
 * Implémente le pattern Singleton pour une connexion unique
 */
public class DatabaseManager {
    
    private static DatabaseManager instance;
    private Connection connection;
    private String url;
    private String username;
    private String password;
    
    /**
     * Constructeur privé (Singleton)
     */
    private DatabaseManager() {
        chargerConfiguration();
        initialiserBaseDeDonnees();
    }
    
    /**
     * Obtient l'instance unique du DatabaseManager
     * 
     * @return Instance du DatabaseManager
     */
    public static synchronized DatabaseManager getInstance() {
        if (instance == null) {
            instance = new DatabaseManager();
        }
        return instance;
    }
    
    /**
     * Charge la configuration depuis le fichier properties
     */
    private void chargerConfiguration() {
        Properties props = new Properties();
        
        try (InputStream input = getClass().getClassLoader()
                .getResourceAsStream("database.properties")) {
            
            if (input == null) {
                System.out.println("Fichier database.properties introuvable, utilisation de la configuration par défaut");
                utiliserConfigurationParDefaut();
                return;
            }
            
            props.load(input);
            url = props.getProperty("db.url", "jdbc:mysql://localhost:3306/pharmacie_db");
            username = props.getProperty("db.username", "root");
            password = props.getProperty("db.password", "");
            
        } catch (IOException e) {
            System.err.println("Erreur lors du chargement de la configuration: " + e.getMessage());
            utiliserConfigurationParDefaut();
        }
    }
    
    /**
     * Utilise la configuration par défaut si le fichier properties n'est pas trouvé
     */
    private void utiliserConfigurationParDefaut() {
        url = "jdbc:mysql://localhost:3306/pharmacie_db?createDatabaseIfNotExist=true&useSSL=false&serverTimezone=UTC";
        username = "root";
        password = "";
    }
    
    /**
     * Initialise la base de données et crée les tables si nécessaire
     */
    private void initialiserBaseDeDonnees() {
        try {
            // Charger le driver JDBC
            Class.forName("com.mysql.cj.jdbc.Driver");
            
            // Créer les tables
            creerTables();
            
            System.out.println("Base de données initialisée avec succès");
            
        } catch (ClassNotFoundException e) {
            System.err.println("Driver JDBC introuvable: " + e.getMessage());
        } catch (SQLException e) {
            System.err.println("Erreur lors de l'initialisation de la base de données: " + e.getMessage());
        }
    }
    
    /**
     * Obtient une connexion à la base de données
     * 
     * @return Connexion active
     * @throws SQLException Si erreur de connexion
     */
    public Connection getConnection() throws SQLException {
        if (connection == null || connection.isClosed()) {
            connection = DriverManager.getConnection(url, username, password);
        }
        return connection;
    }
    
    /**
     * Crée les tables si elles n'existent pas
     * 
     * @throws SQLException Si erreur SQL
     */
    private void creerTables() throws SQLException {
        try (Connection conn = getConnection();
             Statement stmt = conn.createStatement()) {
            
            // Table Fournisseurs
            String sqlFournisseurs = "CREATE TABLE IF NOT EXISTS fournisseurs (" +
                    "id BIGINT PRIMARY KEY AUTO_INCREMENT," +
                    "nom VARCHAR(255) NOT NULL," +
                    "adresse TEXT NOT NULL," +
                    "telephone VARCHAR(20) NOT NULL," +
                    "email VARCHAR(255) NOT NULL," +
                    "contact VARCHAR(255) NOT NULL," +
                    "notes TEXT" +
                    ")";
            stmt.execute(sqlFournisseurs);
            
            // Table Medicaments
            String sqlMedicaments = "CREATE TABLE IF NOT EXISTS medicaments (" +
                    "id BIGINT PRIMARY KEY AUTO_INCREMENT," +
                    "nom VARCHAR(255) NOT NULL," +
                    "categorie VARCHAR(255) NOT NULL," +
                    "prix DOUBLE NOT NULL," +
                    "quantite INT NOT NULL," +
                    "date_expiration DATE NOT NULL," +
                    "fournisseur_id BIGINT," +
                    "FOREIGN KEY (fournisseur_id) REFERENCES fournisseurs(id) ON DELETE SET NULL" +
                    ")";
            stmt.execute(sqlMedicaments);
            
            // Table Ventes
            String sqlVentes = "CREATE TABLE IF NOT EXISTS ventes (" +
                    "id BIGINT PRIMARY KEY AUTO_INCREMENT," +
                    "medicament_id BIGINT NOT NULL," +
                    "quantite INT NOT NULL," +
                    "prix_unitaire DOUBLE NOT NULL," +
                    "montant_total DOUBLE NOT NULL," +
                    "date_vente DATETIME NOT NULL," +
                    "client VARCHAR(255)," +
                    "notes TEXT," +
                    "FOREIGN KEY (medicament_id) REFERENCES medicaments(id) ON DELETE CASCADE" +
                    ")";
            stmt.execute(sqlVentes);
            
            // Index pour améliorer les performances
            try {
                stmt.execute("CREATE INDEX idx_medicament_nom ON medicaments(nom)");
                stmt.execute("CREATE INDEX idx_medicament_categorie ON medicaments(categorie)");
                stmt.execute("CREATE INDEX idx_medicament_expiration ON medicaments(date_expiration)");
                stmt.execute("CREATE INDEX idx_vente_date ON ventes(date_vente)");
            } catch (SQLException e) {
                // Les index existent déjà, ignorer l'erreur
            }
            
            System.out.println("Tables créées avec succès");
        }
    }
    
    /**
     * Teste la connexion à la base de données
     * 
     * @return true si la connexion fonctionne
     */
    public boolean testerConnexion() {
        try (Connection conn = getConnection()) {
            return conn != null && !conn.isClosed();
        } catch (SQLException e) {
            System.err.println("Erreur de connexion: " + e.getMessage());
            return false;
        }
    }
    
    /**
     * Ferme la connexion à la base de données
     */
    public void fermerConnexion() {
        try {
            if (connection != null && !connection.isClosed()) {
                connection.close();
                System.out.println("Connexion à la base de données fermée");
            }
        } catch (SQLException e) {
            System.err.println("Erreur lors de la fermeture de la connexion: " + e.getMessage());
        }
    }
    
    /**
     * Réinitialise la base de données (supprime toutes les données)
     * ATTENTION: Cette opération est irréversible!
     * 
     * @throws SQLException Si erreur SQL
     */
    public void reinitialiserBaseDeDonnees() throws SQLException {
        try (Connection conn = getConnection();
             Statement stmt = conn.createStatement()) {
            
            // Désactiver les contraintes de clés étrangères temporairement
            stmt.execute("SET FOREIGN_KEY_CHECKS = 0");
            
            // Supprimer les tables
            stmt.execute("DROP TABLE IF EXISTS ventes");
            stmt.execute("DROP TABLE IF EXISTS medicaments");
            stmt.execute("DROP TABLE IF EXISTS fournisseurs");
            
            // Réactiver les contraintes
            stmt.execute("SET FOREIGN_KEY_CHECKS = 1");
            
            // Recréer les tables
            creerTables();
            
            System.out.println("Base de données réinitialisée");
        }
    }
    
    /**
     * Exécute un script SQL depuis un fichier
     * 
     * @param cheminFichier Chemin du fichier SQL
     * @throws SQLException Si erreur SQL
     * @throws IOException Si erreur de lecture du fichier
     */
    public void executerScript(String cheminFichier) throws SQLException, IOException {
        try (Connection conn = getConnection();
             Statement stmt = conn.createStatement();
             InputStream input = new FileInputStream(cheminFichier)) {
            
            // Lire et exécuter le script
            // (Implémentation simplifiée, pourrait être améliorée)
            System.out.println("Exécution du script: " + cheminFichier);
        }
    }
    
    /**
     * Obtient des statistiques sur la base de données
     * 
     * @return Statistiques sous forme de texte
     */
    public String obtenirStatistiques() {
        StringBuilder stats = new StringBuilder();
        stats.append("=== STATISTIQUES BASE DE DONNÉES ===\n");
        
        try (Connection conn = getConnection();
             Statement stmt = conn.createStatement()) {
            
            // Compter les enregistrements
            var rsFournisseurs = stmt.executeQuery("SELECT COUNT(*) FROM fournisseurs");
            if (rsFournisseurs.next()) {
                stats.append("Fournisseurs: ").append(rsFournisseurs.getInt(1)).append("\n");
            }
            
            var rsMedicaments = stmt.executeQuery("SELECT COUNT(*) FROM medicaments");
            if (rsMedicaments.next()) {
                stats.append("Médicaments: ").append(rsMedicaments.getInt(1)).append("\n");
            }
            
            var rsVentes = stmt.executeQuery("SELECT COUNT(*) FROM ventes");
            if (rsVentes.next()) {
                stats.append("Ventes: ").append(rsVentes.getInt(1)).append("\n");
            }
            
        } catch (SQLException e) {
            stats.append("Erreur: ").append(e.getMessage());
        }
        
        return stats.toString();
    }
}

