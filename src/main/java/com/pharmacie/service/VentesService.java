package com.pharmacie.service;

import com.pharmacie.model.Medicament;
import com.pharmacie.model.Vente;
import com.pharmacie.dao.VenteDAO;
import com.pharmacie.dao.MedicamentDAO;
import com.pharmacie.exception.StockInsuffisantException;
import com.pharmacie.exception.MedicamentExpireException;
import com.pharmacie.exception.DonneeInvalideException;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Service de gestion des ventes
 * Gère l'enregistrement des ventes et la réduction automatique du stock
 */
public class VentesService {
    
    private VenteDAO venteDAO;
    private MedicamentDAO medicamentDAO;
    
    /**
     * Constructeur
     */
    public VentesService() {
        this.venteDAO = new VenteDAO();
        this.medicamentDAO = new MedicamentDAO();
    }
    
    /**
     * Enregistre une nouvelle vente
     * Réduit automatiquement le stock et vérifie les contraintes
     * 
     * @param medicament Médicament vendu
     * @param quantite Quantité vendue
     * @param dateVente Date de la vente
     * @return La vente enregistrée
     * @throws StockInsuffisantException Si le stock est insuffisant
     * @throws MedicamentExpireException Si le médicament est expiré
     * @throws DonneeInvalideException Si les données sont invalides
     */
    public Vente enregistrerVente(Medicament medicament, int quantite, LocalDate dateVente) 
            throws StockInsuffisantException, MedicamentExpireException, DonneeInvalideException {
        
        // Validation des données
        if (medicament == null) {
            throw new DonneeInvalideException("Le médicament ne peut pas être null");
        }
        if (quantite <= 0) {
            throw new DonneeInvalideException("La quantité doit être supérieure à 0");
        }
        if (dateVente == null) {
            throw new DonneeInvalideException("La date de vente est obligatoire");
        }
        
        // Vérification de l'expiration
        if (medicament.isExpired()) {
            throw new MedicamentExpireException(medicament);
        }
        
        // Vérification du stock
        if (medicament.getQuantite() < quantite) {
            throw new StockInsuffisantException(medicament, quantite, medicament.getQuantite());
        }
        
        // Création de la vente
        Vente vente = new Vente(
            0, // L'ID sera généré par le DAO
            medicament,
            quantite,
            medicament.getPrix(),
            dateVente.atStartOfDay()
        );
        
        // Réduction du stock
        medicament.reduireStock(quantite);
        medicamentDAO.mettreAJour(medicament);
        
        // Enregistrement de la vente
        venteDAO.ajouter(vente);
        
        return vente;
    }
    
    /**
     * Enregistre une vente avec la date du jour
     * 
     * @param medicament Médicament vendu
     * @param quantite Quantité vendue
     * @return La vente enregistrée
     * @throws StockInsuffisantException Si le stock est insuffisant
     * @throws MedicamentExpireException Si le médicament est expiré
     * @throws DonneeInvalideException Si les données sont invalides
     */
    public Vente enregistrerVente(Medicament medicament, int quantite) 
            throws StockInsuffisantException, MedicamentExpireException, DonneeInvalideException {
        return enregistrerVente(medicament, quantite, LocalDate.now());
    }
    
    /**
     * Obtient toutes les ventes
     * 
     * @return Liste de toutes les ventes
     */
    public List<Vente> obtenirToutesLesVentes() {
        return venteDAO.obtenirTous();
    }
    
    /**
     * Obtient une vente par son identifiant
     * 
     * @param id Identifiant de la vente
     * @return La vente trouvée ou null
     */
    public Vente obtenirVenteParId(long id) {
        return venteDAO.obtenirParId(id);
    }
    
    /**
     * Obtient les ventes d'une date spécifique
     * 
     * @param date Date des ventes
     * @return Liste des ventes de cette date
     */
    public List<Vente> obtenirVentesParDate(LocalDate date) {
        return venteDAO.obtenirVentesParDate(date);
    }
    
    /**
     * Obtient les ventes d'une période
     * 
     * @param debut Date de début
     * @param fin Date de fin
     * @return Liste des ventes de la période
     */
    public List<Vente> obtenirVentesParPeriode(LocalDate debut, LocalDate fin) {
        return obtenirToutesLesVentes().stream()
                .filter(v -> v.estDansPeriode(debut, fin))
                .collect(Collectors.toList());
    }
    
    /**
     * Obtient les ventes du jour
     * 
     * @return Liste des ventes d'aujourd'hui
     */
    public List<Vente> obtenirVentesDuJour() {
        return obtenirVentesParDate(LocalDate.now());
    }
    
    /**
     * Obtient les ventes du mois en cours
     * 
     * @return Liste des ventes du mois
     */
    public List<Vente> obtenirVentesDuMois() {
        return obtenirToutesLesVentes().stream()
                .filter(Vente::estVenteMois)
                .collect(Collectors.toList());
    }
    
    /**
     * Obtient les ventes d'un médicament spécifique
     * 
     * @param medicamentId Identifiant du médicament
     * @return Liste des ventes de ce médicament
     */
    public List<Vente> obtenirVentesParMedicament(long medicamentId) {
        return obtenirToutesLesVentes().stream()
                .filter(v -> v.getMedicament().getId() == medicamentId)
                .collect(Collectors.toList());
    }
    
    /**
     * Calcule le chiffre d'affaires d'une période
     * 
     * @param debut Date de début
     * @param fin Date de fin
     * @return Chiffre d'affaires total
     */
    public double calculerChiffreAffaires(LocalDate debut, LocalDate fin) {
        return obtenirVentesParPeriode(debut, fin).stream()
                .mapToDouble(Vente::getMontantTotal)
                .sum();
    }
    
    /**
     * Calcule le chiffre d'affaires du jour
     * 
     * @return Chiffre d'affaires d'aujourd'hui
     */
    public double calculerChiffreAffairesJour() {
        return obtenirVentesDuJour().stream()
                .mapToDouble(Vente::getMontantTotal)
                .sum();
    }
    
    /**
     * Calcule le chiffre d'affaires du mois
     * 
     * @return Chiffre d'affaires du mois en cours
     */
    public double calculerChiffreAffairesMois() {
        return obtenirVentesDuMois().stream()
                .mapToDouble(Vente::getMontantTotal)
                .sum();
    }
    
    /**
     * Obtient le nombre de ventes d'une période
     * 
     * @param debut Date de début
     * @param fin Date de fin
     * @return Nombre de ventes
     */
    public int obtenirNombreVentes(LocalDate debut, LocalDate fin) {
        return obtenirVentesParPeriode(debut, fin).size();
    }
    
    /**
     * Obtient le nombre de ventes du jour
     * 
     * @return Nombre de ventes aujourd'hui
     */
    public int obtenirNombreVentesJour() {
        return obtenirVentesDuJour().size();
    }
    
    /**
     * Calcule la quantité totale vendue pour un médicament
     * 
     * @param medicamentId Identifiant du médicament
     * @return Quantité totale vendue
     */
    public int calculerQuantiteVendue(long medicamentId) {
        return obtenirVentesParMedicament(medicamentId).stream()
                .mapToInt(Vente::getQuantite)
                .sum();
    }
    
    /**
     * Calcule le montant moyen des ventes
     * 
     * @return Montant moyen par vente
     */
    public double calculerMontantMoyenVente() {
        List<Vente> ventes = obtenirToutesLesVentes();
        if (ventes.isEmpty()) {
            return 0.0;
        }
        return ventes.stream()
                .mapToDouble(Vente::getMontantTotal)
                .average()
                .orElse(0.0);
    }
    
    /**
     * Annule une vente et restaure le stock
     * 
     * @param venteId Identifiant de la vente à annuler
     * @throws DonneeInvalideException Si la vente n'existe pas
     */
    public void annulerVente(long venteId) throws DonneeInvalideException {
        Vente vente = obtenirVenteParId(venteId);
        if (vente == null) {
            throw new DonneeInvalideException("Vente introuvable");
        }
        
        // Restauration du stock
        Medicament medicament = vente.getMedicament();
        medicament.augmenterStock(vente.getQuantite());
        medicamentDAO.mettreAJour(medicament);
        
        // Suppression de la vente
        venteDAO.supprimer(venteId);
    }
}

