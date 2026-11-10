package com.FSA;

import java.util.Locale;

import javafx.beans.binding.Bindings;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Hyperlink;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.scene.layout.GridPane;

public class LoginController {

    @FXML private GridPane root;
    @FXML private TextField emailField;
    @FXML private PasswordField passwordField;
    @FXML private Hyperlink forgotLink;
    @FXML private Button signInButton;

    private static final double BASE_FONT = 10.0;

    @FXML
    private void initialize() {
        root.sceneProperty().addListener((obs, oldScene, scene) -> {
            if (scene == null) return;

            root.styleProperty().bind(Bindings.format(Locale.US, "-fx-font-size: %.2fpx;",
                    Bindings.min(
                            scene.widthProperty().divide(940.0),
                            scene.heightProperty().divide(535.0)
                    ).multiply(BASE_FONT)));
        });

        signInButton.setOnAction(e -> {
            String email = emailField.getText();
            String password = passwordField.getText();
        });
    }
}