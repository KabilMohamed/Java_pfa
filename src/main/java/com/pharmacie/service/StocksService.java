package com.pharmacie.service;

import com.pharmacie.model.Medicament;
import com.pharmacie.dao.MedicamentDAO;
import com.pharmacie.exception.DonneeInvalideException;

import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Service de gestion des stocks de médicaments
 * Fournit les opérations métier pour la gestion des médicaments
 */
public class StocksService {
    
    private MedicamentDAO medicamentDAO;
    
    /**
     * Constructeur
     */
    public StocksService() {
        this.medicamentDAO = new MedicamentDAO();
    }
    
    /**
     * Ajoute un nouveau médicament au stock
     * 
     * @param medicament Le médicament à ajouter
     * @throws DonneeInvalideException Si les données sont invalides
     */
    public void ajouterMedicament(Medicament medicament) throws DonneeInvalideException {
        // Validation
        validerMedicament(medicament);
        
        // Ajout via DAO
        medicamentDAO.ajouter(medicament);
    }
    
    /**
     * Met à jour un médicament existant
     * 
     * @param medicament Le médicament à mettre à jour
     * @throws DonneeInvalideException Si les données sont invalides
     */
    public void modifierMedicament(Medicament medicament) throws DonneeInvalideException {
        validerMedicament(medicament);
        medicamentDAO.mettreAJour(medicament);
    }
    
    /**
     * Supprime un médicament du stock
     * 
     * @param id Identifiant du médicament à supprimer
     */
    public void supprimerMedicament(long id) {
        medicamentDAO.supprimer(id);
    }
    
    /**
     * Récupère un médicament par son identifiant
     * 
     * @param id Identifiant du médicament
     * @return Le médicament trouvé ou null
     */
    public Medicament obtenirMedicamentParId(long id) {
        return medicamentDAO.obtenirParId(id);
    }
    
    /**
     * Récupère tous les médicaments
     * 
     * @return Liste de tous les médicaments
     */
    public List<Medicament> obtenirTousLesMedicaments() {
        return medicamentDAO.obtenirTous();
    }
    
    /**
     * Recherche des médicaments par nom
     * 
     * @param nom Nom à rechercher
     * @return Liste des médicaments correspondants
     */
    public List<Medicament> rechercherParNom(String nom) {
        return medicamentDAO.rechercherParNom(nom);
    }
    
    /**
     * Obtient les médicaments d'une catégorie
     * 
     * @param categorie Catégorie recherchée
     * @return Liste des médicaments de cette catégorie
     */
    public List<Medicament> obtenirParCategorie(String categorie) {
        return obtenirTousLesMedicaments().stream()
                .filter(m -> m.getCategorie().equalsIgnoreCase(categorie))
                .collect(Collectors.toList());
    }
    
    /**
     * Obtient les médicaments d'un fournisseur
     * 
     * @param fournisseurId Identifiant du fournisseur
     * @return Liste des médicaments de ce fournisseur
     */
    public List<Medicament> obtenirParFournisseur(long fournisseurId) {
        return obtenirTousLesMedicaments().stream()
                .filter(m -> m.getFournisseur() != null && 
                            m.getFournisseur().getId() == fournisseurId)
                .collect(Collectors.toList());
    }
    
    /**
     * Obtient les médicaments expirés
     * Utilise les Streams Java pour filtrer
     * 
     * @return Liste des médicaments expirés
     */
    public List<Medicament> obtenirMedicamentsExpires() {
        return obtenirTousLesMedicaments().stream()
                .filter(Medicament::isExpired)
                .collect(Collectors.toList());
    }
    
    /**
     * Obtient les médicaments proches de l'expiration
     * 
     * @param joursAlerte Nombre de jours avant expiration pour l'alerte
     * @return Liste des médicaments proches de l'expiration
     */
    public List<Medicament> obtenirMedicamentsProchesExpiration(int joursAlerte) {
        return obtenirTousLesMedicaments().stream()
                .filter(m -> m.isProcheExpiration(joursAlerte))
                .collect(Collectors.toList());
    }
    
    /**
     * Obtient les médicaments en stock faible
     * 
     * @param seuil Seuil de quantité considéré comme faible
     * @return Liste des médicaments en stock faible
     */
    public List<Medicament> obtenirMedicamentsStockFaible(int seuil) {
        return obtenirTousLesMedicaments().stream()
                .filter(m -> m.isStockFaible(seuil))
                .collect(Collectors.toList());
    }
    
    /**
     * Obtient les médicaments en rupture de stock
     * 
     * @return Liste des médicaments en rupture
     */
    public List<Medicament> obtenirMedicamentsRupture() {
        return obtenirTousLesMedicaments().stream()
                .filter(Medicament::isRuptureStock)
                .collect(Collectors.toList());
    }
    
    /**
     * Vérifie la disponibilité d'un médicament pour la vente
     * 
     * @param medicamentId Identifiant du médicament
     * @param quantiteDemandee Quantité demandée
     * @return true si disponible en quantité suffisante et non expiré
     */
    public boolean verifierDisponibilite(long medicamentId, int quantiteDemandee) {
        Medicament medicament = obtenirMedicamentParId(medicamentId);
        if (medicament == null) {
            return false;
        }
        return !medicament.isExpired() && medicament.getQuantite() >= quantiteDemandee;
    }
    
    /**
     * Réapprovisionne un médicament
     * 
     * @param medicamentId Identifiant du médicament
     * @param quantite Quantité à ajouter
     * @throws DonneeInvalideException Si les données sont invalides
     */
    public void reapprovisionner(long medicamentId, int quantite) throws DonneeInvalideException {
        if (quantite <= 0) {
            throw new DonneeInvalideException("La quantité doit être positive");
        }
        
        Medicament medicament = obtenirMedicamentParId(medicamentId);
        if (medicament == null) {
            throw new DonneeInvalideException("Médicament introuvable");
        }
        
        medicament.augmenterStock(quantite);
        medicamentDAO.mettreAJour(medicament);
    }
    
    /**
     * Calcule la valeur totale du stock
     * 
     * @return Valeur totale du stock
     */
    public double calculerValeurTotaleStock() {
        return obtenirTousLesMedicaments().stream()
                .mapToDouble(Medicament::calculerValeurStock)
                .sum();
    }
    
    /**
     * Obtient le nombre total de médicaments différents
     * 
     * @return Nombre de médicaments
     */
    public int obtenirNombreMedicaments() {
        return obtenirTousLesMedicaments().size();
    }
    
    /**
     * Obtient la quantité totale en stock (tous médicaments confondus)
     * 
     * @return Quantité totale
     */
    public int obtenirQuantiteTotale() {
        return obtenirTousLesMedicaments().stream()
                .mapToInt(Medicament::getQuantite)
                .sum();
    }
    
    /**
     * Valide les données d'un médicament
     * 
     * @param medicament Médicament à valider
     * @throws DonneeInvalideException Si les données sont invalides
     */
    private void validerMedicament(Medicament medicament) throws DonneeInvalideException {
        if (medicament == null) {
            throw new DonneeInvalideException("Le médicament ne peut pas être null");
        }
        if (medicament.getNom() == null || medicament.getNom().trim().isEmpty()) {
            throw new DonneeInvalideException("Le nom du médicament est obligatoire");
        }
        if (medicament.getCategorie() == null || medicament.getCategorie().trim().isEmpty()) {
            throw new DonneeInvalideException("La catégorie est obligatoire");
        }
        if (medicament.getPrix() < 0) {
            throw new DonneeInvalideException("Le prix ne peut pas être négatif");
        }
        if (medicament.getQuantite() < 0) {
            throw new DonneeInvalideException("La quantité ne peut pas être négative");
        }
        if (medicament.getDateExpiration() == null) {
            throw new DonneeInvalideException("La date d'expiration est obligatoire");
        }
        if (medicament.getFournisseur() == null) {
            throw new DonneeInvalideException("Le fournisseur est obligatoire");
        }
    }
}

