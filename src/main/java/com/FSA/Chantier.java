package com.FSA;

import javafx.application.Application;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.layout.StackPane;
import javafx.stage.Stage;

public class Chantier extends Application {

    @Override
    public void start(Stage stage) {
        StackPane root = new StackPane(new Label("Hello, JavaFX!"));
        stage.setTitle("School Project");
        stage.setScene(new Scene(root, 640, 400));
        stage.show();
    }

    public static void main(String[] args) {
        launch(args);
    }
}
