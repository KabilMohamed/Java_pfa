package com.pharmacie.model;

import java.io.Serializable;
import java.time.LocalDate;
import java.util.Objects;

/**
 * Classe représentant un médicament dans le stock de la pharmacie
 * Contient toutes les informations nécessaires à la gestion du médicament
 */
public class Medicament implements Serializable {
    
    private static final long serialVersionUID = 1L;
    
    private long id;
    private String nom;
    private String categorie;
    private int quantite;
    private double prix;
    private LocalDate dateExpiration;
    private Fournisseur fournisseur;
    
    /**
     * Constructeur par défaut
     */
    public Medicament() {
    }
    
    /**
     * Constructeur avec tous les champs obligatoires
     * 
     * @param id Identifiant unique du médicament
     * @param nom Nom du médicament
     * @param categorie Catégorie thérapeutique
     * @param prix Prix unitaire
     * @param quantite Quantité en stock
     * @param dateExpiration Date de péremption
     * @param fournisseur Fournisseur du médicament
     */
    public Medicament(long id, String nom, String categorie, double prix, 
                      int quantite, LocalDate dateExpiration, Fournisseur fournisseur) {
        this.id = id;
        this.nom = nom;
        this.categorie = categorie;
        this.prix = prix;
        this.quantite = quantite;
        this.dateExpiration = dateExpiration;
        this.fournisseur = fournisseur;
    }
    
    /**
     * Constructeur sans fournisseur
     */
    public Medicament(long id, String nom, String categorie, double prix, 
                      int quantite, LocalDate dateExpiration) {
        this(id, nom, categorie, prix, quantite, dateExpiration, null);
    }
    
    // Getters et Setters
    
    public long getId() {
        return id;
    }
    
    public void setId(long id) {
        this.id = id;
    }
    
    public String getNom() {
        return nom;
    }
    
    public void setNom(String nom) {
        this.nom = nom;
    }
    
    public String getCategorie() {
        return categorie;
    }
    
    public void setCategorie(String categorie) {
        this.categorie = categorie;
    }
    
    public int getQuantite() {
        return quantite;
    }
    
    public void setQuantite(int quantite) {
        this.quantite = quantite;
    }
    
    public double getPrix() {
        return prix;
    }
    
    public void setPrix(double prix) {
        this.prix = prix;
    }
    
    public LocalDate getDateExpiration() {
        return dateExpiration;
    }
    
    public void setDateExpiration(LocalDate dateExpiration) {
        this.dateExpiration = dateExpiration;
    }
    
    public Fournisseur getFournisseur() {
        return fournisseur;
    }
    
    public void setFournisseur(Fournisseur fournisseur) {
        this.fournisseur = fournisseur;
    }
    
    /**
     * Vérifie si le médicament est expiré
     * 
     * @return true si la date d'expiration est dépassée
     */
    public boolean isExpired() {
        return dateExpiration != null && dateExpiration.isBefore(LocalDate.now());
    }
    
    /**
     * Vérifie si le médicament est proche de l'expiration
     * 
     * @param joursAlerte Nombre de jours avant expiration pour l'alerte
     * @return true si le médicament expire dans les jours spécifiés
     */
    public boolean isProcheExpiration(int joursAlerte) {
        if (dateExpiration == null) {
            return false;
        }
        LocalDate dateAlerte = LocalDate.now().plusDays(joursAlerte);
        return dateExpiration.isBefore(dateAlerte) && !isExpired();
    }
    
    /**
     * Vérifie si le stock est faible
     * 
     * @param seuil Seuil de stock minimum
     * @return true si la quantité est inférieure ou égale au seuil
     */
    public boolean isStockFaible(int seuil) {
        return quantite <= seuil;
    }
    
    /**
     * Vérifie si le médicament est en rupture de stock
     * 
     * @return true si la quantité est zéro
     */
    public boolean isRuptureStock() {
        return quantite == 0;
    }
    
    /**
     * Calcule le montant total en stock (prix * quantité)
     * 
     * @return Valeur totale du stock pour ce médicament
     */
    public double calculerValeurStock() {
        return prix * quantite;
    }
    
    /**
     * Réduit la quantité en stock
     * 
     * @param quantiteVendue Quantité à soustraire
     * @throws IllegalArgumentException si la quantité est insuffisante
     */
    public void reduireStock(int quantiteVendue) {
        if (quantiteVendue > quantite) {
            throw new IllegalArgumentException(
                "Stock insuffisant. Disponible: " + quantite + ", demandé: " + quantiteVendue
            );
        }
        this.quantite -= quantiteVendue;
    }
    
    /**
     * Augmente la quantité en stock (réapprovisionnement)
     * 
     * @param quantiteAjoutee Quantité à ajouter
     */
    public void augmenterStock(int quantiteAjoutee) {
        if (quantiteAjoutee < 0) {
            throw new IllegalArgumentException("La quantité à ajouter doit être positive");
        }
        this.quantite += quantiteAjoutee;
    }
    
    /**
     * Vérifie si le médicament est valide pour la vente
     * 
     * @return true si non expiré et en stock
     */
    public boolean estDisponiblePourVente() {
        return !isExpired() && quantite > 0;
    }
    
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Medicament that = (Medicament) o;
        return id == that.id;
    }
    
    @Override
    public int hashCode() {
        return Objects.hash(id);
    }
    
    @Override
    public String toString() {
        return "Medicament{" +
                "id=" + id +
                ", nom='" + nom + '\'' +
                ", categorie='" + categorie + '\'' +
                ", quantite=" + quantite +
                ", prix=" + prix +
                ", dateExpiration=" + dateExpiration +
                ", fournisseur=" + (fournisseur != null ? fournisseur.getNom() : "N/A") +
                ", expired=" + isExpired() +
                '}';
    }
}

