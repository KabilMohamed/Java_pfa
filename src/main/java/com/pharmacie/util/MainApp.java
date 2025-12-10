package com.pharmacie.util;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.image.Image;
import javafx.stage.Stage;

/**
 * Classe principale de l'application JavaFX
 * Point d'entrée de l'application graphique
 */
public class MainApp extends Application {
    
    private static Stage primaryStage;
    
    @Override
    public void start(Stage stage) {
        try {
            primaryStage = stage;
            
            // Chargement de la vue principale
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/Fxml/MainView.fxml"));
            Parent root = loader.load();
            
            // Configuration de la scène
            Scene scene = new Scene(root, 1200, 800);
            
            // Chargement du CSS si disponible
            try {
                String css = getClass().getResource("/css/style.css").toExternalForm();
                scene.getStylesheets().add(css);
            } catch (Exception e) {
                System.out.println("CSS non trouvé, utilisation du style par défaut");
            }
            
            // Configuration de la fenêtre principale
            primaryStage.setTitle("Système de Gestion de Stock - Pharmacie");
            primaryStage.setScene(scene);
            primaryStage.setMinWidth(1000);
            primaryStage.setMinHeight(600);
            
            // Icône de l'application (si disponible)
            try {
                Image icon = new Image(getClass().getResourceAsStream("/images/icon.png"));
                primaryStage.getIcons().add(icon);
            } catch (Exception e) {
                System.out.println("Icône non trouvée");
            }
            
            // Affichage de la fenêtre
            primaryStage.show();
            
            // Message de bienvenue dans la console
            afficherBanniere();
            
        } catch (Exception e) {
            System.err.println("Erreur lors du démarrage de l'application: " + e.getMessage());
            e.printStackTrace();
        }
    }
    
    /**
     * Obtient le stage principal de l'application
     * 
     * @return Stage principal
     */
    public static Stage getPrimaryStage() {
        return primaryStage;
    }
    
    /**
     * Affiche la bannière de démarrage dans la console
     */
    private void afficherBanniere() {
        System.out.println("╔═══════════════════════════════════════════════════════════════╗");
        System.out.println("║                                                               ║");
        System.out.println("║     SYSTÈME DE GESTION DE STOCK POUR PHARMACIE               ║");
        System.out.println("║     Version 1.0                                               ║");
        System.out.println("║                                                               ║");
        System.out.println("║     Fonctionnalités:                                          ║");
        System.out.println("║     • Gestion des médicaments                                 ║");
        System.out.println("║     • Gestion des ventes                                      ║");
        System.out.println("║     • Gestion des fournisseurs                                ║");
        System.out.println("║     • Surveillance des expirations                            ║");
        System.out.println("║     • Alertes automatiques                                    ║");
        System.out.println("║     • Statistiques et rapports                                ║");
        System.out.println("║     • Export/Import CSV                                       ║");
        System.out.println("║                                                               ║");
        System.out.println("╚═══════════════════════════════════════════════════════════════╝");
        System.out.println();
    }
    
    /**
     * Ferme l'application proprement
     */
    @Override
    public void stop() {
        System.out.println("Fermeture de l'application...");
        // Nettoyage des ressources si nécessaire
        System.exit(0);
    }
    
    /**
     * Méthode principale
     * 
     * @param args Arguments de ligne de commande
     */
    public static void main(String[] args) {
        launch(args);
    }
}

