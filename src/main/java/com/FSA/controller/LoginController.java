
package com.FSA.controller;

import com.FSA.DAO.UtilisateurDAO;
import com.FSA.Navigation;
import com.FSA.chefprojectController;
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

        UtilisateurDAO.Connexion c = utilisateurService.connecter(email, password);
        if (c == null) {
            afficherErreur("Email ou mot de passe incorrect.");
            return;
        }

        if (c.role().equals("CHEF_DEPARTEMENT")) {
            Navigation.go(root, "dashboard.fxml");
        } else if (c.role().equals("CHEF_PROJET")) {
            ouvrirDashboardChef(c);                       // was the "pas encore disponible" error
        } else {
            afficherErreur("Rôle utilisateur non reconnu.");
        }
    }

    private void ouvrirDashboardChef(UtilisateurDAO.Connexion c) {
        try {
            FXMLLoader loader = new FXMLLoader(
                    getClass().getResource("/com/FSA/view/chefproject.fxml"));
            Parent page = loader.load();

            chefprojectController controller = loader.getController();
            controller.setChef(c.id(), c.nom(), c.email());   // gives the page the logged-in chef

            Stage stage = (Stage) root.getScene().getWindow();
            stage.setScene(new Scene(page, 1280, 720));
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
