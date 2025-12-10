package com.pharmacie.dao;

import com.pharmacie.model.Medicament;
import com.pharmacie.model.Fournisseur;

import java.sql.*;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

/**
 * DAO pour la gestion des médicaments dans la base de données
 * Fournit les opérations CRUD (Create, Read, Update, Delete)
 */
public class MedicamentDAO {
    
    private DatabaseManager dbManager;
    private FournisseurDAO fournisseurDAO;
    
    /**
     * Constructeur
     */
    public MedicamentDAO() {
        this.dbManager = DatabaseManager.getInstance();
        this.fournisseurDAO = new FournisseurDAO();
    }
    
    /**
     * Ajoute un médicament à la base de données
     * 
     * @param medicament Médicament à ajouter
     */
    public void ajouter(Medicament medicament) {
        String sql = "INSERT INTO medicaments (nom, categorie, prix, quantite, date_expiration, fournisseur_id) " +
                     "VALUES (?, ?, ?, ?, ?, ?)";
        
        try (Connection conn = dbManager.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            
            stmt.setString(1, medicament.getNom());
            stmt.setString(2, medicament.getCategorie());
            stmt.setDouble(3, medicament.getPrix());
            stmt.setInt(4, medicament.getQuantite());
            stmt.setDate(5, Date.valueOf(medicament.getDateExpiration()));
            
            if (medicament.getFournisseur() != null) {
                stmt.setLong(6, medicament.getFournisseur().getId());
            } else {
                stmt.setNull(6, Types.BIGINT);
            }
            
            int rowsAffected = stmt.executeUpdate();
            
            if (rowsAffected > 0) {
                // Récupérer l'ID généré
                try (ResultSet generatedKeys = stmt.getGeneratedKeys()) {
                    if (generatedKeys.next()) {
                        medicament.setId(generatedKeys.getLong(1));
                    }
                }
            }
            
        } catch (SQLException e) {
            System.err.println("Erreur lors de l'ajout du médicament: " + e.getMessage());
            e.printStackTrace();
        }
    }
    
    /**
     * Met à jour un médicament existant
     * 
     * @param medicament Médicament à mettre à jour
     */
    public void mettreAJour(Medicament medicament) {
        String sql = "UPDATE medicaments SET nom = ?, categorie = ?, prix = ?, quantite = ?, " +
                     "date_expiration = ?, fournisseur_id = ? WHERE id = ?";
        
        try (Connection conn = dbManager.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setString(1, medicament.getNom());
            stmt.setString(2, medicament.getCategorie());
            stmt.setDouble(3, medicament.getPrix());
            stmt.setInt(4, medicament.getQuantite());
            stmt.setDate(5, Date.valueOf(medicament.getDateExpiration()));
            
            if (medicament.getFournisseur() != null) {
                stmt.setLong(6, medicament.getFournisseur().getId());
            } else {
                stmt.setNull(6, Types.BIGINT);
            }
            
            stmt.setLong(7, medicament.getId());
            
            stmt.executeUpdate();
            
        } catch (SQLException e) {
            System.err.println("Erreur lors de la mise à jour du médicament: " + e.getMessage());
            e.printStackTrace();
        }
    }
    
    /**
     * Supprime un médicament de la base de données
     * 
     * @param id Identifiant du médicament à supprimer
     */
    public void supprimer(long id) {
        String sql = "DELETE FROM medicaments WHERE id = ?";
        
        try (Connection conn = dbManager.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setLong(1, id);
            stmt.executeUpdate();
            
        } catch (SQLException e) {
            System.err.println("Erreur lors de la suppression du médicament: " + e.getMessage());
            e.printStackTrace();
        }
    }
    
    /**
     * Récupère un médicament par son identifiant
     * 
     * @param id Identifiant du médicament
     * @return Le médicament trouvé ou null
     */
    public Medicament obtenirParId(long id) {
        String sql = "SELECT * FROM medicaments WHERE id = ?";
        
        try (Connection conn = dbManager.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setLong(1, id);
            
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return mapperResultSet(rs);
                }
            }
            
        } catch (SQLException e) {
            System.err.println("Erreur lors de la récupération du médicament: " + e.getMessage());
            e.printStackTrace();
        }
        
        return null;
    }
    
    /**
     * Récupère tous les médicaments
     * 
     * @return Liste de tous les médicaments
     */
    public List<Medicament> obtenirTous() {
        List<Medicament> medicaments = new ArrayList<>();
        String sql = "SELECT * FROM medicaments ORDER BY nom";
        
        try (Connection conn = dbManager.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            
            while (rs.next()) {
                medicaments.add(mapperResultSet(rs));
            }
            
        } catch (SQLException e) {
            System.err.println("Erreur lors de la récupération des médicaments: " + e.getMessage());
            e.printStackTrace();
        }
        
        return medicaments;
    }
    
    /**
     * Recherche des médicaments par nom
     * 
     * @param nom Nom à rechercher (recherche partielle)
     * @return Liste des médicaments correspondants
     */
    public List<Medicament> rechercherParNom(String nom) {
        List<Medicament> medicaments = new ArrayList<>();
        String sql = "SELECT * FROM medicaments WHERE nom LIKE ? ORDER BY nom";
        
        try (Connection conn = dbManager.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setString(1, "%" + nom + "%");
            
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    medicaments.add(mapperResultSet(rs));
                }
            }
            
        } catch (SQLException e) {
            System.err.println("Erreur lors de la recherche de médicaments: " + e.getMessage());
            e.printStackTrace();
        }
        
        return medicaments;
    }
    
    /**
     * Récupère les médicaments par catégorie
     * 
     * @param categorie Catégorie recherchée
     * @return Liste des médicaments de cette catégorie
     */
    public List<Medicament> obtenirParCategorie(String categorie) {
        List<Medicament> medicaments = new ArrayList<>();
        String sql = "SELECT * FROM medicaments WHERE categorie = ? ORDER BY nom";
        
        try (Connection conn = dbManager.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setString(1, categorie);
            
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    medicaments.add(mapperResultSet(rs));
                }
            }
            
        } catch (SQLException e) {
            System.err.println("Erreur lors de la récupération par catégorie: " + e.getMessage());
            e.printStackTrace();
        }
        
        return medicaments;
    }
    
    /**
     * Récupère les médicaments d'un fournisseur
     * 
     * @param fournisseurId Identifiant du fournisseur
     * @return Liste des médicaments de ce fournisseur
     */
    public List<Medicament> obtenirParFournisseur(long fournisseurId) {
        List<Medicament> medicaments = new ArrayList<>();
        String sql = "SELECT * FROM medicaments WHERE fournisseur_id = ? ORDER BY nom";
        
        try (Connection conn = dbManager.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setLong(1, fournisseurId);
            
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    medicaments.add(mapperResultSet(rs));
                }
            }
            
        } catch (SQLException e) {
            System.err.println("Erreur lors de la récupération par fournisseur: " + e.getMessage());
            e.printStackTrace();
        }
        
        return medicaments;
    }
    
    /**
     * Récupère les médicaments expirés
     * 
     * @return Liste des médicaments expirés
     */
    public List<Medicament> obtenirExpires() {
        List<Medicament> medicaments = new ArrayList<>();
        String sql = "SELECT * FROM medicaments WHERE date_expiration < CURDATE() ORDER BY date_expiration";
        
        try (Connection conn = dbManager.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            
            while (rs.next()) {
                medicaments.add(mapperResultSet(rs));
            }
            
        } catch (SQLException e) {
            System.err.println("Erreur lors de la récupération des médicaments expirés: " + e.getMessage());
            e.printStackTrace();
        }
        
        return medicaments;
    }
    
    /**
     * Récupère les médicaments en stock faible
     * 
     * @param seuil Seuil de quantité
     * @return Liste des médicaments en stock faible
     */
    public List<Medicament> obtenirStockFaible(int seuil) {
        List<Medicament> medicaments = new ArrayList<>();
        String sql = "SELECT * FROM medicaments WHERE quantite <= ? ORDER BY quantite";
        
        try (Connection conn = dbManager.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setInt(1, seuil);
            
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    medicaments.add(mapperResultSet(rs));
                }
            }
            
        } catch (SQLException e) {
            System.err.println("Erreur lors de la récupération du stock faible: " + e.getMessage());
            e.printStackTrace();
        }
        
        return medicaments;
    }
    
    /**
     * Compte le nombre total de médicaments
     * 
     * @return Nombre de médicaments
     */
    public int compter() {
        String sql = "SELECT COUNT(*) FROM medicaments";
        
        try (Connection conn = dbManager.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            
            if (rs.next()) {
                return rs.getInt(1);
            }
            
        } catch (SQLException e) {
            System.err.println("Erreur lors du comptage des médicaments: " + e.getMessage());
            e.printStackTrace();
        }
        
        return 0;
    }
    
    /**
     * Mappe un ResultSet vers un objet Medicament
     * 
     * @param rs ResultSet à mapper
     * @return Medicament créé
     * @throws SQLException Si erreur SQL
     */
    private Medicament mapperResultSet(ResultSet rs) throws SQLException {
        long id = rs.getLong("id");
        String nom = rs.getString("nom");
        String categorie = rs.getString("categorie");
        double prix = rs.getDouble("prix");
        int quantite = rs.getInt("quantite");
        LocalDate dateExpiration = rs.getDate("date_expiration").toLocalDate();
        
        // Récupérer le fournisseur
        Fournisseur fournisseur = null;
        long fournisseurId = rs.getLong("fournisseur_id");
        if (!rs.wasNull()) {
            fournisseur = fournisseurDAO.obtenirParId(fournisseurId);
        }
        
        return new Medicament(id, nom, categorie, prix, quantite, dateExpiration, fournisseur);
    }
}

