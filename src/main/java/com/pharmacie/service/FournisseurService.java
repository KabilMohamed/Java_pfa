package com.pharmacie.service;

import com.pharmacie.model.Fournisseur;
import com.pharmacie.model.Medicament;
import com.pharmacie.dao.FournisseurDAO;
import com.pharmacie.exception.DonneeInvalideException;

import java.io.FileWriter;
import java.io.IOException;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Service de gestion des fournisseurs
 * Fournit les opérations métier pour la gestion des fournisseurs
 */
public class FournisseurService {
    
    private FournisseurDAO fournisseurDAO;
    private StocksService stocksService;
    
    /**
     * Constructeur
     */
    public FournisseurService() {
        this.fournisseurDAO = new FournisseurDAO();
        this.stocksService = new StocksService();
    }
    
    /**
     * Ajoute un nouveau fournisseur
     * 
     * @param fournisseur Le fournisseur à ajouter
     * @throws DonneeInvalideException Si les données sont invalides
     */
    public void ajouterFournisseur(Fournisseur fournisseur) throws DonneeInvalideException {
        validerFournisseur(fournisseur);
        fournisseurDAO.ajouter(fournisseur);
    }
    
    /**
     * Met à jour un fournisseur existant
     * 
     * @param fournisseur Le fournisseur à mettre à jour
     * @throws DonneeInvalideException Si les données sont invalides
     */
    public void modifierFournisseur(Fournisseur fournisseur) throws DonneeInvalideException {
        validerFournisseur(fournisseur);
        fournisseurDAO.mettreAJour(fournisseur);
    }
    
    /**
     * Supprime un fournisseur
     * 
     * @param id Identifiant du fournisseur à supprimer
     */
    public void supprimerFournisseur(long id) {
        fournisseurDAO.supprimer(id);
    }
    
    /**
     * Obtient un fournisseur par son identifiant
     * 
     * @param id Identifiant du fournisseur
     * @return Le fournisseur trouvé ou null
     */
    public Fournisseur obtenirFournisseurParId(long id) {
        return fournisseurDAO.obtenirParId(id);
    }
    
    /**
     * Obtient tous les fournisseurs
     * 
     * @return Liste de tous les fournisseurs
     */
    public List<Fournisseur> obtenirTousLesFournisseurs() {
        return fournisseurDAO.obtenirTous();
    }
    
    /**
     * Recherche des fournisseurs par nom
     * 
     * @param nom Nom à rechercher
     * @return Liste des fournisseurs correspondants
     */
    public List<Fournisseur> rechercherParNom(String nom) {
        return obtenirTousLesFournisseurs().stream()
                .filter(f -> f.getNom().toLowerCase().contains(nom.toLowerCase()))
                .collect(Collectors.toList());
    }
    
    /**
     * Recherche des fournisseurs par ville (dans l'adresse)
     * 
     * @param ville Ville à rechercher
     * @return Liste des fournisseurs de cette ville
     */
    public List<Fournisseur> rechercherParVille(String ville) {
        return obtenirTousLesFournisseurs().stream()
                .filter(f -> f.getAdresse().toLowerCase().contains(ville.toLowerCase()))
                .collect(Collectors.toList());
    }
    
    /**
     * Vérifie si un fournisseur est utilisé par des médicaments
     * 
     * @param fournisseurId Identifiant du fournisseur
     * @return true si le fournisseur a des médicaments associés
     */
    public boolean fournisseurEstUtilise(long fournisseurId) {
        return obtenirNombreMedicaments(fournisseurId) > 0;
    }
    
    /**
     * Obtient le nombre de médicaments d'un fournisseur
     * 
     * @param fournisseurId Identifiant du fournisseur
     * @return Nombre de médicaments
     */
    public int obtenirNombreMedicaments(long fournisseurId) {
        return stocksService.obtenirParFournisseur(fournisseurId).size();
    }
    
    /**
     * Obtient les médicaments d'un fournisseur
     * 
     * @param fournisseurId Identifiant du fournisseur
     * @return Liste des médicaments de ce fournisseur
     */
    public List<Medicament> obtenirMedicamentsDuFournisseur(long fournisseurId) {
        return stocksService.obtenirParFournisseur(fournisseurId);
    }
    
    /**
     * Calcule la valeur du stock fourni par un fournisseur
     * 
     * @param fournisseurId Identifiant du fournisseur
     * @return Valeur totale du stock de ce fournisseur
     */
    public double calculerValeurStockFournisseur(long fournisseurId) {
        return obtenirMedicamentsDuFournisseur(fournisseurId).stream()
                .mapToDouble(Medicament::calculerValeurStock)
                .sum();
    }
    
    /**
     * Obtient le nombre total de fournisseurs
     * 
     * @return Nombre de fournisseurs
     */
    public int obtenirNombreFournisseurs() {
        return obtenirTousLesFournisseurs().size();
    }
    
    /**
     * Exporte la liste des fournisseurs en CSV
     * 
     * @throws IOException Si erreur d'écriture
     */
    public void exporterFournisseursCSV() throws IOException {
        String nomFichier = "fournisseurs_export.csv";
        
        try (FileWriter writer = new FileWriter(nomFichier)) {
            // En-tête
            writer.append("ID,Nom,Adresse,Telephone,Email,Contact,Notes\n");
            
            // Données
            for (Fournisseur f : obtenirTousLesFournisseurs()) {
                writer.append(String.valueOf(f.getId())).append(",");
                writer.append(escaperCSV(f.getNom())).append(",");
                writer.append(escaperCSV(f.getAdresse())).append(",");
                writer.append(escaperCSV(f.getTelephone())).append(",");
                writer.append(escaperCSV(f.getEmail())).append(",");
                writer.append(escaperCSV(f.getContact())).append(",");
                writer.append(escaperCSV(f.getNotes() != null ? f.getNotes() : "")).append("\n");
            }
        }
    }
    
    /**
     * Échappe les caractères spéciaux pour CSV
     * 
     * @param valeur Valeur à échapper
     * @return Valeur échappée
     */
    private String escaperCSV(String valeur) {
        if (valeur == null) {
            return "";
        }
        if (valeur.contains(",") || valeur.contains("\"") || valeur.contains("\n")) {
            return "\"" + valeur.replace("\"", "\"\"") + "\"";
        }
        return valeur;
    }
    
    /**
     * Valide les données d'un fournisseur
     * 
     * @param fournisseur Fournisseur à valider
     * @throws DonneeInvalideException Si les données sont invalides
     */
    private void validerFournisseur(Fournisseur fournisseur) throws DonneeInvalideException {
        if (fournisseur == null) {
            throw new DonneeInvalideException("Le fournisseur ne peut pas être null");
        }
        if (!fournisseur.estValide()) {
            throw new DonneeInvalideException("Les données du fournisseur sont incomplètes");
        }
        
        // Validation de l'email
        if (!validerEmail(fournisseur.getEmail())) {
            throw new DonneeInvalideException("Le format de l'email est invalide");
        }
        
        // Validation du téléphone
        if (fournisseur.getTelephone().length() < 10) {
            throw new DonneeInvalideException("Le téléphone doit contenir au moins 10 chiffres");
        }
    }
    
    /**
     * Valide le format d'un email
     * 
     * @param email Email à valider
     * @return true si le format est valide
     */
    private boolean validerEmail(String email) {
        String emailRegex = "^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$";
        return email != null && email.matches(emailRegex);
    }
}

