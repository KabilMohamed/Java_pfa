package com.pharmacie.controller;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import com.pharmacie.model.Fournisseur;
import com.pharmacie.service.FournisseurService;
import com.pharmacie.exception.DonneeInvalideException;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * Contrôleur pour la gestion des fournisseurs
 * Permet d'ajouter, modifier, supprimer et rechercher des fournisseurs
 */
public class FournisseurController {

    // TableView et colonnes
    @FXML
    private TableView<Fournisseur> tableFournisseurs;

    @FXML
    private TableColumn<Fournisseur, Integer> colId;

    @FXML
    private TableColumn<Fournisseur, String> colNom;

    @FXML
    private TableColumn<Fournisseur, String> colAdresse;

    @FXML
    private TableColumn<Fournisseur, String> colTelephone;

    @FXML
    private TableColumn<Fournisseur, String> colEmail;

    @FXML
    private TableColumn<Fournisseur, String> colContact;

    // Champs de formulaire
    @FXML
    private TextField txtNom;

    @FXML
    private TextField txtAdresse;

    @FXML
    private TextField txtTelephone;

    @FXML
    private TextField txtEmail;

    @FXML
    private TextField txtContact;

    @FXML
    private TextArea txtNotes;

    // Champ de recherche
    @FXML
    private TextField txtRecherche;

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

    @FXML
    private Label lblTotalFournisseurs;

    // Service
    private FournisseurService fournisseurService;
    private ObservableList<Fournisseur> fournisseursList;
    private Fournisseur fournisseurSelectionne;

    /**
     * Initialisation du contrôleur
     */
    @FXML
    public void initialize() {
        // Initialisation du service
        fournisseurService = new FournisseurService();
        fournisseursList = FXCollections.observableArrayList();

        // Configuration de la table
        configurerTableView();

        // Chargement des données
        chargerFournisseurs();

        // Configuration des événements
        configurerEvenements();

        // Désactiver les boutons au départ
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
        colId.setCellValueFactory(new PropertyValueFactory<>("id"));
        colNom.setCellValueFactory(new PropertyValueFactory<>("nom"));
        colAdresse.setCellValueFactory(new PropertyValueFactory<>("adresse"));
        colTelephone.setCellValueFactory(new PropertyValueFactory<>("telephone"));
        colEmail.setCellValueFactory(new PropertyValueFactory<>("email"));
        colContact.setCellValueFactory(new PropertyValueFactory<>("contact"));

        tableFournisseurs.setItems(fournisseursList);

        // Style pour les lignes alternées
        tableFournisseurs.setRowFactory(tv -> {
            TableRow<Fournisseur> row = new TableRow<>();
            row.setOnMouseClicked(event -> {
                if (event.getClickCount() == 2 && !row.isEmpty()) {
                    Fournisseur fournisseur = row.getItem();
                    afficherDetailsFournisseur(fournisseur);
                }
            });
            return row;
        });
    }

    /**
     * Configuration des événements
     */
    private void configurerEvenements() {
        // Sélection dans la table
        tableFournisseurs.getSelectionModel().selectedItemProperty().addListener(
            (observable, oldValue, newValue) -> {
                fournisseurSelectionne = newValue;
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
                rechercherFournisseurs();
            });
        }

        // Validation du téléphone
        if (txtTelephone != null) {
            txtTelephone.textProperty().addListener((observable, oldValue, newValue) -> {
                if (!newValue.matches("\\d*")) {
                    txtTelephone.setText(newValue.replaceAll("[^\\d]", ""));
                }
            });
        }

        // Validation de l'email
        if (txtEmail != null) {
            txtEmail.focusedProperty().addListener((observable, oldValue, newValue) -> {
                if (!newValue && !txtEmail.getText().isEmpty()) {
                    if (!isValidEmail(txtEmail.getText())) {
                        txtEmail.setStyle("-fx-border-color: red;");
                    } else {
                        txtEmail.setStyle("");
                    }
                }
            });
        }
    }

    /**
     * Chargement de tous les fournisseurs
     */
    private void chargerFournisseurs() {
        try {
            List<Fournisseur> fournisseurs = fournisseurService.obtenirTousLesFournisseurs();
            fournisseursList.clear();
            fournisseursList.addAll(fournisseurs);

            if (lblTotalFournisseurs != null) {
                lblTotalFournisseurs.setText(String.valueOf(fournisseurs.size()));
            }

            if (lblStatut != null) {
                lblStatut.setText(fournisseurs.size() + " fournisseur(s) chargé(s)");
                lblStatut.setStyle("-fx-text-fill: green;");
            }
        } catch (Exception e) {
            showErrorAlert("Erreur", "Impossible de charger les fournisseurs: " + e.getMessage());
        }
    }

    /**
     * Ajout d'un nouveau fournisseur
     */
    @FXML
    public void ajouterFournisseur() {
        try {
            // Validation des champs
            validerChamps();

            // Création du fournisseur
            Fournisseur fournisseur = new Fournisseur(
                0, // L'ID sera généré automatiquement
                txtNom.getText().trim(),
                txtAdresse.getText().trim(),
                txtTelephone.getText().trim(),
                txtEmail.getText().trim(),
                txtContact.getText().trim()
            );

            // Ajout des notes si présentes
            if (txtNotes != null && !txtNotes.getText().isEmpty()) {
                fournisseur.setNotes(txtNotes.getText().trim());
            }

            // Ajout via le service
            fournisseurService.ajouterFournisseur(fournisseur);

            // Rafraîchissement
            chargerFournisseurs();
            reinitialiserFormulaire();

            showInfoAlert("Succès", "Fournisseur ajouté avec succès");

            if (lblStatut != null) {
                lblStatut.setText("Fournisseur ajouté: " + fournisseur.getNom());
                lblStatut.setStyle("-fx-text-fill: green;");
            }

        } catch (DonneeInvalideException e) {
            showErrorAlert("Données invalides", e.getMessage());
        } catch (Exception e) {
            showErrorAlert("Erreur", "Erreur lors de l'ajout: " + e.getMessage());
        }
    }

    /**
     * Modification d'un fournisseur existant
     */
    @FXML
    public void modifierFournisseur() {
        if (fournisseurSelectionne == null) {
            showErrorAlert("Aucune sélection", "Veuillez sélectionner un fournisseur à modifier");
            return;
        }

        try {
            // Validation des champs
            validerChamps();

            // Mise à jour des propriétés
            fournisseurSelectionne.setNom(txtNom.getText().trim());
            fournisseurSelectionne.setAdresse(txtAdresse.getText().trim());
            fournisseurSelectionne.setTelephone(txtTelephone.getText().trim());
            fournisseurSelectionne.setEmail(txtEmail.getText().trim());
            fournisseurSelectionne.setContact(txtContact.getText().trim());
            
            if (txtNotes != null) {
                fournisseurSelectionne.setNotes(txtNotes.getText().trim());
            }

            // Mise à jour via le service
            fournisseurService.modifierFournisseur(fournisseurSelectionne);

            // Rafraîchissement
            chargerFournisseurs();
            reinitialiserFormulaire();

            showInfoAlert("Succès", "Fournisseur modifié avec succès");

            if (lblStatut != null) {
                lblStatut.setText("Fournisseur modifié: " + fournisseurSelectionne.getNom());
                lblStatut.setStyle("-fx-text-fill: green;");
            }

        } catch (DonneeInvalideException e) {
            showErrorAlert("Données invalides", e.getMessage());
        } catch (Exception e) {
            showErrorAlert("Erreur", "Erreur lors de la modification: " + e.getMessage());
        }
    }

    /**
     * Suppression d'un fournisseur
     */
    @FXML
    public void supprimerFournisseur() {
        if (fournisseurSelectionne == null) {
            showErrorAlert("Aucune sélection", "Veuillez sélectionner un fournisseur à supprimer");
            return;
        }

        // Vérification des dépendances
        if (fournisseurService.fournisseurEstUtilise(fournisseurSelectionne.getId())) {
            Alert confirmation = new Alert(Alert.AlertType.WARNING);
            confirmation.setTitle("Confirmation");
            confirmation.setHeaderText("Fournisseur utilisé");
            confirmation.setContentText(
                "Ce fournisseur est associé à des médicaments existants.\n" +
                "Êtes-vous sûr de vouloir le supprimer? Les médicaments associés " +
                "perdront leur référence au fournisseur."
            );
            
            Optional<ButtonType> result = confirmation.showAndWait();
            if (result.isEmpty() || result.get() != ButtonType.OK) {
                return;
            }
        } else {
            // Confirmation simple
            Alert confirmation = new Alert(Alert.AlertType.CONFIRMATION);
            confirmation.setTitle("Confirmation");
            confirmation.setHeaderText("Supprimer le fournisseur");
            confirmation.setContentText("Êtes-vous sûr de vouloir supprimer: " + 
                fournisseurSelectionne.getNom() + " ?");

            Optional<ButtonType> result = confirmation.showAndWait();
            if (result.isEmpty() || result.get() != ButtonType.OK) {
                return;
            }
        }

        try {
            fournisseurService.supprimerFournisseur(fournisseurSelectionne.getId());
            chargerFournisseurs();
            reinitialiserFormulaire();

            showInfoAlert("Succès", "Fournisseur supprimé avec succès");

            if (lblStatut != null) {
                lblStatut.setText("Fournisseur supprimé");
                lblStatut.setStyle("-fx-text-fill: green;");
            }
        } catch (Exception e) {
            showErrorAlert("Erreur", "Erreur lors de la suppression: " + e.getMessage());
        }
    }

    /**
     * Recherche de fournisseurs
     */
    @FXML
    public void rechercherFournisseurs() {
        try {
            String recherche = txtRecherche.getText().toLowerCase().trim();

            if (recherche.isEmpty()) {
                chargerFournisseurs();
                return;
            }

            List<Fournisseur> resultats = fournisseurService.obtenirTousLesFournisseurs()
                .stream()
                .filter(f -> 
                    f.getNom().toLowerCase().contains(recherche) ||
                    f.getAdresse().toLowerCase().contains(recherche) ||
                    f.getTelephone().contains(recherche) ||
                    f.getEmail().toLowerCase().contains(recherche) ||
                    f.getContact().toLowerCase().contains(recherche)
                )
                .collect(Collectors.toList());

            fournisseursList.clear();
            fournisseursList.addAll(resultats);

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
        txtAdresse.clear();
        txtTelephone.clear();
        txtEmail.clear();
        txtContact.clear();
        if (txtNotes != null) {
            txtNotes.clear();
        }

        txtEmail.setStyle("");

        tableFournisseurs.getSelectionModel().clearSelection();
        fournisseurSelectionne = null;

        btnModifier.setDisable(true);
        btnSupprimer.setDisable(true);

        if (lblStatut != null) {
            lblStatut.setText("Formulaire réinitialisé");
            lblStatut.setStyle("-fx-text-fill: gray;");
        }
    }

    /**
     * Remplissage du formulaire avec un fournisseur sélectionné
     */
    private void remplirFormulaire(Fournisseur fournisseur) {
        txtNom.setText(fournisseur.getNom());
        txtAdresse.setText(fournisseur.getAdresse());
        txtTelephone.setText(fournisseur.getTelephone());
        txtEmail.setText(fournisseur.getEmail());
        txtContact.setText(fournisseur.getContact());
        if (txtNotes != null && fournisseur.getNotes() != null) {
            txtNotes.setText(fournisseur.getNotes());
        }
    }

    /**
     * Affichage des détails d'un fournisseur dans une boîte de dialogue
     */
    private void afficherDetailsFournisseur(Fournisseur fournisseur) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Détails du Fournisseur");
        alert.setHeaderText(fournisseur.getNom());
        
        StringBuilder details = new StringBuilder();
        details.append("ID: ").append(fournisseur.getId()).append("\n\n");
        details.append("Adresse:\n").append(fournisseur.getAdresse()).append("\n\n");
        details.append("Téléphone: ").append(fournisseur.getTelephone()).append("\n");
        details.append("Email: ").append(fournisseur.getEmail()).append("\n");
        details.append("Contact: ").append(fournisseur.getContact()).append("\n");
        
        if (fournisseur.getNotes() != null && !fournisseur.getNotes().isEmpty()) {
            details.append("\nNotes:\n").append(fournisseur.getNotes());
        }

        // Nombre de médicaments associés
        int nbMedicaments = fournisseurService.obtenirNombreMedicaments(fournisseur.getId());
        details.append("\n\nMédicaments associés: ").append(nbMedicaments);

        alert.setContentText(details.toString());
        alert.showAndWait();
    }

    /**
     * Validation des champs du formulaire
     */
    private void validerChamps() throws DonneeInvalideException {
        if (txtNom.getText().trim().isEmpty()) {
            throw new DonneeInvalideException("Le nom du fournisseur est obligatoire");
        }
        if (txtAdresse.getText().trim().isEmpty()) {
            throw new DonneeInvalideException("L'adresse est obligatoire");
        }
        if (txtTelephone.getText().trim().isEmpty()) {
            throw new DonneeInvalideException("Le téléphone est obligatoire");
        }
        if (txtEmail.getText().trim().isEmpty()) {
            throw new DonneeInvalideException("L'email est obligatoire");
        }
        if (txtContact.getText().trim().isEmpty()) {
            throw new DonneeInvalideException("Le nom du contact est obligatoire");
        }

        // Validation du format d'email
        if (!isValidEmail(txtEmail.getText().trim())) {
            throw new DonneeInvalideException("Le format de l'email est invalide");
        }

        // Validation du téléphone (minimum 10 chiffres)
        if (txtTelephone.getText().trim().length() < 10) {
            throw new DonneeInvalideException("Le téléphone doit contenir au moins 10 chiffres");
        }
    }

    /**
     * Validation du format d'email
     */
    private boolean isValidEmail(String email) {
        String emailRegex = "^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$";
        return email.matches(emailRegex);
    }

    /**
     * Export de la liste des fournisseurs
     */
    @FXML
    public void exporterFournisseurs() {
        try {
            fournisseurService.exporterFournisseursCSV();
            showInfoAlert("Export réussi", "La liste des fournisseurs a été exportée avec succès");
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

