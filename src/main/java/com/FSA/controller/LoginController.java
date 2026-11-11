
package com.FSA.controller;

import com.FSA.service.UtilisateurService;

import javafx.beans.binding.Bindings;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.Hyperlink;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.scene.layout.GridPane;
import javafx.stage.Stage;

import java.io.IOException;
import java.util.Locale;

public class LoginController {

    @FXML private GridPane root;
    @FXML private TextField emailField;
    @FXML private PasswordField passwordField;
    @FXML private Hyperlink forgotLink;
    @FXML private Button signInButton;

    private static final double BASE_FONT = 10.0;

    private final UtilisateurService utilisateurService =
            new UtilisateurService();

    @FXML
    private void initialize() {

        root.sceneProperty().addListener((obs, oldScene, scene) -> {

            if (scene == null) return;

            root.styleProperty().bind(Bindings.format(
                    Locale.US,
                    "-fx-font-size: %.2fpx;",
                    Bindings.min(
                            scene.widthProperty().divide(940.0),
                            scene.heightProperty().divide(535.0)
                    ).multiply(BASE_FONT)
            ));
        });

        signInButton.setOnAction(e -> seConnecter());
    }

    private void seConnecter() {

        String email = emailField.getText();
        String password = passwordField.getText();

        String role = utilisateurService.authentifier(email, password);

        if (role == null) {
            afficherErreur("Email ou mot de passe incorrect.");
            return;
        }

        // Redirect according to role
        if (role.equals("CHEF_DEPARTEMENT")) {

            ouvrirDashboard();

        } else if (role.equals("CHEF_PROJET")) {

            afficherErreur(
                    "Dashboard Chef Projet n'est pas encore disponible."
            );

        } else {

            afficherErreur("Rôle utilisateur non reconnu.");
        }
    }

    private void ouvrirDashboard() {

        try {

            Parent dashboard = FXMLLoader.load(
                    getClass().getResource(
                            "/com/FSA/view/dashboard.fxml"
                    )
            );

            Stage stage = (Stage) root.getScene().getWindow();

            stage.setScene(new Scene(dashboard, 1280, 720));
            stage.setTitle("CS Management");

        } catch (IOException | NullPointerException e) {

            afficherErreur("Impossible d'ouvrir le Dashboard.");
            e.printStackTrace();
        }
    }

    private void afficherErreur(String message) {

        Alert alert = new Alert(Alert.AlertType.ERROR);

        alert.setTitle("Erreur de connexion");
        alert.setHeaderText(null);
        alert.setContentText(message);

        alert.showAndWait();
    }
}
