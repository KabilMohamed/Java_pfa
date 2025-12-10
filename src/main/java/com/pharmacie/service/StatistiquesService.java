package com.pharmacie.service;

import com.pharmacie.model.Vente;
import com.pharmacie.model.Medicament;

import java.time.LocalDate;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Service de génération de statistiques
 * Fournit des statistiques sur les ventes et le stock
 */
public class StatistiquesService {
    
    private VentesService ventesService;
    private StocksService stocksService;
    
    /**
     * Constructeur
     */
    public StatistiquesService() {
        this.ventesService = new VentesService();
        this.stocksService = new StocksService();
    }
    
    /**
     * Calcule le total des ventes du jour
     * 
     * @return Montant total des ventes d'aujourd'hui
     */
    public double calculerVentesJour() {
        return ventesService.calculerChiffreAffairesJour();
    }
    
    /**
     * Calcule le total des ventes du mois
     * 
     * @return Montant total des ventes du mois en cours
     */
    public double calculerVentesMois() {
        return ventesService.calculerChiffreAffairesMois();
    }
    
    /**
     * Obtient le nombre de ventes du jour
     * 
     * @return Nombre de ventes aujourd'hui
     */
    public int obtenirNombreVentesJour() {
        return ventesService.obtenirNombreVentesJour();
    }
    
    /**
     * Obtient le médicament le plus vendu (meilleure vente)
     * 
     * @return Nom du médicament le plus vendu
     */
    public String obtenirMeilleurVente() {
        Map<String, Integer> ventesParMedicament = new HashMap<>();
        
        for (Vente vente : ventesService.obtenirToutesLesVentes()) {
            String nom = vente.getMedicament().getNom();
            ventesParMedicament.put(nom, 
                ventesParMedicament.getOrDefault(nom, 0) + vente.getQuantite());
        }
        
        if (ventesParMedicament.isEmpty()) {
            return "Aucune vente";
        }
        
        return ventesParMedicament.entrySet().stream()
                .max(Map.Entry.comparingByValue())
                .map(Map.Entry::getKey)
                .orElse("N/A");
    }
    
    /**
     * Obtient les ventes par médicament (pour graphique)
     * 
     * @return Map avec nom du médicament et montant total vendu
     */
    public Map<String, Double> obtenirVentesParMedicament() {
        Map<String, Double> ventesParMedicament = new HashMap<>();
        
        for (Vente vente : ventesService.obtenirToutesLesVentes()) {
            String nom = vente.getMedicament().getNom();
            ventesParMedicament.put(nom, 
                ventesParMedicament.getOrDefault(nom, 0.0) + vente.getMontantTotal());
        }
        
        // Retourner les top 10
        return ventesParMedicament.entrySet().stream()
                .sorted(Map.Entry.<String, Double>comparingByValue().reversed())
                .limit(10)
                .collect(Collectors.toMap(
                    Map.Entry::getKey,
                    Map.Entry::getValue,
                    (e1, e2) -> e1,
                    LinkedHashMap::new
                ));
    }
    
    /**
     * Obtient les ventes par jour pour une période
     * 
     * @param debut Date de début
     * @param fin Date de fin
     * @return Map avec date et montant total
     */
    public Map<String, Double> obtenirVentesParJour(LocalDate debut, LocalDate fin) {
        Map<String, Double> ventesParJour = new LinkedHashMap<>();
        
        // Initialiser toutes les dates de la période
        LocalDate date = debut;
        while (!date.isAfter(fin)) {
            ventesParJour.put(date.toString(), 0.0);
            date = date.plusDays(1);
        }
        
        // Calculer les ventes par jour
        List<Vente> ventes = ventesService.obtenirVentesParPeriode(debut, fin);
        for (Vente vente : ventes) {
            String dateStr = vente.getDateSeulement().toString();
            ventesParJour.put(dateStr, 
                ventesParJour.getOrDefault(dateStr, 0.0) + vente.getMontantTotal());
        }
        
        return ventesParJour;
    }
    
    /**
     * Obtient les ventes par catégorie
     * 
     * @return Map avec catégorie et montant total
     */
    public Map<String, Double> obtenirVentesParCategorie() {
        Map<String, Double> ventesParCategorie = new HashMap<>();
        
        for (Vente vente : ventesService.obtenirToutesLesVentes()) {
            String categorie = vente.getMedicament().getCategorie();
            ventesParCategorie.put(categorie, 
                ventesParCategorie.getOrDefault(categorie, 0.0) + vente.getMontantTotal());
        }
        
        return ventesParCategorie;
    }
    
    /**
     * Calcule le taux de rotation du stock
     * 
     * @return Taux de rotation (ventes / stock moyen)
     */
    public double calculerTauxRotation() {
        double totalVentes = calculerVentesMois();
        double valeurStock = stocksService.calculerValeurTotaleStock();
        
        if (valeurStock == 0) {
            return 0;
        }
        
        return totalVentes / valeurStock;
    }
    
    /**
     * Obtient le top des médicaments les plus vendus
     * 
     * @param limite Nombre de médicaments à retourner
     * @return Liste des noms des médicaments les plus vendus
     */
    public List<String> obtenirTopMedicaments(int limite) {
        Map<String, Integer> ventesParMedicament = new HashMap<>();
        
        for (Vente vente : ventesService.obtenirToutesLesVentes()) {
            String nom = vente.getMedicament().getNom();
            ventesParMedicament.put(nom, 
                ventesParMedicament.getOrDefault(nom, 0) + vente.getQuantite());
        }
        
        return ventesParMedicament.entrySet().stream()
                .sorted(Map.Entry.<String, Integer>comparingByValue().reversed())
                .limit(limite)
                .map(Map.Entry::getKey)
                .collect(Collectors.toList());
    }
    
    /**
     * Calcule le montant moyen par vente
     * 
     * @return Montant moyen
     */
    public double calculerMontantMoyenVente() {
        return ventesService.calculerMontantMoyenVente();
    }
    
    /**
     * Obtient le nombre de médicaments par catégorie
     * 
     * @return Map avec catégorie et nombre de médicaments
     */
    public Map<String, Long> obtenirNombreMedicamentsParCategorie() {
        return stocksService.obtenirTousLesMedicaments().stream()
                .collect(Collectors.groupingBy(
                    Medicament::getCategorie,
                    Collectors.counting()
                ));
    }
    
    /**
     * Calcule le pourcentage de médicaments en stock faible
     * 
     * @param seuil Seuil de stock faible
     * @return Pourcentage
     */
    public double calculerPourcentageStockFaible(int seuil) {
        long total = stocksService.obtenirNombreMedicaments();
        if (total == 0) {
            return 0;
        }
        
        long stockFaible = stocksService.obtenirMedicamentsStockFaible(seuil).size();
        return (stockFaible * 100.0) / total;
    }
    
    /**
     * Calcule le pourcentage de médicaments expirés
     * 
     * @return Pourcentage
     */
    public double calculerPourcentageExpires() {
        long total = stocksService.obtenirNombreMedicaments();
        if (total == 0) {
            return 0;
        }
        
        long expires = stocksService.obtenirMedicamentsExpires().size();
        return (expires * 100.0) / total;
    }
    
    /**
     * Génère un rapport de statistiques complet
     * 
     * @return Rapport sous forme de texte
     */
    public String genererRapportComplet() {
        StringBuilder rapport = new StringBuilder();
        rapport.append("=== RAPPORT STATISTIQUES ===\n");
        rapport.append("Date: ").append(LocalDate.now()).append("\n\n");
        
        rapport.append("VENTES:\n");
        rapport.append("- Ventes du jour: ").append(String.format("%.2f DH", calculerVentesJour())).append("\n");
        rapport.append("- Ventes du mois: ").append(String.format("%.2f DH", calculerVentesMois())).append("\n");
        rapport.append("- Nombre de ventes (jour): ").append(obtenirNombreVentesJour()).append("\n");
        rapport.append("- Montant moyen par vente: ").append(String.format("%.2f DH", calculerMontantMoyenVente())).append("\n");
        rapport.append("- Meilleur vente: ").append(obtenirMeilleurVente()).append("\n\n");
        
        rapport.append("STOCK:\n");
        rapport.append("- Nombre de médicaments: ").append(stocksService.obtenirNombreMedicaments()).append("\n");
        rapport.append("- Valeur totale: ").append(String.format("%.2f DH", stocksService.calculerValeurTotaleStock())).append("\n");
        rapport.append("- Médicaments expirés: ").append(stocksService.obtenirMedicamentsExpires().size()).append("\n");
        rapport.append("- Stock faible: ").append(stocksService.obtenirMedicamentsStockFaible(10).size()).append("\n");
        rapport.append("- Taux de rotation: ").append(String.format("%.2f", calculerTauxRotation())).append("\n");
        
        return rapport.toString();
    }
    
    /**
     * Prédit les besoins de réapprovisionnement basés sur les ventes
     * 
     * @param joursAvenir Nombre de jours à prédire
     * @return Map avec médicament et quantité prédite nécessaire
     */
    public Map<String, Integer> predireBesoinsReapprovisionnement(int joursAvenir) {
        Map<String, Integer> besoins = new HashMap<>();
        
        // Calculer la vente moyenne par jour pour chaque médicament
        List<Vente> ventes = ventesService.obtenirVentesParPeriode(
            LocalDate.now().minusDays(30), 
            LocalDate.now()
        );
        
        Map<String, Double> venteMoyenneParJour = new HashMap<>();
        for (Vente vente : ventes) {
            String nom = vente.getMedicament().getNom();
            venteMoyenneParJour.put(nom, 
                venteMoyenneParJour.getOrDefault(nom, 0.0) + (vente.getQuantite() / 30.0));
        }
        
        // Prédire les besoins
        for (Medicament med : stocksService.obtenirTousLesMedicaments()) {
            double venteMoyenne = venteMoyenneParJour.getOrDefault(med.getNom(), 0.0);
            int besoinPredit = (int) Math.ceil(venteMoyenne * joursAvenir);
            int stockActuel = med.getQuantite();
            
            if (besoinPredit > stockActuel) {
                besoins.put(med.getNom(), besoinPredit - stockActuel);
            }
        }
        
        return besoins;
    }
}

