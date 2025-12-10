package com.pharmacie.exception;

/**
 * Exception levée lorsque des données invalides sont saisies
 * Utilisée pour la validation des formulaires et des entrées utilisateur
 */
public class DonneeInvalideException extends Exception {
    
    private static final long serialVersionUID = 1L;
    
    /**
     * Constructeur par défaut
     */
    public DonneeInvalideException() {
        super("Les données saisies sont invalides");
    }
    
    /**
     * Constructeur avec message personnalisé
     * 
     * @param message Message d'erreur détaillé
     */
    public DonneeInvalideException(String message) {
        super(message);
    }
    
    /**
     * Constructeur avec message et cause
     * 
     * @param message Message d'erreur
     * @param cause Cause de l'exception
     */
    public DonneeInvalideException(String message, Throwable cause) {
        super(message, cause);
    }
    
    /**
     * Constructeur avec cause uniquement
     * 
     * @param cause Cause de l'exception
     */
    public DonneeInvalideException(Throwable cause) {
        super("Les données saisies sont invalides", cause);
    }
}

