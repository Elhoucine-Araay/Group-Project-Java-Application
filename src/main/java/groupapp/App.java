
package com.groupapp;

import javafx.application.Application;
import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.control.Button;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

public class App extends Application {

    @Override
    public void start(Stage stage) {
        Label title = new Label("Group Project");
        title.setStyle("-fx-font-size: 24px; -fx-font-weight: bold;");

        Label status = new Label("JavaFX is working!");

        Button button = new Button("Test application");
        button.setOnAction(event ->
            status.setText("Your desktop application is running!")
        );

        VBox layout = new VBox(15, title, status, button);
        layout.setPadding(new Insets(25));

        stage.setTitle("Group Project");
        stage.setScene(new Scene(layout, 500, 300));
        stage.show();
    }

    public static void main(String[] args) {
        launch(args);
    }
}