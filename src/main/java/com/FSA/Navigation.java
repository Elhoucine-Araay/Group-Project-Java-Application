package com.FSA;

import java.io.IOException;

import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;

public final class Navigation {

    private Navigation() {}

    public static void go(Node from, String fxmlFile) {
        try {
            Parent page = FXMLLoader.load(Navigation.class.getResource("/com/FSA/view/" + fxmlFile));
            from.getScene().setRoot(page);
        } catch (IOException e) {
            throw new RuntimeException("Cannot open " + fxmlFile, e);
        }
    }
}