package com.pharmacie.exception;

import com.pharmacie.model.Medicament;
import java.time.LocalDate;

/**
 * Exception levée lors d'une tentative de vente d'un médicament expiré
 * Empêche la vente de produits périmés
 */
public class MedicamentExpireException extends Exception {
    
    private static final long serialVersionUID = 1L;
    
    private Medicament medicament;
    private LocalDate dateExpiration;
    
    /**
     * Constructeur par défaut
     */
    public MedicamentExpireException() {
        super("Le médicament est expiré et ne peut pas être vendu");
    }
    
    /**
     * Constructeur avec message personnalisé
     * 
     * @param message Message d'erreur détaillé
     */
    public MedicamentExpireException(String message) {
        super(message);
    }
    
    /**
     * Constructeur avec médicament expiré
     * 
     * @param medicament Le médicament expiré
     */
    public MedicamentExpireException(Medicament medicament) {
        super(String.format("Le médicament '%s' est expiré depuis le %s et ne peut pas être vendu",
                medicament.getNom(), medicament.getDateExpiration()));
        this.medicament = medicament;
        this.dateExpiration = medicament.getDateExpiration();
    }
    
    /**
     * Constructeur avec médicament et message personnalisé
     * 
     * @param message Message d'erreur
     * @param medicament Le médicament expiré
     */
    public MedicamentExpireException(String message, Medicament medicament) {
        super(message);
        this.medicament = medicament;
        if (medicament != null) {
            this.dateExpiration = medicament.getDateExpiration();
        }
    }
    
    /**
     * Constructeur avec message et cause
     * 
     * @param message Message d'erreur
     * @param cause Cause de l'exception
     */
    public MedicamentExpireException(String message, Throwable cause) {
        super(message, cause);
    }
    
    /**
     * Obtient le médicament expiré concerné
     * 
     * @return Le médicament expiré
     */
    public Medicament getMedicament() {
        return medicament;
    }
    
    /**
     * Obtient la date d'expiration
     * 
     * @return La date d'expiration
     */
    public LocalDate getDateExpiration() {
        return dateExpiration;
    }
    
    /**
     * Calcule le nombre de jours depuis l'expiration
     * 
     * @return Nombre de jours depuis l'expiration
     */
    public long getJoursDepuisExpiration() {
        if (dateExpiration == null) {
            return 0;
        }
        return java.time.temporal.ChronoUnit.DAYS.between(dateExpiration, LocalDate.now());
    }
}

