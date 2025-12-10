package com.pharmacie.dao;

import com.pharmacie.model.Fournisseur;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

/**
 * DAO pour la gestion des fournisseurs dans la base de données
 * Fournit les opérations CRUD pour les fournisseurs
 */
public class FournisseurDAO {
    
    private DatabaseManager dbManager;
    
    /**
     * Constructeur
     */
    public FournisseurDAO() {
        this.dbManager = DatabaseManager.getInstance();
    }
    
    /**
     * Ajoute un fournisseur à la base de données
     * 
     * @param fournisseur Fournisseur à ajouter
     */
    public void ajouter(Fournisseur fournisseur) {
        String sql = "INSERT INTO fournisseurs (nom, adresse, telephone, email, contact, notes) " +
                     "VALUES (?, ?, ?, ?, ?, ?)";
        
        try (Connection conn = dbManager.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            
            stmt.setString(1, fournisseur.getNom());
            stmt.setString(2, fournisseur.getAdresse());
            stmt.setString(3, fournisseur.getTelephone());
            stmt.setString(4, fournisseur.getEmail());
            stmt.setString(5, fournisseur.getContact());
            stmt.setString(6, fournisseur.getNotes());
            
            int rowsAffected = stmt.executeUpdate();
            
            if (rowsAffected > 0) {
                // Récupérer l'ID généré
                try (ResultSet generatedKeys = stmt.getGeneratedKeys()) {
                    if (generatedKeys.next()) {
                        fournisseur.setId(generatedKeys.getLong(1));
                    }
                }
            }
            
        } catch (SQLException e) {
            System.err.println("Erreur lors de l'ajout du fournisseur: " + e.getMessage());
            e.printStackTrace();
        }
    }
    
    /**
     * Met à jour un fournisseur existant
     * 
     * @param fournisseur Fournisseur à mettre à jour
     */
    public void mettreAJour(Fournisseur fournisseur) {
        String sql = "UPDATE fournisseurs SET nom = ?, adresse = ?, telephone = ?, " +
                     "email = ?, contact = ?, notes = ? WHERE id = ?";
        
        try (Connection conn = dbManager.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setString(1, fournisseur.getNom());
            stmt.setString(2, fournisseur.getAdresse());
            stmt.setString(3, fournisseur.getTelephone());
            stmt.setString(4, fournisseur.getEmail());
            stmt.setString(5, fournisseur.getContact());
            stmt.setString(6, fournisseur.getNotes());
            stmt.setLong(7, fournisseur.getId());
            
            stmt.executeUpdate();
            
        } catch (SQLException e) {
            System.err.println("Erreur lors de la mise à jour du fournisseur: " + e.getMessage());
            e.printStackTrace();
        }
    }
    
    /**
     * Supprime un fournisseur de la base de données
     * 
     * @param id Identifiant du fournisseur à supprimer
     */
    public void supprimer(long id) {
        String sql = "DELETE FROM fournisseurs WHERE id = ?";
        
        try (Connection conn = dbManager.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setLong(1, id);
            stmt.executeUpdate();
            
        } catch (SQLException e) {
            System.err.println("Erreur lors de la suppression du fournisseur: " + e.getMessage());
            e.printStackTrace();
        }
    }
    
    /**
     * Récupère un fournisseur par son identifiant
     * 
     * @param id Identifiant du fournisseur
     * @return Le fournisseur trouvé ou null
     */
    public Fournisseur obtenirParId(long id) {
        String sql = "SELECT * FROM fournisseurs WHERE id = ?";
        
        try (Connection conn = dbManager.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setLong(1, id);
            
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return mapperResultSet(rs);
                }
            }
            
        } catch (SQLException e) {
            System.err.println("Erreur lors de la récupération du fournisseur: " + e.getMessage());
            e.printStackTrace();
        }
        
        return null;
    }
    
    /**
     * Récupère tous les fournisseurs
     * 
     * @return Liste de tous les fournisseurs
     */
    public List<Fournisseur> obtenirTous() {
        List<Fournisseur> fournisseurs = new ArrayList<>();
        String sql = "SELECT * FROM fournisseurs ORDER BY nom";
        
        try (Connection conn = dbManager.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            
            while (rs.next()) {
                fournisseurs.add(mapperResultSet(rs));
            }
            
        } catch (SQLException e) {
            System.err.println("Erreur lors de la récupération des fournisseurs: " + e.getMessage());
            e.printStackTrace();
        }
        
        return fournisseurs;
    }
    
    /**
     * Recherche des fournisseurs par nom
     * 
     * @param nom Nom à rechercher (recherche partielle)
     * @return Liste des fournisseurs correspondants
     */
    public List<Fournisseur> rechercherParNom(String nom) {
        List<Fournisseur> fournisseurs = new ArrayList<>();
        String sql = "SELECT * FROM fournisseurs WHERE nom LIKE ? ORDER BY nom";
        
        try (Connection conn = dbManager.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setString(1, "%" + nom + "%");
            
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    fournisseurs.add(mapperResultSet(rs));
                }
            }
            
        } catch (SQLException e) {
            System.err.println("Erreur lors de la recherche de fournisseurs: " + e.getMessage());
            e.printStackTrace();
        }
        
        return fournisseurs;
    }
    
    /**
     * Recherche des fournisseurs par email
     * 
     * @param email Email à rechercher
     * @return Le fournisseur trouvé ou null
     */
    public Fournisseur rechercherParEmail(String email) {
        String sql = "SELECT * FROM fournisseurs WHERE email = ?";
        
        try (Connection conn = dbManager.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setString(1, email);
            
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return mapperResultSet(rs);
                }
            }
            
        } catch (SQLException e) {
            System.err.println("Erreur lors de la recherche par email: " + e.getMessage());
            e.printStackTrace();
        }
        
        return null;
    }
    
    /**
     * Compte le nombre total de fournisseurs
     * 
     * @return Nombre de fournisseurs
     */
    public int compter() {
        String sql = "SELECT COUNT(*) FROM fournisseurs";
        
        try (Connection conn = dbManager.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            
            if (rs.next()) {
                return rs.getInt(1);
            }
            
        } catch (SQLException e) {
            System.err.println("Erreur lors du comptage des fournisseurs: " + e.getMessage());
            e.printStackTrace();
        }
        
        return 0;
    }
    
    /**
     * Vérifie si un fournisseur existe par son email
     * 
     * @param email Email à vérifier
     * @return true si le fournisseur existe
     */
    public boolean existeParEmail(String email) {
        return rechercherParEmail(email) != null;
    }
    
    /**
     * Mappe un ResultSet vers un objet Fournisseur
     * 
     * @param rs ResultSet à mapper
     * @return Fournisseur créé
     * @throws SQLException Si erreur SQL
     */
    private Fournisseur mapperResultSet(ResultSet rs) throws SQLException {
        long id = rs.getLong("id");
        String nom = rs.getString("nom");
        String adresse = rs.getString("adresse");
        String telephone = rs.getString("telephone");
        String email = rs.getString("email");
        String contact = rs.getString("contact");
        String notes = rs.getString("notes");
        
        return new Fournisseur(id, nom, adresse, telephone, email, contact, notes);
    }
}

