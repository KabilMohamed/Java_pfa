package com.pharmacie.dao;

import com.pharmacie.model.Vente;
import com.pharmacie.model.Medicament;

import java.sql.*;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * DAO pour la gestion des ventes dans la base de données
 * Fournit les opérations CRUD pour les ventes
 */
public class VenteDAO {
    
    private DatabaseManager dbManager;
    private MedicamentDAO medicamentDAO;
    
    /**
     * Constructeur
     */
    public VenteDAO() {
        this.dbManager = DatabaseManager.getInstance();
        this.medicamentDAO = new MedicamentDAO();
    }
    
    /**
     * Ajoute une vente à la base de données
     * 
     * @param vente Vente à ajouter
     */
    public void ajouter(Vente vente) {
        String sql = "INSERT INTO ventes (medicament_id, quantite, prix_unitaire, montant_total, " +
                     "date_vente, client, notes) VALUES (?, ?, ?, ?, ?, ?, ?)";
        
        try (Connection conn = dbManager.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            
            stmt.setLong(1, vente.getMedicament().getId());
            stmt.setInt(2, vente.getQuantite());
            stmt.setDouble(3, vente.getPrixUnitaire());
            stmt.setDouble(4, vente.getMontantTotal());
            stmt.setTimestamp(5, Timestamp.valueOf(vente.getDateVente()));
            stmt.setString(6, vente.getClient());
            stmt.setString(7, vente.getNotes());
            
            int rowsAffected = stmt.executeUpdate();
            
            if (rowsAffected > 0) {
                // Récupérer l'ID généré
                try (ResultSet generatedKeys = stmt.getGeneratedKeys()) {
                    if (generatedKeys.next()) {
                        vente.setId(generatedKeys.getLong(1));
                    }
                }
            }
            
        } catch (SQLException e) {
            System.err.println("Erreur lors de l'ajout de la vente: " + e.getMessage());
            e.printStackTrace();
        }
    }
    
    /**
     * Met à jour une vente existante
     * 
     * @param vente Vente à mettre à jour
     */
    public void mettreAJour(Vente vente) {
        String sql = "UPDATE ventes SET medicament_id = ?, quantite = ?, prix_unitaire = ?, " +
                     "montant_total = ?, date_vente = ?, client = ?, notes = ? WHERE id = ?";
        
        try (Connection conn = dbManager.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setLong(1, vente.getMedicament().getId());
            stmt.setInt(2, vente.getQuantite());
            stmt.setDouble(3, vente.getPrixUnitaire());
            stmt.setDouble(4, vente.getMontantTotal());
            stmt.setTimestamp(5, Timestamp.valueOf(vente.getDateVente()));
            stmt.setString(6, vente.getClient());
            stmt.setString(7, vente.getNotes());
            stmt.setLong(8, vente.getId());
            
            stmt.executeUpdate();
            
        } catch (SQLException e) {
            System.err.println("Erreur lors de la mise à jour de la vente: " + e.getMessage());
            e.printStackTrace();
        }
    }
    
    /**
     * Supprime une vente de la base de données
     * 
     * @param id Identifiant de la vente à supprimer
     */
    public void supprimer(long id) {
        String sql = "DELETE FROM ventes WHERE id = ?";
        
        try (Connection conn = dbManager.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setLong(1, id);
            stmt.executeUpdate();
            
        } catch (SQLException e) {
            System.err.println("Erreur lors de la suppression de la vente: " + e.getMessage());
            e.printStackTrace();
        }
    }
    
    /**
     * Récupère une vente par son identifiant
     * 
     * @param id Identifiant de la vente
     * @return La vente trouvée ou null
     */
    public Vente obtenirParId(long id) {
        String sql = "SELECT * FROM ventes WHERE id = ?";
        
        try (Connection conn = dbManager.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setLong(1, id);
            
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return mapperResultSet(rs);
                }
            }
            
        } catch (SQLException e) {
            System.err.println("Erreur lors de la récupération de la vente: " + e.getMessage());
            e.printStackTrace();
        }
        
        return null;
    }
    
    /**
     * Récupère toutes les ventes
     * 
     * @return Liste de toutes les ventes
     */
    public List<Vente> obtenirTous() {
        List<Vente> ventes = new ArrayList<>();
        String sql = "SELECT * FROM ventes ORDER BY date_vente DESC";
        
        try (Connection conn = dbManager.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            
            while (rs.next()) {
                ventes.add(mapperResultSet(rs));
            }
            
        } catch (SQLException e) {
            System.err.println("Erreur lors de la récupération des ventes: " + e.getMessage());
            e.printStackTrace();
        }
        
        return ventes;
    }
    
    /**
     * Récupère les ventes d'une date spécifique
     * 
     * @param date Date des ventes
     * @return Liste des ventes de cette date
     */
    public List<Vente> obtenirVentesParDate(LocalDate date) {
        List<Vente> ventes = new ArrayList<>();
        String sql = "SELECT * FROM ventes WHERE DATE(date_vente) = ? ORDER BY date_vente DESC";
        
        try (Connection conn = dbManager.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setDate(1, Date.valueOf(date));
            
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    ventes.add(mapperResultSet(rs));
                }
            }
            
        } catch (SQLException e) {
            System.err.println("Erreur lors de la récupération des ventes par date: " + e.getMessage());
            e.printStackTrace();
        }
        
        return ventes;
    }
    
    /**
     * Récupère les ventes d'une période
     * 
     * @param debut Date de début
     * @param fin Date de fin
     * @return Liste des ventes de la période
     */
    public List<Vente> obtenirVentesParPeriode(LocalDate debut, LocalDate fin) {
        List<Vente> ventes = new ArrayList<>();
        String sql = "SELECT * FROM ventes WHERE DATE(date_vente) BETWEEN ? AND ? ORDER BY date_vente DESC";
        
        try (Connection conn = dbManager.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setDate(1, Date.valueOf(debut));
            stmt.setDate(2, Date.valueOf(fin));
            
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    ventes.add(mapperResultSet(rs));
                }
            }
            
        } catch (SQLException e) {
            System.err.println("Erreur lors de la récupération des ventes par période: " + e.getMessage());
            e.printStackTrace();
        }
        
        return ventes;
    }
    
    /**
     * Récupère les ventes d'un médicament spécifique
     * 
     * @param medicamentId Identifiant du médicament
     * @return Liste des ventes de ce médicament
     */
    public List<Vente> obtenirVentesParMedicament(long medicamentId) {
        List<Vente> ventes = new ArrayList<>();
        String sql = "SELECT * FROM ventes WHERE medicament_id = ? ORDER BY date_vente DESC";
        
        try (Connection conn = dbManager.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setLong(1, medicamentId);
            
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    ventes.add(mapperResultSet(rs));
                }
            }
            
        } catch (SQLException e) {
            System.err.println("Erreur lors de la récupération des ventes par médicament: " + e.getMessage());
            e.printStackTrace();
        }
        
        return ventes;
    }
    
    /**
     * Calcule le chiffre d'affaires d'une période
     * 
     * @param debut Date de début
     * @param fin Date de fin
     * @return Chiffre d'affaires total
     */
    public double calculerChiffreAffaires(LocalDate debut, LocalDate fin) {
        String sql = "SELECT SUM(montant_total) FROM ventes WHERE DATE(date_vente) BETWEEN ? AND ?";
        
        try (Connection conn = dbManager.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setDate(1, Date.valueOf(debut));
            stmt.setDate(2, Date.valueOf(fin));
            
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return rs.getDouble(1);
                }
            }
            
        } catch (SQLException e) {
            System.err.println("Erreur lors du calcul du chiffre d'affaires: " + e.getMessage());
            e.printStackTrace();
        }
        
        return 0.0;
    }
    
    /**
     * Compte le nombre de ventes d'une période
     * 
     * @param debut Date de début
     * @param fin Date de fin
     * @return Nombre de ventes
     */
    public int compterVentes(LocalDate debut, LocalDate fin) {
        String sql = "SELECT COUNT(*) FROM ventes WHERE DATE(date_vente) BETWEEN ? AND ?";
        
        try (Connection conn = dbManager.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setDate(1, Date.valueOf(debut));
            stmt.setDate(2, Date.valueOf(fin));
            
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt(1);
                }
            }
            
        } catch (SQLException e) {
            System.err.println("Erreur lors du comptage des ventes: " + e.getMessage());
            e.printStackTrace();
        }
        
        return 0;
    }
    
    /**
     * Mappe un ResultSet vers un objet Vente
     * 
     * @param rs ResultSet à mapper
     * @return Vente créée
     * @throws SQLException Si erreur SQL
     */
    private Vente mapperResultSet(ResultSet rs) throws SQLException {
        long id = rs.getLong("id");
        long medicamentId = rs.getLong("medicament_id");
        int quantite = rs.getInt("quantite");
        double prixUnitaire = rs.getDouble("prix_unitaire");
        double montantTotal = rs.getDouble("montant_total");
        LocalDateTime dateVente = rs.getTimestamp("date_vente").toLocalDateTime();
        String client = rs.getString("client");
        String notes = rs.getString("notes");
        
        // Récupérer le médicament
        Medicament medicament = medicamentDAO.obtenirParId(medicamentId);
        
        Vente vente = new Vente(id, medicament, quantite, prixUnitaire, dateVente);
        vente.setMontantTotal(montantTotal);
        vente.setClient(client);
        vente.setNotes(notes);
        
        return vente;
    }
}

