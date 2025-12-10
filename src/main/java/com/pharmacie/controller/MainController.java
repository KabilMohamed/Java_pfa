package com.pharmacie.controller;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.BorderPane;
import javafx.stage.Stage;
import com.pharmacie.service.*;

import java.io.IOException;
import java.util.Optional;

/**
 * Contrôleur principal de l'application
 * Gère la navigation entre les différentes vues et le tableau de bord
 */
public class MainController {

    @FXML
    private BorderPane mainBorderPane;

    @FXML
    private Label totalMedicamentsLabel;

    @FXML
    private Label stockFaibleLabel;

    @FXML
    private Label medicamentsExpiresLabel;

    @FXML
    private Label ventesJourLabel;

    @FXML
    private MenuItem menuMedicaments;

    @FXML
    private MenuItem menuVentes;

    @FXML
    private MenuItem menuFournisseurs;

    @FXML
    private MenuItem menuAlertes;

    @FXML
    private MenuItem menuExporterCSV;

    @FXML
    private MenuItem menuImporterCSV;

    @FXML
    private MenuItem menuQuitter;

    @FXML
    private Button btnMedicaments;

    @FXML
    private Button btnVentes;

    @FXML
    private Button btnFournisseurs;

    @FXML
    private Button btnAlertes;

    @FXML
    private Button btnRafraichir;

    private StocksService stocksService;
    private VentesService ventesService;
    private StatistiquesService statistiquesService;
    private CSVService csvService;

    /**
     * Initialisation du contrôleur
     */
    @FXML
    public void initialize() {
        // Initialisation des services
        stocksService = new StocksService();
        ventesService = new VentesService();
        statistiquesService = new StatistiquesService();
        csvService = new CSVService();

        // Mise à jour du tableau de bord
        rafraichirTableauDeBord();

        // Configuration des événements
        configureEventHandlers();
    }

    /**
     * Configuration des gestionnaires d'événements
     */
    private void configureEventHandlers() {
        if (btnMedicaments != null) {
            btnMedicaments.setOnAction(e -> afficherVueMedicaments());
        }
        if (btnVentes != null) {
            btnVentes.setOnAction(e -> afficherVueVentes());
        }
        if (btnFournisseurs != null) {
            btnFournisseurs.setOnAction(e -> afficherVueFournisseurs());
        }
        if (btnAlertes != null) {
            btnAlertes.setOnAction(e -> afficherVueAlertes());
        }
        if (btnRafraichir != null) {
            btnRafraichir.setOnAction(e -> rafraichirTableauDeBord());
        }
    }

    /**
     * Rafraîchissement des statistiques du tableau de bord
     */
    @FXML
    public void rafraichirTableauDeBord() {
        try {
            // Total des médicaments
            int totalMedicaments = stocksService.obtenirTousLesMedicaments().size();
            if (totalMedicamentsLabel != null) {
                totalMedicamentsLabel.setText(String.valueOf(totalMedicaments));
            }

            // Médicaments en stock faible
            int stockFaible = stocksService.obtenirMedicamentsStockFaible(10).size();
            if (stockFaibleLabel != null) {
                stockFaibleLabel.setText(String.valueOf(stockFaible));
                stockFaibleLabel.setStyle(stockFaible > 0 ? 
                    "-fx-text-fill: orange; -fx-font-weight: bold;" : 
                    "-fx-text-fill: green;");
            }

            // Médicaments expirés
            int medicamentsExpires = stocksService.obtenirMedicamentsExpires().size();
            if (medicamentsExpiresLabel != null) {
                medicamentsExpiresLabel.setText(String.valueOf(medicamentsExpires));
                medicamentsExpiresLabel.setStyle(medicamentsExpires > 0 ? 
                    "-fx-text-fill: red; -fx-font-weight: bold;" : 
                    "-fx-text-fill: green;");
            }

            // Ventes du jour
            double ventesJour = statistiquesService.calculerVentesJour();
            if (ventesJourLabel != null) {
                ventesJourLabel.setText(String.format("%.2f DH", ventesJour));
            }

            // Afficher une alerte si nécessaire
            if (medicamentsExpires > 0 || stockFaible > 0) {
                afficherAlertesAutomatiques(medicamentsExpires, stockFaible);
            }

        } catch (Exception e) {
            showErrorAlert("Erreur", "Erreur lors du rafraîchissement: " + e.getMessage());
        }
    }

    /**
     * Affichage automatique des alertes importantes
     */
    private void afficherAlertesAutomatiques(int expres, int stockFaible) {
        StringBuilder message = new StringBuilder();
        if (expres > 0) {
            message.append("⚠ ").append(expres).append(" médicament(s) expiré(s)\n");
        }
        if (stockFaible > 0) {
            message.append("⚠ ").append(stockFaible).append(" médicament(s) en stock faible\n");
        }

        if (message.length() > 0) {
            Alert alert = new Alert(Alert.AlertType.WARNING);
            alert.setTitle("Alertes Stock");
            alert.setHeaderText("Attention requise");
            alert.setContentText(message.toString());
            alert.show();
        }
    }

    /**
     * Navigation vers la vue des médicaments
     */
    @FXML
    public void afficherVueMedicaments() {
        chargerVue("/Fxml/MedicamentView.fxml", "Gestion des Médicaments");
    }

    /**
     * Navigation vers la vue des ventes
     */
    @FXML
    public void afficherVueVentes() {
        chargerVue("/Fxml/VenteView.fxml", "Gestion des Ventes");
    }

    /**
     * Navigation vers la vue des fournisseurs
     */
    @FXML
    public void afficherVueFournisseurs() {
        chargerVue("/Fxml/FournisseurView.fxml", "Gestion des Fournisseurs");
    }

    /**
     * Navigation vers la vue des alertes
     */
    @FXML
    public void afficherVueAlertes() {
        chargerVue("/Fxml/AlertView.fxml", "Alertes et Notifications");
    }

    /**
     * Chargement dynamique d'une vue dans le BorderPane central
     */
    private void chargerVue(String fxmlPath, String titre) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource(fxmlPath));
            Parent view = loader.load();
            
            if (mainBorderPane != null) {
                mainBorderPane.setCenter(view);
            } else {
                // Si pas de BorderPane, ouvrir dans une nouvelle fenêtre
                Stage stage = new Stage();
                stage.setTitle(titre);
                stage.setScene(new Scene(view));
                stage.show();
            }
        } catch (IOException e) {
            showErrorAlert("Erreur de Navigation", 
                "Impossible de charger la vue: " + e.getMessage());
            e.printStackTrace();
        }
    }

    /**
     * Export du stock au format CSV
     */
    @FXML
    public void exporterCSV() {
        try {
            csvService.exporterStock(stocksService.obtenirTousLesMedicaments());
            showInfoAlert("Export réussi", "Le stock a été exporté avec succès en CSV");
            
        } catch (Exception e) {
            showErrorAlert("Erreur d'Export", 
                "Erreur lors de l'export CSV: " + e.getMessage());
        }
    }

    /**
     * Import du stock depuis un fichier CSV
     */
    @FXML
    public void importerCSV() {
        try {
            Alert confirmation = new Alert(Alert.AlertType.CONFIRMATION);
            confirmation.setTitle("Confirmation");
            confirmation.setHeaderText("Importer un stock CSV");
            confirmation.setContentText("Cette opération va écraser le stock actuel. Continuer?");
            
            Optional<ButtonType> result = confirmation.showAndWait();
            if (result.isPresent() && result.get() == ButtonType.OK) {
                csvService.importerStock();
                rafraichirTableauDeBord();
                showInfoAlert("Import réussi", "Le stock a été importé avec succès");
            }
            
        } catch (Exception e) {
            showErrorAlert("Erreur d'Import", 
                "Erreur lors de l'import CSV: " + e.getMessage());
        }
    }

    /**
     * Quitter l'application
     */
    @FXML
    public void quitterApplication() {
        Alert confirmation = new Alert(Alert.AlertType.CONFIRMATION);
        confirmation.setTitle("Quitter");
        confirmation.setHeaderText("Quitter l'application");
        confirmation.setContentText("Êtes-vous sûr de vouloir quitter?");
        
        Optional<ButtonType> result = confirmation.showAndWait();
        if (result.isPresent() && result.get() == ButtonType.OK) {
            System.exit(0);
        }
    }

    /**
     * Affichage d'une alerte d'erreur
     */
    private void showErrorAlert(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    /**
     * Affichage d'une alerte d'information
     */
    private void showInfoAlert(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    /**
     * Affichage de la boîte de dialogue "À propos"
     */
    @FXML
    public void afficherAPropos() {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("À propos");
        alert.setHeaderText("Système de Gestion de Stock pour Pharmacie");
        alert.setContentText(
            "Version 1.0\n\n" +
            "Application complète de gestion de stock pharmaceutique\n" +
            "Développée en Java avec JavaFX\n\n" +
            "Fonctionnalités:\n" +
            "- Gestion des médicaments\n" +
            "- Gestion des ventes\n" +
            "- Gestion des fournisseurs\n" +
            "- Alertes automatiques\n" +
            "- Export/Import CSV\n" +
            "- Statistiques et rapports"
        );
        alert.showAndWait();
    }
}

