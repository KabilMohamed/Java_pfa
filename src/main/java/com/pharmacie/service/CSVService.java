package com.pharmacie.service;

import com.pharmacie.model.Medicament;
import com.pharmacie.model.Fournisseur;

import java.io.*;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

/**
 * Service de gestion des fichiers CSV
 * Permet l'export et l'import du stock de médicaments
 */
public class CSVService {
    
    private static final String FICHIER_STOCK = "stock_pharmacie.csv";
    private static final String SEPARATEUR = ",";
    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ISO_LOCAL_DATE;
    
    private StocksService stocksService;
    private FournisseurService fournisseurService;
    
    /**
     * Constructeur
     */
    public CSVService() {
        this.stocksService = new StocksService();
        this.fournisseurService = new FournisseurService();
    }
    
    /**
     * Exporte le stock complet au format CSV
     * 
     * @param medicaments Liste des médicaments à exporter
     * @throws IOException Si erreur d'écriture
     */
    public void exporterStock(List<Medicament> medicaments) throws IOException {
        exporterStock(medicaments, FICHIER_STOCK);
    }
    
    /**
     * Exporte le stock vers un fichier spécifique
     * 
     * @param medicaments Liste des médicaments à exporter
     * @param nomFichier Nom du fichier de destination
     * @throws IOException Si erreur d'écriture
     */
    public void exporterStock(List<Medicament> medicaments, String nomFichier) throws IOException {
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(nomFichier))) {
            // En-tête
            writer.write("ID,Nom,Categorie,Prix,Quantite,DateExpiration,FournisseurID,FournisseurNom");
            writer.newLine();
            
            // Données
            for (Medicament med : medicaments) {
                StringBuilder ligne = new StringBuilder();
                ligne.append(med.getId()).append(SEPARATEUR);
                ligne.append(escaperCSV(med.getNom())).append(SEPARATEUR);
                ligne.append(escaperCSV(med.getCategorie())).append(SEPARATEUR);
                ligne.append(med.getPrix()).append(SEPARATEUR);
                ligne.append(med.getQuantite()).append(SEPARATEUR);
                ligne.append(med.getDateExpiration().format(DATE_FORMATTER)).append(SEPARATEUR);
                
                if (med.getFournisseur() != null) {
                    ligne.append(med.getFournisseur().getId()).append(SEPARATEUR);
                    ligne.append(escaperCSV(med.getFournisseur().getNom()));
                } else {
                    ligne.append("0").append(SEPARATEUR);
                    ligne.append("N/A");
                }
                
                writer.write(ligne.toString());
                writer.newLine();
            }
        }
    }
    
    /**
     * Importe le stock depuis un fichier CSV
     * 
     * @return Liste des médicaments importés
     * @throws IOException Si erreur de lecture
     */
    public List<Medicament> importerStock() throws IOException {
        return importerStock(FICHIER_STOCK);
    }
    
    /**
     * Importe le stock depuis un fichier spécifique
     * 
     * @param nomFichier Nom du fichier source
     * @return Liste des médicaments importés
     * @throws IOException Si erreur de lecture
     */
    public List<Medicament> importerStock(String nomFichier) throws IOException {
        List<Medicament> medicaments = new ArrayList<>();
        
        try (BufferedReader reader = new BufferedReader(new FileReader(nomFichier))) {
            // Ignorer l'en-tête
            String ligne = reader.readLine();
            
            // Lire les données
            while ((ligne = reader.readLine()) != null) {
                if (ligne.trim().isEmpty()) {
                    continue;
                }
                
                try {
                    Medicament med = parseLigneCSV(ligne);
                    if (med != null) {
                        medicaments.add(med);
                        
                        // Ajouter à la base si ce n'est pas déjà fait
                        if (stocksService.obtenirMedicamentParId(med.getId()) == null) {
                            stocksService.ajouterMedicament(med);
                        }
                    }
                } catch (Exception e) {
                    System.err.println("Erreur lors de l'import de la ligne: " + ligne);
                    System.err.println("Erreur: " + e.getMessage());
                }
            }
        }
        
        return medicaments;
    }
    
    /**
     * Parse une ligne CSV en objet Medicament
     * 
     * @param ligne Ligne CSV à parser
     * @return Medicament créé ou null si erreur
     */
    private Medicament parseLigneCSV(String ligne) {
        try {
            String[] champs = ligne.split(SEPARATEUR);
            
            if (champs.length < 7) {
                System.err.println("Ligne CSV incomplète: " + ligne);
                return null;
            }
            
            long id = Long.parseLong(champs[0].trim());
            String nom = champs[1].trim();
            String categorie = champs[2].trim();
            double prix = Double.parseDouble(champs[3].trim());
            int quantite = Integer.parseInt(champs[4].trim());
            LocalDate dateExpiration = LocalDate.parse(champs[5].trim(), DATE_FORMATTER);
            
            // Récupération du fournisseur
            Fournisseur fournisseur = null;
            if (champs.length >= 7) {
                long fournisseurId = Long.parseLong(champs[6].trim());
                if (fournisseurId > 0) {
                    fournisseur = fournisseurService.obtenirFournisseurParId(fournisseurId);
                    
                    // Si le fournisseur n'existe pas, créer un fournisseur par défaut
                    if (fournisseur == null && champs.length >= 8) {
                        String nomFournisseur = champs[7].trim();
                        fournisseur = new Fournisseur(
                            fournisseurId,
                            nomFournisseur,
                            "Adresse inconnue",
                            "0000000000",
                            nomFournisseur.toLowerCase().replace(" ", "") + "@email.com",
                            "Contact"
                        );
                        fournisseurService.ajouterFournisseur(fournisseur);
                    }
                }
            }
            
            return new Medicament(id, nom, categorie, prix, quantite, dateExpiration, fournisseur);
            
        } catch (Exception e) {
            System.err.println("Erreur de parsing: " + e.getMessage());
            return null;
        }
    }
    
    /**
     * Échappe les caractères spéciaux pour CSV
     * 
     * @param valeur Valeur à échapper
     * @return Valeur échappée
     */
    private String escaperCSV(String valeur) {
        if (valeur == null) {
            return "";
        }
        
        // Si la valeur contient des caractères spéciaux, l'entourer de guillemets
        if (valeur.contains(SEPARATEUR) || valeur.contains("\"") || valeur.contains("\n")) {
            return "\"" + valeur.replace("\"", "\"\"") + "\"";
        }
        
        return valeur;
    }
    
    /**
     * Exporte un rapport de stock en CSV avec statistiques
     * 
     * @throws IOException Si erreur d'écriture
     */
    public void exporterRapportStock() throws IOException {
        String nomFichier = "rapport_stock_" + LocalDate.now() + ".csv";
        
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(nomFichier))) {
            // En-tête avec informations supplémentaires
            writer.write("ID,Nom,Categorie,Prix,Quantite,ValeurStock,DateExpiration,Statut,Fournisseur");
            writer.newLine();
            
            List<Medicament> medicaments = stocksService.obtenirTousLesMedicaments();
            
            for (Medicament med : medicaments) {
                StringBuilder ligne = new StringBuilder();
                ligne.append(med.getId()).append(SEPARATEUR);
                ligne.append(escaperCSV(med.getNom())).append(SEPARATEUR);
                ligne.append(escaperCSV(med.getCategorie())).append(SEPARATEUR);
                ligne.append(med.getPrix()).append(SEPARATEUR);
                ligne.append(med.getQuantite()).append(SEPARATEUR);
                ligne.append(String.format("%.2f", med.calculerValeurStock())).append(SEPARATEUR);
                ligne.append(med.getDateExpiration().format(DATE_FORMATTER)).append(SEPARATEUR);
                
                // Statut
                String statut;
                if (med.isExpired()) {
                    statut = "EXPIRE";
                } else if (med.isRuptureStock()) {
                    statut = "RUPTURE";
                } else if (med.isStockFaible(10)) {
                    statut = "STOCK_FAIBLE";
                } else if (med.isProcheExpiration(90)) {
                    statut = "PROCHE_EXPIRATION";
                } else {
                    statut = "OK";
                }
                ligne.append(statut).append(SEPARATEUR);
                
                // Fournisseur
                if (med.getFournisseur() != null) {
                    ligne.append(escaperCSV(med.getFournisseur().getNom()));
                } else {
                    ligne.append("N/A");
                }
                
                writer.write(ligne.toString());
                writer.newLine();
            }
            
            // Ajouter des statistiques à la fin
            writer.newLine();
            writer.write("=== STATISTIQUES ===");
            writer.newLine();
            writer.write("Nombre total de medicaments," + medicaments.size());
            writer.newLine();
            writer.write("Valeur totale du stock," + String.format("%.2f DH", stocksService.calculerValeurTotaleStock()));
            writer.newLine();
            writer.write("Medicaments expires," + stocksService.obtenirMedicamentsExpires().size());
            writer.newLine();
            writer.write("Medicaments en rupture," + stocksService.obtenirMedicamentsRupture().size());
            writer.newLine();
            writer.write("Medicaments en stock faible," + stocksService.obtenirMedicamentsStockFaible(10).size());
        }
    }
    
    /**
     * Vérifie si le fichier de stock existe
     * 
     * @return true si le fichier existe
     */
    public boolean fichierStockExiste() {
        return new File(FICHIER_STOCK).exists();
    }
    
    /**
     * Crée une sauvegarde du fichier de stock
     * 
     * @throws IOException Si erreur lors de la sauvegarde
     */
    public void sauvegarderStock() throws IOException {
        String nomSauvegarde = "stock_backup_" + LocalDate.now() + ".csv";
        List<Medicament> medicaments = stocksService.obtenirTousLesMedicaments();
        exporterStock(medicaments, nomSauvegarde);
    }
}

