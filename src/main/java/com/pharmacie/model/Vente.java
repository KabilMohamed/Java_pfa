package com.pharmacie.model;

import java.io.Serializable;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Objects;

/**
 * Classe représentant une vente de médicament
 * Enregistre les détails d'une transaction de vente
 */
public class Vente implements Serializable {
    
    private static final long serialVersionUID = 1L;
    
    private long id;
    private Medicament medicament;
    private int quantite;
    private double prixUnitaire;
    private double montantTotal;
    private LocalDateTime dateVente;
    private String client;
    private String notes;
    
    /**
     * Constructeur par défaut
     */
    public Vente() {
        this.dateVente = LocalDateTime.now();
    }
    
    /**
     * Constructeur avec les champs essentiels
     * 
     * @param id Identifiant unique de la vente
     * @param medicament Médicament vendu
     * @param quantite Quantité vendue
     * @param prixUnitaire Prix unitaire au moment de la vente
     * @param dateVente Date et heure de la vente
     */
    public Vente(long id, Medicament medicament, int quantite, 
                 double prixUnitaire, LocalDateTime dateVente) {
        this.id = id;
        this.medicament = medicament;
        this.quantite = quantite;
        this.prixUnitaire = prixUnitaire;
        this.dateVente = dateVente;
        this.montantTotal = calculerTotal();
    }
    
    /**
     * Constructeur simplifié avec date automatique
     * 
     * @param id Identifiant unique de la vente
     * @param medicament Médicament vendu
     * @param quantite Quantité vendue
     * @param prixUnitaire Prix unitaire
     */
    public Vente(long id, Medicament medicament, int quantite, double prixUnitaire) {
        this(id, medicament, quantite, prixUnitaire, LocalDateTime.now());
    }
    
    /**
     * Constructeur avec tous les champs
     */
    public Vente(long id, Medicament medicament, int quantite, double prixUnitaire, 
                 LocalDateTime dateVente, String client, String notes) {
        this(id, medicament, quantite, prixUnitaire, dateVente);
        this.client = client;
        this.notes = notes;
    }
    
    // Getters et Setters
    
    public long getId() {
        return id;
    }
    
    public void setId(long id) {
        this.id = id;
    }
    
    public Medicament getMedicament() {
        return medicament;
    }
    
    public void setMedicament(Medicament medicament) {
        this.medicament = medicament;
        // Recalculer le total si le médicament change
        if (this.prixUnitaire == 0 && medicament != null) {
            this.prixUnitaire = medicament.getPrix();
        }
        this.montantTotal = calculerTotal();
    }
    
    public int getQuantite() {
        return quantite;
    }
    
    public void setQuantite(int quantite) {
        this.quantite = quantite;
        this.montantTotal = calculerTotal();
    }
    
    public double getPrixUnitaire() {
        return prixUnitaire;
    }
    
    public void setPrixUnitaire(double prixUnitaire) {
        this.prixUnitaire = prixUnitaire;
        this.montantTotal = calculerTotal();
    }
    
    public double getMontantTotal() {
        return montantTotal;
    }
    
    public void setMontantTotal(double montantTotal) {
        this.montantTotal = montantTotal;
    }
    
    public LocalDateTime getDateVente() {
        return dateVente;
    }
    
    public void setDateVente(LocalDateTime dateVente) {
        this.dateVente = dateVente;
    }
    
    public String getClient() {
        return client;
    }
    
    public void setClient(String client) {
        this.client = client;
    }
    
    public String getNotes() {
        return notes;
    }
    
    public void setNotes(String notes) {
        this.notes = notes;
    }
    
    /**
     * Calcule le montant total de la vente
     * 
     * @return Montant total (prix unitaire * quantité)
     */
    public double calculerTotal() {
        return prixUnitaire * quantite;
    }
    
    /**
     * Obtient la date de vente sans l'heure
     * 
     * @return Date de la vente (LocalDate)
     */
    public LocalDate getDateSeulement() {
        return dateVente != null ? dateVente.toLocalDate() : null;
    }
    
    /**
     * Vérifie si la vente date d'aujourd'hui
     * 
     * @return true si la vente a été effectuée aujourd'hui
     */
    public boolean estVenteJour() {
        return getDateSeulement() != null && 
               getDateSeulement().equals(LocalDate.now());
    }
    
    /**
     * Vérifie si la vente date de ce mois
     * 
     * @return true si la vente a été effectuée ce mois-ci
     */
    public boolean estVenteMois() {
        if (getDateSeulement() == null) {
            return false;
        }
        LocalDate now = LocalDate.now();
        LocalDate dateVente = getDateSeulement();
        return dateVente.getYear() == now.getYear() && 
               dateVente.getMonth() == now.getMonth();
    }
    
    /**
     * Vérifie si la vente est dans une période donnée
     * 
     * @param debut Date de début de la période
     * @param fin Date de fin de la période
     * @return true si la vente est dans la période
     */
    public boolean estDansPeriode(LocalDate debut, LocalDate fin) {
        LocalDate dateVente = getDateSeulement();
        if (dateVente == null) {
            return false;
        }
        return !dateVente.isBefore(debut) && !dateVente.isAfter(fin);
    }
    
    /**
     * Applique une remise sur le montant total
     * 
     * @param pourcentageRemise Pourcentage de remise (0-100)
     * @return Nouveau montant après remise
     */
    public double appliquerRemise(double pourcentageRemise) {
        if (pourcentageRemise < 0 || pourcentageRemise > 100) {
            throw new IllegalArgumentException("Le pourcentage de remise doit être entre 0 et 100");
        }
        double remise = montantTotal * (pourcentageRemise / 100);
        this.montantTotal = montantTotal - remise;
        return this.montantTotal;
    }
    
    /**
     * Calcule la marge bénéficiaire (si on connaît le prix d'achat)
     * Note: Nécessiterait un prix d'achat dans le modèle pour être complet
     * 
     * @param prixAchatUnitaire Prix d'achat unitaire
     * @return Marge bénéficiaire totale
     */
    public double calculerMarge(double prixAchatUnitaire) {
        double coutTotal = prixAchatUnitaire * quantite;
        return montantTotal - coutTotal;
    }
    
    /**
     * Vérifie si la vente est valide
     * 
     * @return true si tous les champs obligatoires sont renseignés
     */
    public boolean estValide() {
        return medicament != null && 
               quantite > 0 && 
               prixUnitaire > 0 && 
               dateVente != null;
    }
    
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Vente vente = (Vente) o;
        return id == vente.id;
    }
    
    @Override
    public int hashCode() {
        return Objects.hash(id);
    }
    
    @Override
    public String toString() {
        return "Vente{" +
                "id=" + id +
                ", medicament=" + (medicament != null ? medicament.getNom() : "N/A") +
                ", quantite=" + quantite +
                ", prixUnitaire=" + prixUnitaire +
                ", montantTotal=" + montantTotal +
                ", dateVente=" + dateVente +
                ", client='" + client + '\'' +
                '}';
    }
}

