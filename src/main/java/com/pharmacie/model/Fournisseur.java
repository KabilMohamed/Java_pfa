package com.pharmacie.model;

import java.io.Serializable;
import java.util.Objects;

/**
 * Classe représentant un fournisseur de médicaments
 * Contient les informations de contact et d'identification du fournisseur
 */
public class Fournisseur implements Serializable {
    
    private static final long serialVersionUID = 1L;
    
    private long id;
    private String nom;
    private String adresse;
    private String telephone;
    private String email;
    private String contact;
    private String notes;
    
    /**
     * Constructeur par défaut
     */
    public Fournisseur() {
    }
    
    /**
     * Constructeur avec tous les champs obligatoires
     * 
     * @param id Identifiant unique du fournisseur
     * @param nom Nom du fournisseur
     * @param adresse Adresse complète du fournisseur
     * @param telephone Numéro de téléphone
     * @param email Adresse email
     * @param contact Nom de la personne de contact
     */
    public Fournisseur(long id, String nom, String adresse, String telephone, 
                       String email, String contact) {
        this.id = id;
        this.nom = nom;
        this.adresse = adresse;
        this.telephone = telephone;
        this.email = email;
        this.contact = contact;
    }
    
    /**
     * Constructeur avec tous les champs incluant les notes
     */
    public Fournisseur(long id, String nom, String adresse, String telephone, 
                       String email, String contact, String notes) {
        this(id, nom, adresse, telephone, email, contact);
        this.notes = notes;
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
    
    public String getAdresse() {
        return adresse;
    }
    
    public void setAdresse(String adresse) {
        this.adresse = adresse;
    }
    
    public String getTelephone() {
        return telephone;
    }
    
    public void setTelephone(String telephone) {
        this.telephone = telephone;
    }
    
    public String getEmail() {
        return email;
    }
    
    public void setEmail(String email) {
        this.email = email;
    }
    
    public String getContact() {
        return contact;
    }
    
    public void setContact(String contact) {
        this.contact = contact;
    }
    
    public String getNotes() {
        return notes;
    }
    
    public void setNotes(String notes) {
        this.notes = notes;
    }
    
    /**
     * Vérifie si le fournisseur a toutes les informations obligatoires
     * 
     * @return true si toutes les informations sont présentes
     */
    public boolean estValide() {
        return nom != null && !nom.trim().isEmpty() &&
               adresse != null && !adresse.trim().isEmpty() &&
               telephone != null && !telephone.trim().isEmpty() &&
               email != null && !email.trim().isEmpty() &&
               contact != null && !contact.trim().isEmpty();
    }
    
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Fournisseur that = (Fournisseur) o;
        return id == that.id;
    }
    
    @Override
    public int hashCode() {
        return Objects.hash(id);
    }
    
    @Override
    public String toString() {
        return "Fournisseur{" +
                "id=" + id +
                ", nom='" + nom + '\'' +
                ", adresse='" + adresse + '\'' +
                ", telephone='" + telephone + '\'' +
                ", email='" + email + '\'' +
                ", contact='" + contact + '\'' +
                '}';
    }
}

