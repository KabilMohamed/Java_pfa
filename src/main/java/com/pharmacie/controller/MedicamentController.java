package com.pharmacie.controller;

import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import com.pharmacie.model.Medicament;
import com.pharmacie.model.Fournisseur;
import com.pharmacie.service.StocksService;
import com.pharmacie.service.FournisseurService;
import com.pharmacie.exception.DonneeInvalideException;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * Contrôleur pour la gestion des médicaments
 * Permet d'ajouter, modifier, supprimer et rechercher des médicaments
 */
public class MedicamentController {

    // TableView et colonnes
    @FXML
    private TableView<Medicament> tableMedicaments;

    @FXML
    private TableColumn<Medicament, Integer> colId;

    @FXML
    private TableColumn<Medicament, String> colNom;

    @FXML
    private TableColumn<Medicament, String> colCategorie;

    @FXML
    private TableColumn<Medicament, Double> colPrix;

    @FXML
    private TableColumn<Medicament, Integer> colQuantite;

    @FXML
    private TableColumn<Medicament, String> colDateExpiration;

    @FXML
    private TableColumn<Medicament, String> colFournisseur;

    // Champs de formulaire
    @FXML
    private TextField txtNom;

    @FXML
    private TextField txtCategorie;

    @FXML
    private TextField txtPrix;

    @FXML
    private TextField txtQuantite;

    @FXML
    private DatePicker dateExpiration;

    @FXML
    private ComboBox<Fournisseur> comboFournisseur;

    // Champ de recherche
    @FXML
    private TextField txtRecherche;

    @FXML
    private ComboBox<String> comboFiltreCategorie;

    // Boutons
    @FXML
    private Button btnAjouter;

    @FXML
    private Button btnModifier;

    @FXML
    private Button btnSupprimer;

    @FXML
    private Button btnReinitialiser;

    @FXML
    private Button btnRechercher;

    @FXML
    private Label lblStatut;

    // Services
    private StocksService stocksService;
    private FournisseurService fournisseurService;
    private ObservableList<Medicament> medicamentsList;
    private Medicament medicamentSelectionne;

    /**
     * Initialisation du contrôleur
     */
    @FXML
    public void initialize() {
        // Initialisation des services
        stocksService = new StocksService();
        fournisseurService = new FournisseurService();
        medicamentsList = FXCollections.observableArrayList();

        // Configuration de la table
        configurerTableView();

        // Chargement des données
        chargerMedicaments();
        chargerFournisseurs();
        chargerCategories();

        // Configuration des événements
        configurerEvenements();

        // Désactiver le bouton modifier au départ
        if (btnModifier != null) {
            btnModifier.setDisable(true);
        }
        if (btnSupprimer != null) {
            btnSupprimer.setDisable(true);
        }
    }

    /**
     * Configuration de la TableView
     */
    private void configurerTableView() {
        // Configuration des colonnes
        colId.setCellValueFactory(new PropertyValueFactory<>("id"));
        colNom.setCellValueFactory(new PropertyValueFactory<>("nom"));
        colCategorie.setCellValueFactory(new PropertyValueFactory<>("categorie"));
        colPrix.setCellValueFactory(new PropertyValueFactory<>("prix"));
        colQuantite.setCellValueFactory(new PropertyValueFactory<>("quantite"));
        
        // Formatage de la date d'expiration
        colDateExpiration.setCellValueFactory(cellData -> 
            new SimpleStringProperty(cellData.getValue().getDateExpiration().toString()));
        
        // Affichage du nom du fournisseur
        colFournisseur.setCellValueFactory(cellData -> {
            Fournisseur f = cellData.getValue().getFournisseur();
            return new SimpleStringProperty(f != null ? f.getNom() : "N/A");
        });

        // Style conditionnel pour les quantités faibles et dates expirées
        colQuantite.setCellFactory(column -> new TableCell<Medicament, Integer>() {
            @Override
            protected void updateItem(Integer item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText(null);
                    setStyle("");
                } else {
                    setText(item.toString());
                    if (item == 0) {
                        setStyle("-fx-background-color: #ffcccc; -fx-text-fill: red; -fx-font-weight: bold;");
                    } else if (item < 10) {
                        setStyle("-fx-background-color: #fff3cd; -fx-text-fill: orange; -fx-font-weight: bold;");
                    } else {
                        setStyle("");
                    }
                }
            }
        });

        colDateExpiration.setCellFactory(column -> new TableCell<Medicament, String>() {
            @Override
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText(null);
                    setStyle("");
                } else {
                    setText(item);
                    Medicament med = getTableView().getItems().get(getIndex());
                    if (med.getDateExpiration().isBefore(LocalDate.now())) {
                        setStyle("-fx-background-color: #ffcccc; -fx-text-fill: red; -fx-font-weight: bold;");
                    } else if (med.getDateExpiration().isBefore(LocalDate.now().plusMonths(3))) {
                        setStyle("-fx-background-color: #fff3cd; -fx-text-fill: orange;");
                    } else {
                        setStyle("");
                    }
                }
            }
        });

        tableMedicaments.setItems(medicamentsList);
    }

    /**
     * Configuration des événements
     */
    private void configurerEvenements() {
        // Sélection dans la table
        tableMedicaments.getSelectionModel().selectedItemProperty().addListener(
            (observable, oldValue, newValue) -> {
                medicamentSelectionne = newValue;
                if (newValue != null) {
                    remplirFormulaire(newValue);
                    btnModifier.setDisable(false);
                    btnSupprimer.setDisable(false);
                } else {
                    btnModifier.setDisable(true);
                    btnSupprimer.setDisable(true);
                }
            }
        );

        // Recherche en temps réel
        if (txtRecherche != null) {
            txtRecherche.textProperty().addListener((observable, oldValue, newValue) -> {
                rechercherMedicaments();
            });
        }

        // Filtre par catégorie
        if (comboFiltreCategorie != null) {
            comboFiltreCategorie.valueProperty().addListener((observable, oldValue, newValue) -> {
                rechercherMedicaments();
            });
        }
    }

    /**
     * Chargement de tous les médicaments
     */
    private void chargerMedicaments() {
        try {
            List<Medicament> medicaments = stocksService.obtenirTousLesMedicaments();
            medicamentsList.clear();
            medicamentsList.addAll(medicaments);
            
            if (lblStatut != null) {
                lblStatut.setText(medicaments.size() + " médicament(s) chargé(s)");
                lblStatut.setStyle("-fx-text-fill: green;");
            }
        } catch (Exception e) {
            showErrorAlert("Erreur", "Impossible de charger les médicaments: " + e.getMessage());
        }
    }

    /**
     * Chargement des fournisseurs dans le ComboBox
     */
    private void chargerFournisseurs() {
        try {
            List<Fournisseur> fournisseurs = fournisseurService.obtenirTousLesFournisseurs();
            comboFournisseur.setItems(FXCollections.observableArrayList(fournisseurs));
            
            // Affichage personnalisé
            comboFournisseur.setCellFactory(param -> new ListCell<Fournisseur>() {
                @Override
                protected void updateItem(Fournisseur item, boolean empty) {
                    super.updateItem(item, empty);
                    setText(empty || item == null ? null : item.getNom());
                }
            });
            
            comboFournisseur.setButtonCell(new ListCell<Fournisseur>() {
                @Override
                protected void updateItem(Fournisseur item, boolean empty) {
                    super.updateItem(item, empty);
                    setText(empty || item == null ? null : item.getNom());
                }
            });
        } catch (Exception e) {
            showErrorAlert("Erreur", "Impossible de charger les fournisseurs: " + e.getMessage());
        }
    }

    /**
     * Chargement des catégories pour le filtre
     */
    private void chargerCategories() {
        try {
            List<String> categories = stocksService.obtenirTousLesMedicaments()
                .stream()
                .map(Medicament::getCategorie)
                .distinct()
                .sorted()
                .collect(Collectors.toList());
            
            categories.add(0, "Toutes les catégories");
            comboFiltreCategorie.setItems(FXCollections.observableArrayList(categories));
            comboFiltreCategorie.getSelectionModel().selectFirst();
        } catch (Exception e) {
            // Ignorer l'erreur
        }
    }

    /**
     * Ajout d'un nouveau médicament
     */
    @FXML
    public void ajouterMedicament() {
        try {
            // Validation des champs
            validerChamps();

            // Création du médicament
            Medicament medicament = new Medicament(
                0, // L'ID sera généré automatiquement
                txtNom.getText().trim(),
                txtCategorie.getText().trim(),
                Double.parseDouble(txtPrix.getText().trim()),
                Integer.parseInt(txtQuantite.getText().trim()),
                dateExpiration.getValue(),
                comboFournisseur.getValue()
            );

            // Ajout via le service
            stocksService.ajouterMedicament(medicament);

            // Rafraîchissement
            chargerMedicaments();
            reinitialiserFormulaire();
            
            showInfoAlert("Succès", "Médicament ajouté avec succès");
            
            if (lblStatut != null) {
                lblStatut.setText("Médicament ajouté: " + medicament.getNom());
                lblStatut.setStyle("-fx-text-fill: green;");
            }

        } catch (DonneeInvalideException e) {
            showErrorAlert("Données invalides", e.getMessage());
        } catch (NumberFormatException e) {
            showErrorAlert("Erreur de format", "Vérifiez que le prix et la quantité sont des nombres valides");
        } catch (Exception e) {
            showErrorAlert("Erreur", "Erreur lors de l'ajout: " + e.getMessage());
        }
    }

    /**
     * Modification d'un médicament existant
     */
    @FXML
    public void modifierMedicament() {
        if (medicamentSelectionne == null) {
            showErrorAlert("Aucune sélection", "Veuillez sélectionner un médicament à modifier");
            return;
        }

        try {
            // Validation des champs
            validerChamps();

            // Mise à jour des propriétés
            medicamentSelectionne.setNom(txtNom.getText().trim());
            medicamentSelectionne.setCategorie(txtCategorie.getText().trim());
            medicamentSelectionne.setPrix(Double.parseDouble(txtPrix.getText().trim()));
            medicamentSelectionne.setQuantite(Integer.parseInt(txtQuantite.getText().trim()));
            medicamentSelectionne.setDateExpiration(dateExpiration.getValue());
            medicamentSelectionne.setFournisseur(comboFournisseur.getValue());

            // Mise à jour via le service
            stocksService.modifierMedicament(medicamentSelectionne);

            // Rafraîchissement
            chargerMedicaments();
            reinitialiserFormulaire();
            
            showInfoAlert("Succès", "Médicament modifié avec succès");
            
            if (lblStatut != null) {
                lblStatut.setText("Médicament modifié: " + medicamentSelectionne.getNom());
                lblStatut.setStyle("-fx-text-fill: green;");
            }

        } catch (DonneeInvalideException e) {
            showErrorAlert("Données invalides", e.getMessage());
        } catch (NumberFormatException e) {
            showErrorAlert("Erreur de format", "Vérifiez que le prix et la quantité sont des nombres valides");
        } catch (Exception e) {
            showErrorAlert("Erreur", "Erreur lors de la modification: " + e.getMessage());
        }
    }

    /**
     * Suppression d'un médicament
     */
    @FXML
    public void supprimerMedicament() {
        if (medicamentSelectionne == null) {
            showErrorAlert("Aucune sélection", "Veuillez sélectionner un médicament à supprimer");
            return;
        }

        // Confirmation
        Alert confirmation = new Alert(Alert.AlertType.CONFIRMATION);
        confirmation.setTitle("Confirmation");
        confirmation.setHeaderText("Supprimer le médicament");
        confirmation.setContentText("Êtes-vous sûr de vouloir supprimer: " + 
            medicamentSelectionne.getNom() + " ?");

        Optional<ButtonType> result = confirmation.showAndWait();
        if (result.isPresent() && result.get() == ButtonType.OK) {
            try {
                stocksService.supprimerMedicament(medicamentSelectionne.getId());
                chargerMedicaments();
                reinitialiserFormulaire();
                
                showInfoAlert("Succès", "Médicament supprimé avec succès");
                
                if (lblStatut != null) {
                    lblStatut.setText("Médicament supprimé");
                    lblStatut.setStyle("-fx-text-fill: green;");
                }
            } catch (Exception e) {
                showErrorAlert("Erreur", "Erreur lors de la suppression: " + e.getMessage());
            }
        }
    }

    /**
     * Recherche de médicaments
     */
    @FXML
    public void rechercherMedicaments() {
        try {
            String recherche = txtRecherche.getText().toLowerCase().trim();
            String categorieFiltre = comboFiltreCategorie.getValue();

            List<Medicament> resultats = stocksService.obtenirTousLesMedicaments()
                .stream()
                .filter(med -> {
                    // Filtre par texte de recherche
                    boolean matchRecherche = recherche.isEmpty() || 
                        med.getNom().toLowerCase().contains(recherche) ||
                        med.getCategorie().toLowerCase().contains(recherche) ||
                        (med.getFournisseur() != null && 
                         med.getFournisseur().getNom().toLowerCase().contains(recherche));
                    
                    // Filtre par catégorie
                    boolean matchCategorie = categorieFiltre == null || 
                        categorieFiltre.equals("Toutes les catégories") ||
                        med.getCategorie().equals(categorieFiltre);
                    
                    return matchRecherche && matchCategorie;
                })
                .collect(Collectors.toList());

            medicamentsList.clear();
            medicamentsList.addAll(resultats);

            if (lblStatut != null) {
                lblStatut.setText(resultats.size() + " résultat(s) trouvé(s)");
                lblStatut.setStyle("-fx-text-fill: blue;");
            }

        } catch (Exception e) {
            showErrorAlert("Erreur", "Erreur lors de la recherche: " + e.getMessage());
        }
    }

    /**
     * Réinitialisation du formulaire
     */
    @FXML
    public void reinitialiserFormulaire() {
        txtNom.clear();
        txtCategorie.clear();
        txtPrix.clear();
        txtQuantite.clear();
        dateExpiration.setValue(null);
        comboFournisseur.getSelectionModel().clearSelection();
        
        tableMedicaments.getSelectionModel().clearSelection();
        medicamentSelectionne = null;
        
        btnModifier.setDisable(true);
        btnSupprimer.setDisable(true);
        
        if (lblStatut != null) {
            lblStatut.setText("Formulaire réinitialisé");
            lblStatut.setStyle("-fx-text-fill: gray;");
        }
    }

    /**
     * Remplissage du formulaire avec un médicament sélectionné
     */
    private void remplirFormulaire(Medicament medicament) {
        txtNom.setText(medicament.getNom());
        txtCategorie.setText(medicament.getCategorie());
        txtPrix.setText(String.valueOf(medicament.getPrix()));
        txtQuantite.setText(String.valueOf(medicament.getQuantite()));
        dateExpiration.setValue(medicament.getDateExpiration());
        comboFournisseur.setValue(medicament.getFournisseur());
    }

    /**
     * Validation des champs du formulaire
     */
    private void validerChamps() throws DonneeInvalideException {
        if (txtNom.getText().trim().isEmpty()) {
            throw new DonneeInvalideException("Le nom du médicament est obligatoire");
        }
        if (txtCategorie.getText().trim().isEmpty()) {
            throw new DonneeInvalideException("La catégorie est obligatoire");
        }
        if (txtPrix.getText().trim().isEmpty()) {
            throw new DonneeInvalideException("Le prix est obligatoire");
        }
        if (txtQuantite.getText().trim().isEmpty()) {
            throw new DonneeInvalideException("La quantité est obligatoire");
        }
        if (dateExpiration.getValue() == null) {
            throw new DonneeInvalideException("La date d'expiration est obligatoire");
        }
        if (comboFournisseur.getValue() == null) {
            throw new DonneeInvalideException("Le fournisseur est obligatoire");
        }

        // Validation des valeurs numériques
        try {
            double prix = Double.parseDouble(txtPrix.getText().trim());
            if (prix < 0) {
                throw new DonneeInvalideException("Le prix ne peut pas être négatif");
            }
        } catch (NumberFormatException e) {
            throw new DonneeInvalideException("Le prix doit être un nombre valide");
        }

        try {
            int quantite = Integer.parseInt(txtQuantite.getText().trim());
            if (quantite < 0) {
                throw new DonneeInvalideException("La quantité ne peut pas être négative");
            }
        } catch (NumberFormatException e) {
            throw new DonneeInvalideException("La quantité doit être un nombre entier valide");
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

