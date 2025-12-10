package com.pharmacie.service;

import com.pharmacie.model.Medicament;

import java.time.LocalDate;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * Service de surveillance automatique des dates d'expiration
 * Utilise un thread pour surveiller en continu les médicaments expirés
 * Implémente la synchronisation pour éviter les accès concurrents
 */
public class ExpirationMonitor implements Runnable {
    
    private StocksService stocksService;
    private AtomicBoolean actif;
    private Thread monitorThread;
    private int intervalSeconde;
    private final Object verrou = new Object();
    
    // Callbacks pour les alertes
    private ExpirationCallback callback;
    
    /**
     * Interface pour les callbacks d'alerte
     */
    public interface ExpirationCallback {
        void onMedicamentExpire(Medicament medicament);
        void onMedicamentProcheExpiration(Medicament medicament, long joursRestants);
    }
    
    /**
     * Constructeur avec intervalle par défaut (60 secondes)
     * 
     * @param stocksService Service de gestion des stocks
     */
    public ExpirationMonitor(StocksService stocksService) {
        this(stocksService, 60);
    }
    
    /**
     * Constructeur avec intervalle personnalisé
     * 
     * @param stocksService Service de gestion des stocks
     * @param intervalSeconde Intervalle de vérification en secondes
     */
    public ExpirationMonitor(StocksService stocksService, int intervalSeconde) {
        this.stocksService = stocksService;
        this.intervalSeconde = intervalSeconde;
        this.actif = new AtomicBoolean(false);
    }
    
    /**
     * Définit le callback pour les alertes
     * 
     * @param callback Callback à appeler lors des alertes
     */
    public void setCallback(ExpirationCallback callback) {
        synchronized (verrou) {
            this.callback = callback;
        }
    }
    
    /**
     * Démarre le monitoring dans un thread séparé
     */
    public void demarrer() {
        if (actif.get()) {
            System.out.println("Le monitoring est déjà actif");
            return;
        }
        
        actif.set(true);
        monitorThread = new Thread(this, "ExpirationMonitor-Thread");
        monitorThread.setDaemon(true); // Thread daemon pour ne pas bloquer l'arrêt de l'application
        monitorThread.start();
        
        System.out.println("Monitoring d'expiration démarré (intervalle: " + intervalSeconde + "s)");
    }
    
    /**
     * Arrête le monitoring
     */
    public void arreter() {
        if (!actif.get()) {
            System.out.println("Le monitoring n'est pas actif");
            return;
        }
        
        actif.set(false);
        
        if (monitorThread != null) {
            monitorThread.interrupt();
            try {
                monitorThread.join(5000); // Attendre max 5 secondes
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        }
        
        System.out.println("Monitoring d'expiration arrêté");
    }
    
    /**
     * Vérifie si le monitoring est actif
     * 
     * @return true si actif
     */
    public boolean estActif() {
        return actif.get();
    }
    
    /**
     * Méthode principale du thread
     * Vérifie périodiquement les expirations
     */
    @Override
    public void run() {
        while (actif.get()) {
            try {
                // Vérification synchronisée
                synchronized (verrou) {
                    verifierExpirations();
                }
                
                // Attendre l'intervalle spécifié
                Thread.sleep(intervalSeconde * 1000L);
                
            } catch (InterruptedException e) {
                // Le thread a été interrompu, sortir proprement
                Thread.currentThread().interrupt();
                break;
            } catch (Exception e) {
                System.err.println("Erreur dans le monitoring d'expiration: " + e.getMessage());
                e.printStackTrace();
            }
        }
        
        System.out.println("Thread de monitoring terminé");
    }
    
    /**
     * Vérifie les expirations et déclenche les alertes
     * Méthode synchronisée pour éviter les accès concurrents
     */
    private synchronized void verifierExpirations() {
        List<Medicament> medicaments = stocksService.obtenirTousLesMedicaments();
        LocalDate aujourd'hui = LocalDate.now();
        
        int compteurExpires = 0;
        int compteurProches = 0;
        
        for (Medicament medicament : medicaments) {
            // Vérifier si expiré
            if (medicament.isExpired()) {
                compteurExpires++;
                System.out.println("⚠ ALERTE: Médicament expiré - " + 
                    medicament.getNom() + " (Expiré le: " + medicament.getDateExpiration() + ")");
                
                // Callback si défini
                if (callback != null) {
                    callback.onMedicamentExpire(medicament);
                }
            }
            // Vérifier si proche de l'expiration (90 jours)
            else if (medicament.isProcheExpiration(90)) {
                compteurProches++;
                long joursRestants = java.time.temporal.ChronoUnit.DAYS.between(
                    aujourd'hui, 
                    medicament.getDateExpiration()
                );
                
                System.out.println("⚠ ATTENTION: Médicament proche expiration - " + 
                    medicament.getNom() + " (Expire dans " + joursRestants + " jours)");
                
                // Callback si défini
                if (callback != null) {
                    callback.onMedicamentProcheExpiration(medicament, joursRestants);
                }
            }
        }
        
        // Log du résultat
        if (compteurExpires > 0 || compteurProches > 0) {
            System.out.println("Vérification terminée - Expirés: " + compteurExpires + 
                ", Proches expiration: " + compteurProches);
        }
    }
    
    /**
     * Force une vérification immédiate (pour tests)
     */
    public void verifierMaintenant() {
        synchronized (verrou) {
            verifierExpirations();
        }
    }
    
    /**
     * Obtient la liste des médicaments expirés (accès synchronisé)
     * 
     * @return Liste des médicaments expirés
     */
    public synchronized List<Medicament> obtenirMedicamentsExpires() {
        return stocksService.obtenirMedicamentsExpires();
    }
    
    /**
     * Obtient la liste des médicaments proches de l'expiration (accès synchronisé)
     * 
     * @param joursAlerte Nombre de jours d'alerte
     * @return Liste des médicaments proches de l'expiration
     */
    public synchronized List<Medicament> obtenirMedicamentsProchesExpiration(int joursAlerte) {
        return stocksService.obtenirMedicamentsProchesExpiration(joursAlerte);
    }
    
    /**
     * Modifie l'intervalle de vérification
     * 
     * @param intervalSeconde Nouvel intervalle en secondes
     */
    public void setIntervalle(int intervalSeconde) {
        synchronized (verrou) {
            this.intervalSeconde = intervalSeconde;
            System.out.println("Intervalle de monitoring modifié: " + intervalSeconde + "s");
        }
    }
    
    /**
     * Obtient l'intervalle de vérification actuel
     * 
     * @return Intervalle en secondes
     */
    public int getIntervalle() {
        synchronized (verrou) {
            return intervalSeconde;
        }
    }
    
    /**
     * Compte le nombre total d'alertes actives
     * 
     * @return Nombre d'alertes (expirés + proches expiration)
     */
    public synchronized int compterAlertes() {
        int expires = stocksService.obtenirMedicamentsExpires().size();
        int proches = stocksService.obtenirMedicamentsProchesExpiration(90).size();
        return expires + proches;
    }
    
    /**
     * Génère un rapport d'expiration
     * 
     * @return Rapport sous forme de texte
     */
    public synchronized String genererRapport() {
        StringBuilder rapport = new StringBuilder();
        rapport.append("=== RAPPORT D'EXPIRATION ===\n");
        rapport.append("Date: ").append(LocalDate.now()).append("\n");
        rapport.append("Statut monitoring: ").append(actif.get() ? "ACTIF" : "INACTIF").append("\n\n");
        
        List<Medicament> expires = obtenirMedicamentsExpires();
        rapport.append("Médicaments expirés: ").append(expires.size()).append("\n");
        for (Medicament med : expires) {
            long joursDepuis = java.time.temporal.ChronoUnit.DAYS.between(
                med.getDateExpiration(), 
                LocalDate.now()
            );
            rapport.append("  - ").append(med.getNom())
                   .append(" (Expiré depuis ").append(joursDepuis).append(" jours)\n");
        }
        
        rapport.append("\n");
        List<Medicament> proches = obtenirMedicamentsProchesExpiration(90);
        rapport.append("Médicaments proches expiration (< 90 jours): ").append(proches.size()).append("\n");
        for (Medicament med : proches) {
            long joursRestants = java.time.temporal.ChronoUnit.DAYS.between(
                LocalDate.now(), 
                med.getDateExpiration()
            );
            rapport.append("  - ").append(med.getNom())
                   .append(" (Expire dans ").append(joursRestants).append(" jours)\n");
        }
        
        return rapport.toString();
    }
}

