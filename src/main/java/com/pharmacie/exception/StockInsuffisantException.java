package com.pharmacie.exception;

import com.pharmacie.model.Medicament;

/**
 * Exception levée lorsqu'une vente ne peut pas être effectuée
 * car le stock disponible est insuffisant
 */
public class StockInsuffisantException extends Exception {
    
    private static final long serialVersionUID = 1L;
    
    private Medicament medicament;
    private int quantiteDemandee;
    private int quantiteDisponible;
    
    /**
     * Constructeur par défaut
     */
    public StockInsuffisantException() {
        super("Stock insuffisant pour effectuer cette opération");
    }
    
    /**
     * Constructeur avec message personnalisé
     * 
     * @param message Message d'erreur détaillé
     */
    public StockInsuffisantException(String message) {
        super(message);
    }
    
    /**
     * Constructeur avec médicament et quantités
     * 
     * @param medicament Le médicament concerné
     * @param quantiteDemandee Quantité demandée
     * @param quantiteDisponible Quantité disponible en stock
     */
    public StockInsuffisantException(Medicament medicament, int quantiteDemandee, int quantiteDisponible) {
        super(String.format("Stock insuffisant pour '%s'. Demandé: %d, Disponible: %d",
                medicament.getNom(), quantiteDemandee, quantiteDisponible));
        this.medicament = medicament;
        this.quantiteDemandee = quantiteDemandee;
        this.quantiteDisponible = quantiteDisponible;
    }
    
    /**
     * Constructeur avec nom de médicament et quantités
     * 
     * @param nomMedicament Nom du médicament
     * @param quantiteDemandee Quantité demandée
     * @param quantiteDisponible Quantité disponible
     */
    public StockInsuffisantException(String nomMedicament, int quantiteDemandee, int quantiteDisponible) {
        super(String.format("Stock insuffisant pour '%s'. Demandé: %d, Disponible: %d",
                nomMedicament, quantiteDemandee, quantiteDisponible));
        this.quantiteDemandee = quantiteDemandee;
        this.quantiteDisponible = quantiteDisponible;
    }
    
    /**
     * Constructeur avec message et cause
     * 
     * @param message Message d'erreur
     * @param cause Cause de l'exception
     */
    public StockInsuffisantException(String message, Throwable cause) {
        super(message, cause);
    }
    
    /**
     * Obtient le médicament concerné
     * 
     * @return Le médicament
     */
    public Medicament getMedicament() {
        return medicament;
    }
    
    /**
     * Obtient la quantité demandée
     * 
     * @return Quantité demandée
     */
    public int getQuantiteDemandee() {
        return quantiteDemandee;
    }
    
    /**
     * Obtient la quantité disponible
     * 
     * @return Quantité disponible
     */
    public int getQuantiteDisponible() {
        return quantiteDisponible;
    }
    
    /**
     * Calcule la quantité manquante
     * 
     * @return Quantité manquante pour satisfaire la demande
     */
    public int getQuantiteManquante() {
        return quantiteDemandee - quantiteDisponible;
    }
    
    /**
     * Vérifie si le stock est complètement épuisé
     * 
     * @return true si aucun stock disponible
     */
    public boolean isRuptureComplete() {
        return quantiteDisponible == 0;
    }
}

