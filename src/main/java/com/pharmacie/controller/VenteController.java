package com.pharmacie.controller;

import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.chart.PieChart;
import javafx.scene.chart.LineChart;
import javafx.scene.chart.XYChart;
import com.pharmacie.model.Medicament;
import com.pharmacie.model.Vente;
import com.pharmacie.service.StocksService;
import com.pharmacie.service.VentesService;
import com.pharmacie.service.StatistiquesService;
import com.pharmacie.exception.StockInsuffisantException;
import com.pharmacie.exception.MedicamentExpireException;
import com.pharmacie.exception.DonneeInvalideException;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

/**
 * Contrôleur pour la gestion des ventes
 * Permet d'enregistrer des ventes et consulter les statistiques
 */
public class VenteController {

    // TableView des ventes
    @FXML
    private TableView<Vente> tableVentes;

    @FXML
    private TableColumn<Vente, Integer> colIdVente;

    @FXML
    private TableColumn<Vente, String> colMedicament;

    @FXML
    private TableColumn<Vente, Integer> colQuantiteVendue;

    @FXML
    private TableColumn<Vente, Double> colPrixUnitaire;

    @FXML
    private TableColumn<Vente, Double> colMontantTotal;

    @FXML
    private TableColumn<Vente, String> colDateVente;

    // Formulaire de vente
    @FXML
    private ComboBox<Medicament> comboMedicament;

    @FXML
    private TextField txtQuantite;

    @FXML
    private Label lblPrixUnitaire;

    @FXML
    private Label lblStockDisponible;

    @FXML
    private Label lblMontantTotal;

    @FXML
    private DatePicker dateVente;

    @FXML
    private Button btnEnregistrerVente;

    @FXML
    private Button btnReinitialiser;

    // Statistiques
    @FXML
    private Label lblTotalVentesJour;

    @FXML
    private Label lblTotalVentesMois;

    @FXML
    private Label lblNombreVentesJour;

    @FXML
    private Label lblMeilleurVente;

    @FXML
    private PieChart pieChartVentes;

    @FXML
    private LineChart<String, Number> lineChartVentes;

    @FXML
    private DatePicker dateDebut;

    @FXML
    private DatePicker dateFin;

    @FXML
    private Button btnFiltrer;

    @FXML
    private Label lblStatut;

    // Services
    private VentesService ventesService;
    private StocksService stocksService;
    private StatistiquesService statistiquesService;
    private ObservableList<Vente> ventesList;

    /**
     * Initialisation du contrôleur
     */
    @FXML
    public void initialize() {
        // Initialisation des services
        ventesService = new VentesService();
        stocksService = new StocksService();
        statistiquesService = new StatistiquesService();
        ventesList = FXCollections.observableArrayList();

        // Configuration de la table
        configurerTableView();

        // Chargement des données
        chargerVentes();
        chargerMedicaments();
        chargerStatistiques();

        // Configuration des événements
        configurerEvenements();

        // Date par défaut
        if (dateVente != null) {
            dateVente.setValue(LocalDate.now());
        }
        if (dateDebut != null) {
            dateDebut.setValue(LocalDate.now().minusMonths(1));
        }
        if (dateFin != null) {
            dateFin.setValue(LocalDate.now());
        }
    }

    /**
     * Configuration de la TableView
     */
    private void configurerTableView() {
        colIdVente.setCellValueFactory(new PropertyValueFactory<>("id"));
        
        colMedicament.setCellValueFactory(cellData -> 
            new SimpleStringProperty(cellData.getValue().getMedicament().getNom()));
        
        colQuantiteVendue.setCellValueFactory(new PropertyValueFactory<>("quantite"));
        colPrixUnitaire.setCellValueFactory(new PropertyValueFactory<>("prixUnitaire"));
        colMontantTotal.setCellValueFactory(new PropertyValueFactory<>("montantTotal"));
        
        colDateVente.setCellValueFactory(cellData -> 
            new SimpleStringProperty(cellData.getValue().getDateVente().toString()));

        // Formatage des colonnes monétaires
        colPrixUnitaire.setCellFactory(column -> new TableCell<Vente, Double>() {
            @Override
            protected void updateItem(Double item, boolean empty) {
                super.updateItem(item, empty);
                setText(empty || item == null ? null : String.format("%.2f DH", item));
            }
        });

        colMontantTotal.setCellFactory(column -> new TableCell<Vente, Double>() {
            @Override
            protected void updateItem(Double item, boolean empty) {
                super.updateItem(item, empty);
                setText(empty || item == null ? null : String.format("%.2f DH", item));
            }
        });

        tableVentes.setItems(ventesList);
    }

    /**
     * Configuration des événements
     */
    private void configurerEvenements() {
        // Sélection de médicament
        if (comboMedicament != null) {
            comboMedicament.valueProperty().addListener((observable, oldValue, newValue) -> {
                if (newValue != null) {
                    lblPrixUnitaire.setText(String.format("%.2f DH", newValue.getPrix()));
                    lblStockDisponible.setText(String.valueOf(newValue.getQuantite()));
                    
                    // Vérification de l'expiration
                    if (newValue.getDateExpiration().isBefore(LocalDate.now())) {
                        lblStockDisponible.setStyle("-fx-text-fill: red; -fx-font-weight: bold;");
                        showWarningAlert("Médicament Expiré", 
                            "Attention: Ce médicament est expiré depuis le " + 
                            newValue.getDateExpiration());
                    } else if (newValue.getQuantite() == 0) {
                        lblStockDisponible.setStyle("-fx-text-fill: red; -fx-font-weight: bold;");
                    } else if (newValue.getQuantite() < 10) {
                        lblStockDisponible.setStyle("-fx-text-fill: orange; -fx-font-weight: bold;");
                    } else {
                        lblStockDisponible.setStyle("-fx-text-fill: green;");
                    }
                    
                    calculerMontantTotal();
                }
            });
        }

        // Calcul automatique du montant
        if (txtQuantite != null) {
            txtQuantite.textProperty().addListener((observable, oldValue, newValue) -> {
                calculerMontantTotal();
            });
        }
    }

    /**
     * Calcul du montant total de la vente
     */
    private void calculerMontantTotal() {
        try {
            if (comboMedicament.getValue() != null && !txtQuantite.getText().isEmpty()) {
                int quantite = Integer.parseInt(txtQuantite.getText());
                double prix = comboMedicament.getValue().getPrix();
                double total = quantite * prix;
                lblMontantTotal.setText(String.format("%.2f DH", total));
                lblMontantTotal.setStyle("-fx-font-size: 18; -fx-font-weight: bold; -fx-text-fill: green;");
            } else {
                lblMontantTotal.setText("0.00 DH");
            }
        } catch (NumberFormatException e) {
            lblMontantTotal.setText("0.00 DH");
        }
    }

    /**
     * Chargement de toutes les ventes
     */
    private void chargerVentes() {
        try {
            List<Vente> ventes = ventesService.obtenirToutesLesVentes();
            ventesList.clear();
            ventesList.addAll(ventes);
            
            if (lblStatut != null) {
                lblStatut.setText(ventes.size() + " vente(s) enregistrée(s)");
                lblStatut.setStyle("-fx-text-fill: green;");
            }
        } catch (Exception e) {
            showErrorAlert("Erreur", "Impossible de charger les ventes: " + e.getMessage());
        }
    }

    /**
     * Chargement des médicaments disponibles
     */
    private void chargerMedicaments() {
        try {
            List<Medicament> medicaments = stocksService.obtenirTousLesMedicaments();
            comboMedicament.setItems(FXCollections.observableArrayList(medicaments));
            
            // Affichage personnalisé
            comboMedicament.setCellFactory(param -> new ListCell<Medicament>() {
                @Override
                protected void updateItem(Medicament item, boolean empty) {
                    super.updateItem(item, empty);
                    if (empty || item == null) {
                        setText(null);
                    } else {
                        String text = item.getNom() + " (Stock: " + item.getQuantite() + ")";
                        setText(text);
                        
                        // Couleur selon le stock
                        if (item.getQuantite() == 0) {
                            setStyle("-fx-text-fill: red;");
                        } else if (item.getQuantite() < 10) {
                            setStyle("-fx-text-fill: orange;");
                        } else {
                            setStyle("-fx-text-fill: black;");
                        }
                    }
                }
            });
            
            comboMedicament.setButtonCell(new ListCell<Medicament>() {
                @Override
                protected void updateItem(Medicament item, boolean empty) {
                    super.updateItem(item, empty);
                    setText(empty || item == null ? null : item.getNom());
                }
            });
        } catch (Exception e) {
            showErrorAlert("Erreur", "Impossible de charger les médicaments: " + e.getMessage());
        }
    }

    /**
     * Enregistrement d'une nouvelle vente
     */
    @FXML
    public void enregistrerVente() {
        try {
            // Validation
            validerVente();

            Medicament medicament = comboMedicament.getValue();
            int quantite = Integer.parseInt(txtQuantite.getText().trim());
            LocalDate date = dateVente.getValue();

            // Enregistrement via le service
            Vente vente = ventesService.enregistrerVente(medicament, quantite, date);

            // Rafraîchissement
            chargerVentes();
            chargerMedicaments();  // Pour mettre à jour les stocks
            chargerStatistiques();
            reinitialiserFormulaire();

            showInfoAlert("Succès", 
                String.format("Vente enregistrée avec succès!\nMontant: %.2f DH", 
                vente.getMontantTotal()));

            if (lblStatut != null) {
                lblStatut.setText("Vente enregistrée: " + medicament.getNom());
                lblStatut.setStyle("-fx-text-fill: green;");
            }

        } catch (StockInsuffisantException e) {
            showErrorAlert("Stock Insuffisant", e.getMessage());
        } catch (MedicamentExpireException e) {
            showErrorAlert("Médicament Expiré", e.getMessage());
        } catch (DonneeInvalideException e) {
            showErrorAlert("Données Invalides", e.getMessage());
        } catch (NumberFormatException e) {
            showErrorAlert("Erreur de Format", "La quantité doit être un nombre entier");
        } catch (Exception e) {
            showErrorAlert("Erreur", "Erreur lors de l'enregistrement: " + e.getMessage());
            e.printStackTrace();
        }
    }

    /**
     * Validation d'une vente
     */
    private void validerVente() throws DonneeInvalideException {
        if (comboMedicament.getValue() == null) {
            throw new DonneeInvalideException("Veuillez sélectionner un médicament");
        }
        if (txtQuantite.getText().trim().isEmpty()) {
            throw new DonneeInvalideException("Veuillez saisir une quantité");
        }
        if (dateVente.getValue() == null) {
            throw new DonneeInvalideException("Veuillez sélectionner une date");
        }

        try {
            int quantite = Integer.parseInt(txtQuantite.getText().trim());
            if (quantite <= 0) {
                throw new DonneeInvalideException("La quantité doit être supérieure à 0");
            }
        } catch (NumberFormatException e) {
            throw new DonneeInvalideException("La quantité doit être un nombre entier valide");
        }
    }

    /**
     * Chargement des statistiques
     */
    @FXML
    public void chargerStatistiques() {
        try {
            // Statistiques générales
            double ventesJour = statistiquesService.calculerVentesJour();
            double ventesMois = statistiquesService.calculerVentesMois();
            int nombreVentesJour = statistiquesService.obtenirNombreVentesJour();
            String meilleurVente = statistiquesService.obtenirMeilleurVente();

            if (lblTotalVentesJour != null) {
                lblTotalVentesJour.setText(String.format("%.2f DH", ventesJour));
            }
            if (lblTotalVentesMois != null) {
                lblTotalVentesMois.setText(String.format("%.2f DH", ventesMois));
            }
            if (lblNombreVentesJour != null) {
                lblNombreVentesJour.setText(String.valueOf(nombreVentesJour));
            }
            if (lblMeilleurVente != null) {
                lblMeilleurVente.setText(meilleurVente != null ? meilleurVente : "N/A");
            }

            // Graphiques
            chargerPieChart();
            chargerLineChart();

        } catch (Exception e) {
            showErrorAlert("Erreur", "Erreur lors du chargement des statistiques: " + e.getMessage());
        }
    }

    /**
     * Chargement du graphique circulaire des ventes par médicament
     */
    private void chargerPieChart() {
        try {
            if (pieChartVentes == null) return;

            Map<String, Double> ventesParMedicament = statistiquesService.obtenirVentesParMedicament();
            
            ObservableList<PieChart.Data> pieChartData = FXCollections.observableArrayList();
            ventesParMedicament.forEach((medicament, montant) -> {
                pieChartData.add(new PieChart.Data(medicament, montant));
            });

            pieChartVentes.setData(pieChartData);
            pieChartVentes.setTitle("Top Ventes par Médicament");

        } catch (Exception e) {
            System.err.println("Erreur lors du chargement du PieChart: " + e.getMessage());
        }
    }

    /**
     * Chargement du graphique linéaire des ventes dans le temps
     */
    private void chargerLineChart() {
        try {
            if (lineChartVentes == null) return;

            Map<String, Double> ventesParJour = statistiquesService.obtenirVentesParJour(
                dateDebut != null ? dateDebut.getValue() : LocalDate.now().minusMonths(1),
                dateFin != null ? dateFin.getValue() : LocalDate.now()
            );

            XYChart.Series<String, Number> series = new XYChart.Series<>();
            series.setName("Ventes");

            ventesParJour.forEach((date, montant) -> {
                series.getData().add(new XYChart.Data<>(date, montant));
            });

            lineChartVentes.getData().clear();
            lineChartVentes.getData().add(series);
            lineChartVentes.setTitle("Évolution des Ventes");

        } catch (Exception e) {
            System.err.println("Erreur lors du chargement du LineChart: " + e.getMessage());
        }
    }

    /**
     * Filtrage des ventes par période
     */
    @FXML
    public void filtrerVentes() {
        try {
            if (dateDebut.getValue() == null || dateFin.getValue() == null) {
                showErrorAlert("Erreur", "Veuillez sélectionner une période");
                return;
            }

            LocalDate debut = dateDebut.getValue();
            LocalDate fin = dateFin.getValue();

            if (debut.isAfter(fin)) {
                showErrorAlert("Erreur", "La date de début doit être antérieure à la date de fin");
                return;
            }

            List<Vente> ventes = ventesService.obtenirVentesParPeriode(debut, fin);
            ventesList.clear();
            ventesList.addAll(ventes);

            // Mise à jour des statistiques pour la période
            chargerLineChart();

            if (lblStatut != null) {
                lblStatut.setText(ventes.size() + " vente(s) trouvée(s) pour la période");
                lblStatut.setStyle("-fx-text-fill: blue;");
            }

        } catch (Exception e) {
            showErrorAlert("Erreur", "Erreur lors du filtrage: " + e.getMessage());
        }
    }

    /**
     * Réinitialisation du formulaire
     */
    @FXML
    public void reinitialiserFormulaire() {
        comboMedicament.getSelectionModel().clearSelection();
        txtQuantite.clear();
        dateVente.setValue(LocalDate.now());
        lblPrixUnitaire.setText("0.00 DH");
        lblStockDisponible.setText("0");
        lblMontantTotal.setText("0.00 DH");
        lblStockDisponible.setStyle("");
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
     * Affichage d'une alerte d'avertissement
     */
    private void showWarningAlert(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.WARNING);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.show();
    }
}

