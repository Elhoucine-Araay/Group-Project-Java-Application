
package com.FSA.controller;

import com.FSA.Navigation;
import com.FSA.service.UtilisateurService;

import javafx.beans.binding.Bindings;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.control.Button;
import javafx.scene.control.Hyperlink;
import javafx.scene.control.PasswordField;
import javafx.scene.Node;
import javafx.scene.control.TextField;
import javafx.scene.layout.GridPane;
import javafx.stage.Stage;

import java.io.IOException;
import java.util.Locale;

public class LoginController {
    @FXML private Label errorLabel;
    @FXML private GridPane root;
    @FXML private TextField emailField;
    @FXML private PasswordField passwordField;
    @FXML private Hyperlink forgotLink;
    @FXML private Button signInButton;

    private static final double BASE_FONT = 10.0;

    private final UtilisateurService utilisateurService = new UtilisateurService();

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
        emailField.textProperty().addListener((obs, oldV, newV) -> cacherErreur());
        passwordField.textProperty().addListener((obs, oldV, newV) -> cacherErreur());
    }

    private void seConnecter() {
        cacherErreur();

        String email = emailField.getText();
        String password = passwordField.getText();

        String role = utilisateurService.authentifier(email, password);

        if (role == null) {
            afficherErreur("Email ou mot de passe incorrect.");
            return;
        }

        // Redirect according to role
        switch (role) {
            case "CHEF_DEPARTEMENT" -> Navigation.go(root, "dashboard.fxml");
            case "CHEF_PROJET"      -> Navigation.go(root, "chefproject.fxml");
            default -> afficherErreur("Rôle utilisateur non reconnu.");
        }
    }

    private void ouvrirDashboard() {

        try {

            Parent dashboard = FXMLLoader.load(getClass().getResource("/com/FSA/view/dashboard.fxml")
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
        errorLabel.setText(message);
        errorLabel.setVisible(true);
        errorLabel.setManaged(true);
        marquerChamps(true);
    }

    private void cacherErreur() {
        errorLabel.setVisible(false);
        errorLabel.setManaged(false);
        marquerChamps(false);
    }

    private void marquerChamps(boolean erreur) {
        for (Node champ : new Node[]{emailField.getParent(), passwordField.getParent()}) {
            champ.getStyleClass().remove("error");
            if (erreur) {
                champ.getStyleClass().add("error");
            }
        }
    }
}
