package com.FSA.controller;

import com.FSA.model.Chantier;
import com.FSA.service.ChantierService;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.DatePicker;
import javafx.scene.control.ProgressBar;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.scene.control.cell.PropertyValueFactory;

import java.sql.SQLException;

/** JavaFX controller: handles the screen and delegates work to the service. */
public class ChantierController {
    @FXML private TableView<Chantier> chantierTable;
    @FXML private TableColumn<Chantier, Integer> idColumn;
    @FXML private TableColumn<Chantier, String> nomColumn;
    @FXML private TableColumn<Chantier, String> adresseColumn;
    @FXML private TableColumn<Chantier, String> statutColumn;
    @FXML private TableColumn<Chantier, Double> avancementColumn;
    @FXML private TextField nomField;
    @FXML private TextField adresseField;
    @FXML private DatePicker dateDebutPicker;
    @FXML private DatePicker dateFinPicker;
    @FXML private TextField statutField;
    @FXML private TextField avancementField;
    @FXML private TextField chefDepartementIdField;
    @FXML private TextField chefProjetIdField;
    @FXML private Button updateButton;
    @FXML private Button deleteButton;

    private final ChantierService service = new ChantierService();
    private Chantier selectedChantier;

    @FXML
    private void initialize() {
        idColumn.setCellValueFactory(new PropertyValueFactory<>("idChantier"));
        nomColumn.setCellValueFactory(new PropertyValueFactory<>("nom"));
        adresseColumn.setCellValueFactory(new PropertyValueFactory<>("adresse"));
        statutColumn.setCellValueFactory(new PropertyValueFactory<>("statut"));
        avancementColumn.setCellValueFactory(new PropertyValueFactory<>("avancement"));
        chantierTable.getSelectionModel().selectedItemProperty().addListener(
                (observable, oldValue, newValue) -> selectChantier(newValue));
        refreshTable();
    }

    @FXML
    private void handleAdd() {
        try {
            Chantier chantier = readForm();
            int id = service.create(chantier);
            refreshTable();
            clearForm();
            showInfo("Chantier ajouté avec succès. ID : " + id);
        } catch (IllegalArgumentException | SQLException exception) {
            showError(exception.getMessage());
        }
    }

    @FXML
    private void handleUpdate() {
        if (selectedChantier == null) {
            showError("Sélectionnez d'abord un chantier dans le tableau.");
            return;
        }
        try {
            Chantier chantier = readForm();
            chantier.setIdChantier(selectedChantier.getIdChantier());
            if (service.update(chantier)) {
                refreshTable();
                clearForm();
                showInfo("Chantier modifié avec succès.");
            } else showError("Aucun chantier n'a été modifié.");
        } catch (IllegalArgumentException | SQLException exception) {
            showError(exception.getMessage());
        }
    }

    @FXML
    private void handleDelete() {
        if (selectedChantier == null) {
            showError("Sélectionnez d'abord un chantier dans le tableau.");
            return;
        }
        try {
            if (service.delete(selectedChantier.getIdChantier())) {
                refreshTable();
                clearForm();
                showInfo("Chantier supprimé avec succès.");
            } else showError("Le chantier n'existe plus ou a déjà été supprimé.");
        } catch (SQLException | IllegalArgumentException exception) {
            showError(exception.getMessage());
        }
    }

    @FXML
    private void handleClear() {
        clearForm();
        chantierTable.getSelectionModel().clearSelection();
    }

    private Chantier readForm() {
        Chantier chantier = new Chantier();
        chantier.setNom(nomField.getText().trim());
        chantier.setAdresse(adresseField.getText().trim());
        chantier.setDateDebut(dateDebutPicker.getValue());
        chantier.setDateFinPrevue(dateFinPicker.getValue());
        chantier.setStatut(statutField.getText().trim());
        try {
            chantier.setAvancement(Double.parseDouble(avancementField.getText().trim()));
            chantier.setIdChefDepartement(Integer.parseInt(chefDepartementIdField.getText().trim()));
            chantier.setIdChefProjet(Integer.parseInt(chefProjetIdField.getText().trim()));
        } catch (NumberFormatException exception) {
            throw new IllegalArgumentException("Avancement et identifiants des responsables doivent être numériques.");
        }
        return chantier;
    }

    private void refreshTable() {
        try {
            chantierTable.setItems(FXCollections.observableArrayList(service.findAll()));
        } catch (SQLException exception) {
            showError("Impossible de charger les chantiers : " + exception.getMessage());
        }
    }

    private void selectChantier(Chantier chantier) {
        selectedChantier = chantier;
        boolean hasSelection = chantier != null;
        updateButton.setDisable(!hasSelection);
        deleteButton.setDisable(!hasSelection);
        if (!hasSelection) return;
        nomField.setText(chantier.getNom());
        adresseField.setText(chantier.getAdresse());
        dateDebutPicker.setValue(chantier.getDateDebut());
        dateFinPicker.setValue(chantier.getDateFinPrevue());
        statutField.setText(chantier.getStatut());
        avancementField.setText(String.valueOf(chantier.getAvancement()));
        chefDepartementIdField.setText(String.valueOf(chantier.getIdChefDepartement()));
        chefProjetIdField.setText(String.valueOf(chantier.getIdChefProjet()));
    }

    private void clearForm() {
        selectedChantier = null;
        nomField.clear();
        adresseField.clear();
        dateDebutPicker.setValue(null);
        dateFinPicker.setValue(null);
        statutField.clear();
        avancementField.clear();
        chefDepartementIdField.clear();
        chefProjetIdField.clear();
        updateButton.setDisable(true);
        deleteButton.setDisable(true);
    }

    private void showInfo(String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Gestion des chantiers");
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    private void showError(String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle("Erreur");
        alert.setHeaderText("Opération impossible");
        alert.setContentText(message == null ? "Erreur inconnue." : message);
        alert.showAndWait();
    }
}
