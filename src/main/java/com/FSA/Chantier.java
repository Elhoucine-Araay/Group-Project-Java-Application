package com.FSA;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.image.Image;
import javafx.scene.control.Label;
import javafx.scene.layout.StackPane;
import javafx.stage.Stage;
import java.io.IOException;

public class Chantier extends Application {

    @Override
    public void start(Stage stage) throws IOException {
        Parent root = FXMLLoader.load(getClass().getResource("/com/FSA/view/login.fxml"));
        stage.getIcons().add(new Image(getClass().getResourceAsStream("/com/FSA/images/logo.png")));
        stage.setTitle("CS Management");
        stage.setScene(new Scene(root, 1280, 720));
        stage.show();
    }

    public static void main(String[] args) {
        launch(args);
    }
}
