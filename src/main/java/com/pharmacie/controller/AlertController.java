package com.pharmacie.controller;

import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.VBox;
import com.pharmacie.model.Medicament;
import com.pharmacie.service.StocksService;
import com.pharmacie.service.ExpirationMonitor;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Contrôleur pour la gestion des alertes
 * Affiche les médicaments expirés, en stock faible, et proche de l'expiration
 */
public class AlertController {

    // Onglets
    @FXML
    private TabPane tabPaneAlertes;

    // === ONGLET MÉDICAMENTS EXPIRÉS ===
    @FXML
    private TableView<Medicament> tableExpires;

    @FXML
    private TableColumn<Medicament, String> colNomExpire;

    @FXML
    private TableColumn<Medicament, String> colCategorieExpire;

    @FXML
    private TableColumn<Medicament, Integer> colQuantiteExpire;

    @FXML
    private TableColumn<Medicament, String> colDateExpirationExpire;

    @FXML
    private TableColumn<Medicament, String> colJoursExpires;

    @FXML
    private Label lblTotalExpires;

    @FXML
    private Button btnSupprimerExpires;

    // === ONGLET STOCK FAIBLE ===
    @FXML
    private TableView<Medicament> tableStockFaible;

    @FXML
    private TableColumn<Medicament, String> colNomStock;

    @FXML
    private TableColumn<Medicament, String> colCategorieStock;

    @FXML
    private TableColumn<Medicament, Integer> colQuantiteStock;

    @FXML
    private TableColumn<Medicament, Double> colPrixStock;

    @FXML
    private TableColumn<Medicament, String> colFournisseurStock;

    @FXML
    private Label lblTotalStockFaible;

    @FXML
    private Spinner<Integer> spinnerSeuilStock;

    @FXML
    private Button btnReapprovisionner;

    // === ONGLET PROCHE EXPIRATION ===
    @FXML
    private TableView<Medicament> tableProcheExpiration;

    @FXML
    private TableColumn<Medicament, String> colNomProche;

    @FXML
    private TableColumn<Medicament, String> colCategorieProche;

    @FXML
    private TableColumn<Medicament, Integer> colQuantiteProche;

    @FXML
    private TableColumn<Medicament, String> colDateExpirationProche;

    @FXML
    private TableColumn<Medicament, String> colJoursRestants;

    @FXML
    private Label lblTotalProcheExpiration;

    @FXML
    private Spinner<Integer> spinnerJoursAlerte;

    // Statistiques globales
    @FXML
    private Label lblStatutGlobal;

    @FXML
    private VBox vboxAlertes;

    @FXML
    private Button btnRafraichir;

    @FXML
    private Button btnDemarrerMonitoring;

    @FXML
    private Button btnArreterMonitoring;

    @FXML
    private Label lblMonitoringStatut;

    // Services
    private StocksService stocksService;
    private ExpirationMonitor expirationMonitor;
    private ObservableList<Medicament> medicamentsExpiresList;
    private ObservableList<Medicament> medicamentsStockFaibleList;
    private ObservableList<Medicament> medicamentsProchesExpirationList;

    /**
     * Initialisation du contrôleur
     */
    @FXML
    public void initialize() {
        // Initialisation des services
        stocksService = new StocksService();
        expirationMonitor = new ExpirationMonitor(stocksService);

        medicamentsExpiresList = FXCollections.observableArrayList();
        medicamentsStockFaibleList = FXCollections.observableArrayList();
        medicamentsProchesExpirationList = FXCollections.observableArrayList();

        // Configuration des tables
        configurerTableExpires();
        configurerTableStockFaible();
        configurerTableProchesExpiration();

        // Configuration des spinners
        configurerSpinners();

        // Chargement des données
        rafraichirToutesLesAlertes();

        // Démarrage automatique du monitoring
        demarrerMonitoring();
    }

    /**
     * Configuration de la table des médicaments expirés
     */
    private void configurerTableExpires() {
        colNomExpire.setCellValueFactory(new PropertyValueFactory<>("nom"));
        colCategorieExpire.setCellValueFactory(new PropertyValueFactory<>("categorie"));
        colQuantiteExpire.setCellValueFactory(new PropertyValueFactory<>("quantite"));
        
        colDateExpirationExpire.setCellValueFactory(cellData -> 
            new SimpleStringProperty(cellData.getValue().getDateExpiration().toString()));
        
        colJoursExpires.setCellValueFactory(cellData -> {
            long jours = ChronoUnit.DAYS.between(
                cellData.getValue().getDateExpiration(), 
                LocalDate.now()
            );
            return new SimpleStringProperty(jours + " jour(s)");
        });

        // Style pour mettre en évidence
        tableExpires.setRowFactory(tv -> {
            TableRow<Medicament> row = new TableRow<>();
            row.setStyle("-fx-background-color: #ffcccc;");
            return row;
        });

        tableExpires.setItems(medicamentsExpiresList);
    }

    /**
     * Configuration de la table du stock faible
     */
    private void configurerTableStockFaible() {
        colNomStock.setCellValueFactory(new PropertyValueFactory<>("nom"));
        colCategorieStock.setCellValueFactory(new PropertyValueFactory<>("categorie"));
        colQuantiteStock.setCellValueFactory(new PropertyValueFactory<>("quantite"));
        colPrixStock.setCellValueFactory(new PropertyValueFactory<>("prix"));
        
        colFournisseurStock.setCellValueFactory(cellData -> {
            String fournisseur = cellData.getValue().getFournisseur() != null ? 
                cellData.getValue().getFournisseur().getNom() : "N/A";
            return new SimpleStringProperty(fournisseur);
        });

        // Coloration selon la quantité
        colQuantiteStock.setCellFactory(column -> new TableCell<Medicament, Integer>() {
            @Override
            protected void updateItem(Integer item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText(null);
                    setStyle("");
                } else {
                    setText(item.toString());
                    if (item == 0) {
                        setStyle("-fx-background-color: #ff4444; -fx-text-fill: white; -fx-font-weight: bold;");
                    } else if (item < 5) {
                        setStyle("-fx-background-color: #ffaa44; -fx-text-fill: white; -fx-font-weight: bold;");
                    } else {
                        setStyle("-fx-background-color: #ffff99;");
                    }
                }
            }
        });

        tableStockFaible.setItems(medicamentsStockFaibleList);
    }

    /**
     * Configuration de la table des médicaments proches de l'expiration
     */
    private void configurerTableProchesExpiration() {
        colNomProche.setCellValueFactory(new PropertyValueFactory<>("nom"));
        colCategorieProche.setCellValueFactory(new PropertyValueFactory<>("categorie"));
        colQuantiteProche.setCellValueFactory(new PropertyValueFactory<>("quantite"));
        
        colDateExpirationProche.setCellValueFactory(cellData -> 
            new SimpleStringProperty(cellData.getValue().getDateExpiration().toString()));
        
        colJoursRestants.setCellValueFactory(cellData -> {
            long jours = ChronoUnit.DAYS.between(
                LocalDate.now(),
                cellData.getValue().getDateExpiration()
            );
            return new SimpleStringProperty(jours + " jour(s)");
        });

        // Coloration selon les jours restants
        colJoursRestants.setCellFactory(column -> new TableCell<Medicament, String>() {
            @Override
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText(null);
                    setStyle("");
                } else {
                    setText(item);
                    int jours = Integer.parseInt(item.split(" ")[0]);
                    if (jours <= 30) {
                        setStyle("-fx-background-color: #ffcccc; -fx-font-weight: bold;");
                    } else if (jours <= 60) {
                        setStyle("-fx-background-color: #fff3cd;");
                    } else {
                        setStyle("");
                    }
                }
            }
        });

        tableProchesExpiration.setItems(medicamentsProchesExpirationList);
    }

    /**
     * Configuration des spinners
     */
    private void configurerSpinners() {
        // Spinner pour le seuil de stock
        if (spinnerSeuilStock != null) {
            SpinnerValueFactory<Integer> valueFactoryStock = 
                new SpinnerValueFactory.IntegerSpinnerValueFactory(1, 100, 10);
            spinnerSeuilStock.setValueFactory(valueFactoryStock);
            
            spinnerSeuilStock.valueProperty().addListener((obs, oldValue, newValue) -> {
                chargerMedicamentsStockFaible();
            });
        }

        // Spinner pour les jours d'alerte d'expiration
        if (spinnerJoursAlerte != null) {
            SpinnerValueFactory<Integer> valueFactoryJours = 
                new SpinnerValueFactory.IntegerSpinnerValueFactory(1, 365, 90);
            spinnerJoursAlerte.setValueFactory(valueFactoryJours);
            
            spinnerJoursAlerte.valueProperty().addListener((obs, oldValue, newValue) -> {
                chargerMedicamentsProchesExpiration();
            });
        }
    }

    /**
     * Rafraîchissement de toutes les alertes
     */
    @FXML
    public void rafraichirToutesLesAlertes() {
        chargerMedicamentsExpires();
        chargerMedicamentsStockFaible();
        chargerMedicamentsProchesExpiration();
        mettreAJourStatutGlobal();
    }

    /**
     * Chargement des médicaments expirés
     */
    private void chargerMedicamentsExpires() {
        try {
            List<Medicament> expires = stocksService.obtenirMedicamentsExpires();
            medicamentsExpiresList.clear();
            medicamentsExpiresList.addAll(expires);

            if (lblTotalExpires != null) {
                lblTotalExpires.setText(String.valueOf(expires.size()));
                lblTotalExpires.setStyle("-fx-text-fill: red; -fx-font-size: 24; -fx-font-weight: bold;");
            }

        } catch (Exception e) {
            showErrorAlert("Erreur", "Impossible de charger les médicaments expirés: " + e.getMessage());
        }
    }

    /**
     * Chargement des médicaments en stock faible
     */
    private void chargerMedicamentsStockFaible() {
        try {
            int seuil = spinnerSeuilStock != null ? spinnerSeuilStock.getValue() : 10;
            List<Medicament> stockFaible = stocksService.obtenirMedicamentsStockFaible(seuil);
            medicamentsStockFaibleList.clear();
            medicamentsStockFaibleList.addAll(stockFaible);

            if (lblTotalStockFaible != null) {
                lblTotalStockFaible.setText(String.valueOf(stockFaible.size()));
                lblTotalStockFaible.setStyle("-fx-text-fill: orange; -fx-font-size: 24; -fx-font-weight: bold;");
            }

        } catch (Exception e) {
            showErrorAlert("Erreur", "Impossible de charger le stock faible: " + e.getMessage());
        }
    }

    /**
     * Chargement des médicaments proches de l'expiration
     */
    private void chargerMedicamentsProchesExpiration() {
        try {
            int joursAlerte = spinnerJoursAlerte != null ? spinnerJoursAlerte.getValue() : 90;
            List<Medicament> proches = stocksService.obtenirMedicamentsProchesExpiration(joursAlerte);
            medicamentsProchesExpirationList.clear();
            medicamentsProchesExpirationList.addAll(proches);

            if (lblTotalProcheExpiration != null) {
                lblTotalProcheExpiration.setText(String.valueOf(proches.size()));
                lblTotalProcheExpiration.setStyle("-fx-text-fill: darkorange; -fx-font-size: 24; -fx-font-weight: bold;");
            }

        } catch (Exception e) {
            showErrorAlert("Erreur", "Impossible de charger les médicaments proches de l'expiration: " + e.getMessage());
        }
    }

    /**
     * Mise à jour du statut global
     */
    private void mettreAJourStatutGlobal() {
        int totalAlertes = medicamentsExpiresList.size() + 
                          medicamentsStockFaibleList.size() + 
                          medicamentsProchesExpirationList.size();

        if (lblStatutGlobal != null) {
            if (totalAlertes == 0) {
                lblStatutGlobal.setText("✓ Aucune alerte - Tout est en ordre");
                lblStatutGlobal.setStyle("-fx-text-fill: green; -fx-font-size: 16; -fx-font-weight: bold;");
            } else {
                lblStatutGlobal.setText("⚠ " + totalAlertes + " alerte(s) nécessitant votre attention");
                lblStatutGlobal.setStyle("-fx-text-fill: red; -fx-font-size: 16; -fx-font-weight: bold;");
            }
        }
    }

    /**
     * Suppression des médicaments expirés sélectionnés
     */
    @FXML
    public void supprimerMedicamentsExpires() {
        Medicament selected = tableExpires.getSelectionModel().getSelectedItem();
        if (selected == null) {
            showErrorAlert("Aucune sélection", "Veuillez sélectionner un médicament à supprimer");
            return;
        }

        Alert confirmation = new Alert(Alert.AlertType.CONFIRMATION);
        confirmation.setTitle("Confirmation");
        confirmation.setHeaderText("Supprimer le médicament expiré");
        confirmation.setContentText("Confirmer la suppression de: " + selected.getNom() + " ?");

        confirmation.showAndWait().ifPresent(response -> {
            if (response == ButtonType.OK) {
                try {
                    stocksService.supprimerMedicament(selected.getId());
                    rafraichirToutesLesAlertes();
                    showInfoAlert("Succès", "Médicament expiré supprimé avec succès");
                } catch (Exception e) {
                    showErrorAlert("Erreur", "Erreur lors de la suppression: " + e.getMessage());
                }
            }
        });
    }

    /**
     * Suppression de tous les médicaments expirés
     */
    @FXML
    public void supprimerTousMedicamentsExpires() {
        if (medicamentsExpiresList.isEmpty()) {
            showInfoAlert("Information", "Aucun médicament expiré à supprimer");
            return;
        }

        Alert confirmation = new Alert(Alert.AlertType.CONFIRMATION);
        confirmation.setTitle("Confirmation");
        confirmation.setHeaderText("Supprimer tous les médicaments expirés");
        confirmation.setContentText("Confirmer la suppression de " + medicamentsExpiresList.size() + 
            " médicament(s) expiré(s) ?");

        confirmation.showAndWait().ifPresent(response -> {
            if (response == ButtonType.OK) {
                try {
                    int count = 0;
                    for (Medicament med : medicamentsExpiresList) {
                        stocksService.supprimerMedicament(med.getId());
                        count++;
                    }
                    rafraichirToutesLesAlertes();
                    showInfoAlert("Succès", count + " médicament(s) expiré(s) supprimé(s)");
                } catch (Exception e) {
                    showErrorAlert("Erreur", "Erreur lors de la suppression: " + e.getMessage());
                }
            }
        });
    }

    /**
     * Réapprovisionnement des médicaments en stock faible
     */
    @FXML
    public void reapprovisionner() {
        Medicament selected = tableStockFaible.getSelectionModel().getSelectedItem();
        if (selected == null) {
            showErrorAlert("Aucune sélection", "Veuillez sélectionner un médicament à réapprovisionner");
            return;
        }

        // Dialogue de saisie de la quantité
        TextInputDialog dialog = new TextInputDialog("50");
        dialog.setTitle("Réapprovisionnement");
        dialog.setHeaderText("Réapprovisionner: " + selected.getNom());
        dialog.setContentText("Quantité à ajouter:");

        dialog.showAndWait().ifPresent(quantiteStr -> {
            try {
                int quantite = Integer.parseInt(quantiteStr);
                if (quantite <= 0) {
                    showErrorAlert("Erreur", "La quantité doit être positive");
                    return;
                }

                selected.setQuantite(selected.getQuantite() + quantite);
                stocksService.modifierMedicament(selected);
                rafraichirToutesLesAlertes();
                
                showInfoAlert("Succès", 
                    "Réapprovisionnement effectué\nNouveau stock: " + selected.getQuantite());

            } catch (NumberFormatException e) {
                showErrorAlert("Erreur", "Veuillez saisir un nombre valide");
            } catch (Exception e) {
                showErrorAlert("Erreur", "Erreur lors du réapprovisionnement: " + e.getMessage());
            }
        });
    }

    /**
     * Démarrage du monitoring automatique
     */
    @FXML
    public void demarrerMonitoring() {
        try {
            expirationMonitor.demarrer();
            
            if (btnDemarrerMonitoring != null) {
                btnDemarrerMonitoring.setDisable(true);
            }
            if (btnArreterMonitoring != null) {
                btnArreterMonitoring.setDisable(false);
            }
            if (lblMonitoringStatut != null) {
                lblMonitoringStatut.setText("✓ Monitoring actif");
                lblMonitoringStatut.setStyle("-fx-text-fill: green; -fx-font-weight: bold;");
            }

            // Rafraîchissement automatique toutes les 5 secondes
            new Thread(() -> {
                while (expirationMonitor.estActif()) {
                    try {
                        Thread.sleep(5000);
                        javafx.application.Platform.runLater(this::rafraichirToutesLesAlertes);
                    } catch (InterruptedException e) {
                        break;
                    }
                }
            }).start();

        } catch (Exception e) {
            showErrorAlert("Erreur", "Impossible de démarrer le monitoring: " + e.getMessage());
        }
    }

    /**
     * Arrêt du monitoring automatique
     */
    @FXML
    public void arreterMonitoring() {
        try {
            expirationMonitor.arreter();
            
            if (btnDemarrerMonitoring != null) {
                btnDemarrerMonitoring.setDisable(false);
            }
            if (btnArreterMonitoring != null) {
                btnArreterMonitoring.setDisable(true);
            }
            if (lblMonitoringStatut != null) {
                lblMonitoringStatut.setText("○ Monitoring arrêté");
                lblMonitoringStatut.setStyle("-fx-text-fill: gray;");
            }

        } catch (Exception e) {
            showErrorAlert("Erreur", "Impossible d'arrêter le monitoring: " + e.getMessage());
        }
    }

    /**
     * Export du rapport d'alertes
     */
    @FXML
    public void exporterRapportAlertes() {
        try {
            StringBuilder rapport = new StringBuilder();
            rapport.append("=== RAPPORT D'ALERTES ===\n");
            rapport.append("Date: ").append(LocalDate.now()).append("\n\n");

            rapport.append("MÉDICAMENTS EXPIRÉS: ").append(medicamentsExpiresList.size()).append("\n");
            for (Medicament med : medicamentsExpiresList) {
                rapport.append("- ").append(med.getNom())
                       .append(" (Expiré le: ").append(med.getDateExpiration()).append(")\n");
            }

            rapport.append("\nSTOCK FAIBLE: ").append(medicamentsStockFaibleList.size()).append("\n");
            for (Medicament med : medicamentsStockFaibleList) {
                rapport.append("- ").append(med.getNom())
                       .append(" (Stock: ").append(med.getQuantite()).append(")\n");
            }

            rapport.append("\nPROCHES EXPIRATION: ").append(medicamentsProchesExpirationList.size()).append("\n");
            for (Medicament med : medicamentsProchesExpirationList) {
                long jours = ChronoUnit.DAYS.between(LocalDate.now(), med.getDateExpiration());
                rapport.append("- ").append(med.getNom())
                       .append(" (Expire dans ").append(jours).append(" jours)\n");
            }

            // Sauvegarde du rapport
            java.io.FileWriter writer = new java.io.FileWriter("rapport_alertes_" + LocalDate.now() + ".txt");
            writer.write(rapport.toString());
            writer.close();

            showInfoAlert("Export réussi", "Le rapport d'alertes a été exporté avec succès");

        } catch (Exception e) {
            showErrorAlert("Erreur d'Export", "Erreur lors de l'export: " + e.getMessage());
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
}

